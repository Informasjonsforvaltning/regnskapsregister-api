package no.brreg.regnskap.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import no.brreg.regnskap.TestData;
import no.brreg.regnskap.XmlTestData;
import no.brreg.regnskap.controller.TestApiImpl;
import no.brreg.regnskap.repository.ConnectionManager;
import no.brreg.regnskap.repository.RegnskapLogRepository;
import no.brreg.regnskap.repository.RegnskapRepository;

import no.brreg.regnskap.utils.EmbeddedPostgresSetup;

public class TestApiIT extends EmbeddedPostgresSetup {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestApiIT.class);

    final static String TESTDATA_FILENAME = "xmlTestString";

    @Autowired
    private TestApiImpl testApiImpl;

    @Autowired
    private RegnskapLogRepository regnskapLogRepository;
    
    private static boolean hasImportedTestdata = false;
    private static Integer regnskapId1;
    private static Integer regnskapId2;

    private static Integer regnskap2016Id;
    private static Integer regnskap2017Id;
    private static Integer regnskap2018_1Id;
    private static Integer regnskap2018_2Id;
    private static Integer regnskap2018_3Id;
    private static Integer regnskap2019_1Id;
    private static Integer regnskap2019_2Id;

    @Autowired
    private RegnskapRepository regnskapRepository;

    @Autowired
    private ConnectionManager connectionManager;

    @Mock
    HttpServletRequest httpServletRequestMock;


    @BeforeEach
    void resetMocks() throws IOException, SQLException {
        Mockito.reset(
            httpServletRequestMock
        );

        if (!hasImportedTestdata) {
            /*
            InputStream testdataIS = new ByteArrayInputStream(XmlTestData.xmlTestString.getBytes(StandardCharsets.UTF_8));
            try {
                regnskapLogRepository.persistRegnskapFile(TESTDATA_FILENAME, testdataIS);
            } catch (SQLException e) {
                LOGGER.info("Regnskap file test data already loaded");
            }

            try {
                regnskap2016Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2016S);
                regnskap2017Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2017S);
                regnskap2018_1Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_1S);
                regnskap2018_2Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_2S);
                regnskap2018_3Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_3K);
                regnskap2019_1Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2019_1S);
                regnskap2019_2Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2019_2K);
            } catch (SQLException e) {
                LOGGER.info("Regnskap test data already loaded");
            }
            */

            //regnskapId1 = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2_2015S);

            //Add partner
            Connection connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO rregapi.partners (name,key) VALUES ('test','test')")) {
                stmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                LOGGER.info("Partner test data already loaded");
            }

            hasImportedTestdata = true;
        }
    }

    @Test
    public void orgnrOfLastAddedRegnskapIsReturned() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        ResponseEntity<String> response = testApiImpl.getMostRecent(httpServletRequestMock);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), TestData.TEST_ORGNR_2);
    }
}
