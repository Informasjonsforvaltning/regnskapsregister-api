package no.regnskap.integration;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import no.regnskap.model.RegnskapDB;
import no.regnskap.testcategories.IntegrationTest;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static no.regnskap.TestData.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Category(IntegrationTest.class)
@Ignore
public class RegnskapApiTest {
    private static File testComposeFile = createTmpComposeFile();
    private final static Logger logger = LoggerFactory.getLogger(RegnskapApiTest.class);
    private static Slf4jLogConsumer mongoLog = new Slf4jLogConsumer(logger).withPrefix("mongo-container");
    private static Slf4jLogConsumer apiLog = new Slf4jLogConsumer(logger).withPrefix("api-container");
    private static DockerComposeContainer compose;

    //trigger build

    @BeforeClass
    public static void setup() {
        if (testComposeFile != null && testComposeFile.exists()) {
            compose = new DockerComposeContainer<>(testComposeFile)
                .withExposedService(MONGO_SERVICE_NAME, MONGO_PORT, Wait.forListeningPort())
                .withExposedService(API_SERVICE_NAME, API_PORT, Wait.forHttp("/ready").forStatusCode(200))
                .withTailChildContainers(true)
                .withPull(false)
                .withLocalCompose(true)
                .withLogConsumer(MONGO_SERVICE_NAME, mongoLog)
                .withLogConsumer(API_SERVICE_NAME, apiLog);

            compose.start();
        } else {
            logger.debug("Unable to start containers, missing test-compose.yml");
        }

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientURI uri = new MongoClientURI(buildMongoURI(compose.getServiceHost(MONGO_SERVICE_NAME, MONGO_PORT), compose.getServicePort(MONGO_SERVICE_NAME, MONGO_PORT)));
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RegnskapDB> mongoCollection = mongoDatabase.getCollection(COLLECTION_NAME).withDocumentClass(RegnskapDB.class);

        mongoCollection.insertOne(regnskap2018Second);
        mongoCollection.insertOne(regnskap2017);
        mongoCollection.insertOne(regnskap2018First);

        mongoClient.close();
    }

    @AfterClass
    public static void teardown() {
        if (testComposeFile != null && testComposeFile.exists()) {
            compose.stop();

            logger.debug("Delete temporary test-compose.yml: " + testComposeFile.delete());
        } else {
            logger.debug("Teardown skipped, missing test-compose.yml");
        }
    }

    @Test
    public void pingTest() throws Exception {
        String response = simpleGet(buildRegnskapURL("/ping"));
        Assert.assertEquals("RegnskapAPI is available", "pong", response);
    }

    @Test
    public void getByOrgnr() throws Exception {
        String response = simpleGet(buildRegnskapURL("/regnskap?orgNummer=orgnummer"));
        Assert.assertEquals(EXPECTED_RESPONSE_ORGNR, response);
    }

    @Test
    public void getById() throws Exception {
        String response2018 = simpleGet(buildRegnskapURL("/regnskap/" + GENERATED_ID_1.toHexString()));
        String response2017 = simpleGet(buildRegnskapURL("/regnskap/" + GENERATED_ID_0.toHexString()));
        Assert.assertEquals(buildExpectedDatabaseResponse(GENERATED_ID_1, 2018), response2018);
        Assert.assertEquals(buildExpectedDatabaseResponse(GENERATED_ID_0, 2017), response2017);
    }

    private String simpleGet(URL address) throws Exception {
        HttpURLConnection con = (HttpURLConnection) address.openConnection();
        con.setRequestMethod("GET");

        StringBuilder content = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            reader.lines().forEach(line -> content.append(line));
        }

        return content.toString();
    }

    private URL buildRegnskapURL(String address) throws MalformedURLException {
        return new URL("http", compose.getServiceHost(API_SERVICE_NAME, API_PORT), compose.getServicePort(API_SERVICE_NAME, API_PORT), address);
    }

    private static File createTmpComposeFile() {
        try {
            File tmpComposeFile = File.createTempFile("test-compose", ".yml");
            InputStream testCompseStream = IOUtils.toInputStream(TEST_COMPOSE, StandardCharsets.UTF_8);

            try (FileOutputStream outputStream = new FileOutputStream(tmpComposeFile)) {
                int read;
                byte[] bytes = new byte[1024];

                while ((read = testCompseStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }

            return tmpComposeFile;
        } catch (IOException e) {
            return null;
        }
    }
}
