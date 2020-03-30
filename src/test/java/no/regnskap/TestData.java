package no.regnskap;

import com.google.common.collect.ImmutableMap;
import no.regnskap.generated.model.*;
import no.regnskap.model.*;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestData {
    private static final String MONGO_USER = "testuser";
    private static final String MONGO_PASSWORD = "testpassword";
    private static final String MONGO_AUTH = "?authSource=admin&authMechanism=SCRAM-SHA-1";
    public static final int MONGO_PORT = 27017;
    public static final String DATABASE_NAME = "regnskapAPI";

    public static final Map<String, String> MONGO_ENV_VALUES = ImmutableMap.of(
        "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
        "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD);

    public static final String SFTP_USER = "sftpuser";
    public static final String SFTP_PWD = "sftppwd";
    public static final int SFTP_PORT = 22;
    private static final String SFTP_DIRECTORY = "sftpdir";
    public static final String SFTP_DIR = "/" + SFTP_DIRECTORY;
    public static final Map<String, String> SFTP_ENV_VALUES =
        ImmutableMap.of("SFTP_USERS", SFTP_USER + ":" + SFTP_PWD + ":::" + SFTP_DIRECTORY);

    public static String buildMongoURI(String host, int port, boolean withDbName) {
        String uri = "mongodb://" + MONGO_USER + ":" + MONGO_PASSWORD + "@" + host + ":" + port + "/";

        if (withDbName) {
            uri += DATABASE_NAME;
        }

        return uri + MONGO_AUTH;
    }

    public static String rregUrl = "http://invalid.org/regnskap/";
    public static String orgcatUrl = "https://invalid.org/organizations/";

    public static ObjectId GENERATED_ID_0 = ObjectId.get();
    public static ObjectId GENERATED_ID_1 = ObjectId.get();
    public static ObjectId GENERATED_ID_2 = new ObjectId("5d81fe657091ee0cce6bccdb");
    public static ObjectId GENERATED_ID_3 = ObjectId.get();

    public static Regnskap REGNSKAP_2018 = createRegnskap(GENERATED_ID_2, 2018);
    public static Regnskap REGNSKAP_2017 = createRegnskap(GENERATED_ID_0, 2017);

    private static Regnskap createRegnskap(ObjectId id, int year) {
        return new Regnskap()
            .id(id.toHexString())
            .avviklingsregnskap(true)
            .valuta("valutakode")
            .oppstillingsplan(Regnskap.OppstillingsplanEnum.fromValue("store"))
            .revisjon(
                new Revisjon()
                    .ikkeRevidertAarsregnskap(true))
            .regnskapsperiode(
                new Tidsperiode()
                    .fraDato(LocalDate.of(year, 1, 1))
                    .tilDato(LocalDate.of(year, 12, 31)))
            .regnkapsprinsipper(
                new Regnskapsprinsipper()
                    .smaaForetak(true)
                    .regnskapsregler(Regnskapsprinsipper.RegnskapsreglerEnum.REGNSKAPSLOVENALMINNELIGREGLER))
            .virksomhet(
                new Virksomhet()
                    .organisasjonsnummer("orgnummer")
                    .organisasjonsform("orgform")
                    .morselskap(true))
            .egenkapitalGjeld(egenkapitalGjeldWithValues(year))
            .eiendeler(eiendelerWithValues(year))
            .resultatregnskapResultat(resultatregnskapResultatWithValues(year));
    }

    public static List<Regnskap> EMPTY_REGNSKAP_LIST = new ArrayList<>();

    public static List<Regnskap> REGNSKAP_LIST = Collections.singletonList(REGNSKAP_2018);

    public static List<RegnskapDB> EMPTY_DB_REGNSKAP_LIST = new ArrayList<>();

    public static final RegnskapDB DB_REGNSKAP_2017 = createRegnskapDB(GENERATED_ID_0, 2017, "0");
    public static final RegnskapDB DB_REGNSKAP_2018_FIRST = createRegnskapDB(GENERATED_ID_1, 2018, "1");
    public static final RegnskapDB DB_REGNSKAP_2018_SECOND = createRegnskapDB(GENERATED_ID_2, 2018, "2");

    public static RegnskapDB dbRegnskapEmptyFields() {
        RegnskapDB emptyRegnskapDB = new RegnskapDB();
        emptyRegnskapDB.setId(GENERATED_ID_3);
        emptyRegnskapDB.setFields(new RegnskapFieldsDB());
        return emptyRegnskapDB;
    }

    public static List<RegnskapDB> DB_REGNSKAP_LIST = createDatabaseList();

    public static RegnskapDB createRegnskapDB(ObjectId id, int year, String journalnr) {
        RegnskapDB tmpRegnskapDB = new RegnskapDB();
        tmpRegnskapDB.setOrgnr("orgnummer");
        tmpRegnskapDB.setOrgform("orgform");
        tmpRegnskapDB.setRegnskapstype("S");
        tmpRegnskapDB.setOppstillingsplanVersjonsnr("oppstillingsplanVersjonsnr");
        tmpRegnskapDB.setValutakode("valutakode");
        tmpRegnskapDB.setStartdato(LocalDate.of(year, 1, 1));
        tmpRegnskapDB.setAvslutningsdato(LocalDate.of(year, 12, 31));
        tmpRegnskapDB.setMottakstype("mottakstype");
        tmpRegnskapDB.setAvviklingsregnskap(true);
        tmpRegnskapDB.setBistandRegnskapsforer(true);
        tmpRegnskapDB.setJournalnr(journalnr);
        tmpRegnskapDB.setMottattDato(LocalDate.now());
        tmpRegnskapDB.setFeilvaloer(true);
        tmpRegnskapDB.setFleksiblePoster(true);
        tmpRegnskapDB.setFravalgRevisjon(false);
        tmpRegnskapDB.setLandForLand(false);
        tmpRegnskapDB.setMorselskap(true);
        tmpRegnskapDB.setReglerSmaa(true);
        tmpRegnskapDB.setAarsregnskapstype("STORE");
        tmpRegnskapDB.setUtarbeidetRegnskapsforer(false);
        tmpRegnskapDB.setRevisorberetningIkkeLevert(true);
        tmpRegnskapDB.setIfrsSelskap(false);
        tmpRegnskapDB.setForenkletIfrsSelskap(false);
        tmpRegnskapDB.setIfrsKonsern(false);
        tmpRegnskapDB.setForenkletIfrsKonsern(false);
        tmpRegnskapDB.setFields(new RegnskapFieldsDB().copy(
            eiendelerWithValues(year),
            egenkapitalGjeldWithValues(year),
            resultatregnskapResultatWithValues(year)
        ));

        tmpRegnskapDB.setRegnaar(year);

        tmpRegnskapDB.setId(id);

        return tmpRegnskapDB;
    }

    private static List<RegnskapDB> createDatabaseList() {
        List<RegnskapDB> list = new ArrayList<>();
        list.add(DB_REGNSKAP_2017);
        list.add(DB_REGNSKAP_2018_SECOND);
        list.add(DB_REGNSKAP_2018_FIRST);
        return list;
    }

    public static List<RegnskapLog> DB_LOG = createLog();
    public static Sort DB_SORT = Sort.by(Sort.Direction.ASC, "filename");

    private static List<RegnskapLog> createLog() {
        List<RegnskapLog> list = new ArrayList<>();
        list.add(createLogEntry("file0.xml"));
        list.add(createLogEntry("file1.xml"));
        list.add(createLogEntry("file2.xml"));
        list.add(createLogEntry("file3.xml"));
        return list;
    }

    private static RegnskapLog createLogEntry(String filename) {
        RegnskapLog entry = new RegnskapLog();
        entry.setFilename(filename);
        return entry;
    }

    private static Eiendeler eiendelerWithValues(long baseValue) {
        return new Eiendeler()
            .sumEiendeler(BigDecimal.valueOf(baseValue))
            .anleggsmidler(new Anleggsmidler().sumAnleggsmidler(BigDecimal.valueOf(baseValue + 1)))
            .omloepsmidler(new Omloepsmidler().sumOmloepsmidler(BigDecimal.valueOf(baseValue + 2)));
    }

    private static EgenkapitalGjeld egenkapitalGjeldWithValues(long baseValue) {
        return new EgenkapitalGjeld()
            .sumEgenkapitalGjeld(BigDecimal.valueOf(baseValue + 3))
            .egenkapital(
                new Egenkapital()
                    .sumEgenkapital(BigDecimal.valueOf(baseValue + 4))
                    .innskuttEgenkapital(new InnskuttEgenkapital().sumInnskuttEgenkaptial(BigDecimal.valueOf(baseValue + 5)))
                    .opptjentEgenkapital(new OpptjentEgenkapital().sumOpptjentEgenkapital(BigDecimal.valueOf(baseValue + 6))))
            .gjeldOversikt(
                new Gjeld()
                    .sumGjeld(BigDecimal.valueOf(baseValue + 7))
                    .kortsiktigGjeld(new KortsiktigGjeld().sumKortsiktigGjeld(BigDecimal.valueOf(baseValue + 8)))
                    .langsiktigGjeld(new LangsiktigGjeld().sumLangsiktigGjeld(BigDecimal.valueOf(baseValue + 9))));
    }

    private static ResultatregnskapResultat resultatregnskapResultatWithValues(long baseValue) {
        return new ResultatregnskapResultat()
            .ordinaertResultatFoerSkattekostnad(BigDecimal.valueOf(baseValue + 10))
            .aarsresultat(BigDecimal.valueOf(baseValue + 11))
            .totalresultat(BigDecimal.valueOf(baseValue + 12))
            .driftsresultat(
                new Driftsresultat()
                    .driftsresultat(BigDecimal.valueOf(baseValue + 13))
                    .driftsinntekter(new Driftsinntekter().sumDriftsinntekter(BigDecimal.valueOf(baseValue + 14)))
                    .driftskostnad(new Driftskostnad().annenDriftskostnad(BigDecimal.valueOf(baseValue + 15))
                                                      .sumDriftskostnad(BigDecimal.valueOf(baseValue + 16))))
            .finansresultat(
                new Finansresultat()
                    .nettoFinans(BigDecimal.valueOf(baseValue + 17))
                    .finansinntekt(new Finansinntekt().sumFinansinntekter(BigDecimal.valueOf(baseValue + 18)))
                    .finanskostnad(new Finanskostnad().sumFinanskostnad(BigDecimal.valueOf(baseValue + 19))));
    }
}