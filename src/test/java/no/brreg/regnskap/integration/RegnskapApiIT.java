package no.brreg.regnskap.integration;

import no.brreg.regnskap.JenaResponseReader;
import no.brreg.regnskap.TestData;
import no.brreg.regnskap.TestUtils;
import no.brreg.regnskap.XmlTestData;
import no.brreg.regnskap.controller.RegnskapApiImpl;
import no.brreg.regnskap.generated.model.Regnskap;
import no.brreg.regnskap.generated.model.Regnskapsprinsipper;
import no.brreg.regnskap.generated.model.Regnskapstype;
import no.brreg.regnskap.repository.ConnectionManager;
import no.brreg.regnskap.repository.RegnskapLogRepository;
import no.brreg.regnskap.repository.RegnskapRepository;
import no.brreg.regnskap.utils.EmbeddedPostgresSetup;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class RegnskapApiIT extends EmbeddedPostgresSetup {
    private final static Logger LOGGER = LoggerFactory.getLogger(RegnskapApiIT.class);

    final static String TESTDATA_FILENAME = "xmlTestString";

    @Autowired
    private RegnskapApiImpl regnskapApiImpl;

    @Autowired
    private RegnskapLogRepository regnskapLogRepository;
    private static boolean hasImportedTestdata = false;
    private static Integer regnskap2016Id;
    private static Integer regnskap2017Id;
    private static Integer regnskap2018_1Id;
    private static Integer regnskap2018_2Id;
    private static Integer regnskap2018_3Id;
    private static Integer regnskap2019_1Id;
    private static Integer regnskap2019_2Id;

    private static Integer regnskapId1;
    private static Integer regnskapId2;

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

        InputStream testdataIS = new ByteArrayInputStream(XmlTestData.xmlTestString.getBytes(StandardCharsets.UTF_8));
        try {
            regnskapLogRepository.persistRegnskapFile(TESTDATA_FILENAME, testdataIS);
        } catch (SQLException e) {
            LOGGER.info("Regnskap file test data already loaded");
        }


        regnskap2016Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2016S);
        regnskap2017Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2017S);
        regnskap2018_1Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_1S);
        regnskap2018_2Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_2S);
        regnskap2018_3Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2018_3K);
        regnskap2019_1Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2019_1S);
        regnskap2019_2Id = regnskapRepository.persistRegnskap(TestData.REGNSKAP_2019_2K);

        //Add partner
        Connection connection = connectionManager.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO rregapi.partners (name,key) VALUES ('test','test')")) {
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.info("Partner test data already loaded");
        }
    }

    @Test
    public void regnskapsreglerEnumTest() {
        Regnskapsprinsipper.RegnskapsreglerEnum regler = TestData.REGNSKAP_2018_1S.getRegnkapsprinsipper().getRegnskapsregler();
        assertTrue(regler == Regnskapsprinsipper.RegnskapsreglerEnum.REGNSKAPSLOVENALMINNELIGREGLER);
        assertFalse(regler == Regnskapsprinsipper.RegnskapsreglerEnum.FORENKLETANVENDELSEIFRS);
        assertFalse(regler == Regnskapsprinsipper.RegnskapsreglerEnum.IFRS);
    }

    @Test
    public void getRegnskapTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        final String orgNummer = TestData.TEST_ORGNR_1;
        //Get most recent SELSKAP regnskap for TestData.TEST_ORGNR_1
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, orgNummer, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(orgNummer, body.get(0).getVirksomhet().getOrganisasjonsnummer());
        assertEquals(2019, body.get(0).getRegnskapsperiode().getFraDato().getYear());
        assertEquals(Regnskapstype.SELSKAP, body.get(0).getRegnskapstype());
    }

    @Test
    public void getRegnskapPartnerTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        final String orgNummer = TestData.TEST_ORGNR_1;
        //Get most recent regnskap of any type for three most recent years for TestData.TEST_ORGNR_1
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, orgNummer, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        assertEquals(5, body.size());
        int foundBitmask = 0;
        for (Regnskap regnskap : body) {
            assertEquals(orgNummer, regnskap.getVirksomhet().getOrganisasjonsnummer());
            if (regnskap.getRegnskapsperiode().getFraDato().getYear()==2017 && regnskap.getRegnskapstype()==Regnskapstype.SELSKAP) {
                foundBitmask |= 1<<0;
            } else if (regnskap.getRegnskapsperiode().getFraDato().getYear()==2018 && regnskap.getRegnskapstype()==Regnskapstype.SELSKAP) {
                foundBitmask |= 1<<1;
            } else if (regnskap.getRegnskapsperiode().getFraDato().getYear()==2018 && regnskap.getRegnskapstype()==Regnskapstype.KONSERN) {
                foundBitmask |= 1<<2;
            } else if (regnskap.getRegnskapsperiode().getFraDato().getYear()==2019 && regnskap.getRegnskapstype()==Regnskapstype.SELSKAP) {
                foundBitmask |= 1<<3;
            } else if (regnskap.getRegnskapsperiode().getFraDato().getYear()==2019 && regnskap.getRegnskapstype()==Regnskapstype.KONSERN) {
                foundBitmask |= 1<<4;
            }
        }
        assertEquals(0b11111, foundBitmask);
    }

    @Test
    public void getRegnskapInvalidPartnerTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic d3Jvbmc6cGFzc3dvcmQ="); // "Basic wrong:password"
        final String orgNummer = TestData.TEST_ORGNR_1;
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, orgNummer, null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        assertNull(body);
    }

    @Test
    public void getRegnskapPartner2018SelskapTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        final String orgNummer = TestData.TEST_ORGNR_1;
        final int år = 2018;
        final Regnskapstype regnskapstype = Regnskapstype.SELSKAP;
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, orgNummer, år, regnskapstype);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        for (Regnskap regnskap : body) {
            assertEquals(orgNummer, regnskap.getVirksomhet().getOrganisasjonsnummer());
            assertTrue(TestUtils.forYear(regnskap.getRegnskapsperiode(), år));
        }
    }

    @Test
    public void getRegnskapPartner2018SelskapInvalidÅrTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        final String orgNummer = TestData.TEST_ORGNR_1;
        final int år = Integer.parseInt(TestData.TEST_ORGNR_1);
        final Regnskapstype regnskapstype = Regnskapstype.SELSKAP;
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, orgNummer, år, regnskapstype);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void getRegnskapAcceptHeaderTest() {
        final String[] acceptHeaders = {"application/json", "application/xml", "application/ld+json",
                                        "application/rdf+json", "application/rdf+xml", "text/turtle"};
        for (String acceptHeader : acceptHeaders) {
            Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn(acceptHeader);
            ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, TestData.TEST_ORGNR_1, 2018, Regnskapstype.SELSKAP);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    public void getRegnskapByIdTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        final String orgNummer = "980919676";
        final Integer id = 1;
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskapById(httpServletRequestMock, orgNummer, id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Regnskap body = (Regnskap) response.getBody();
        assertEquals(id, body.getId());
    }

    @Test
    public void getRegnskapByIdPartnerTest() {
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        final String orgNummer = "980919676";
        final Integer id = 1;
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskapById(httpServletRequestMock, orgNummer, id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Regnskap body = (Regnskap) response.getBody();
        assertEquals(id, body.getId());
    }

    @Test
    public void getRegnskapByIdDefaultTurtleTest() throws IOException, SQLException {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("text/turtle");
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskapById(httpServletRequestMock, "123456789", regnskap2018_1Id);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Model modelFromResponse = JenaResponseReader.parseResponse((String)response.getBody(), "TURTLE");
        Map<String,String> patches = new HashMap<>();
        patches.put("<identifier>", regnskap2018_1Id.toString());
        Model expectedResponse = JenaResponseReader.getExpectedResponse("OrgnrResponseDefault.ttl", patches, "TURTLE");

        boolean isIsomorphicWith = expectedResponse.isIsomorphicWith(modelFromResponse);
        if (!isIsomorphicWith) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                modelFromResponse.write(baos, "TURTLE");
                String s = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                LOGGER.info("Response differ. Got:\n" + s);
            }
        }

        assertTrue(isIsomorphicWith);
    }

    @Test
    public void getRegnskapByIdPartnerTurtleTest() throws IOException, SQLException {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("text/turtle");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskapById(httpServletRequestMock, "123456789", regnskap2018_1Id);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Model modelFromResponse = JenaResponseReader.parseResponse((String)response.getBody(), "TURTLE");
        Map<String,String> patches = new HashMap<>();
        patches.put("<identifier>", regnskap2018_1Id.toString());
        Model expectedResponse = JenaResponseReader.getExpectedResponse("OrgnrResponsePartner.ttl", patches, "TURTLE");

        boolean isIsomorphicWith = expectedResponse.isIsomorphicWith(modelFromResponse);
        if (!isIsomorphicWith) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                modelFromResponse.write(baos, "TURTLE");
                String s = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                LOGGER.info("Response differ. Got:\n" + s);
            }
        }

        assertTrue(isIsomorphicWith);
    }

    @Test
    void correctSumsInPersistenceFieldsDefault() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskapById(httpServletRequestMock, "123456789", regnskap2018_1Id);
        Regnskap regnskap = (Regnskap) response.getBody();

        long baseValue = 2018L;
        assertNull(regnskap.getEiendeler().getGoodwill());
        assertNull(regnskap.getEiendeler().getSumVarer());
        assertNull(regnskap.getEiendeler().getSumFordringer());
        assertNull(regnskap.getEiendeler().getSumInvesteringer());
        assertNull(regnskap.getEiendeler().getSumBankinnskuddOgKontanter());
        assertEquals(baseValue + 5, regnskap.getEiendeler().getSumEiendeler().intValue());
        assertEquals(baseValue + 6, regnskap.getEiendeler().getAnleggsmidler().getSumAnleggsmidler().intValue());
        assertEquals(baseValue + 7, regnskap.getEiendeler().getOmloepsmidler().getSumOmloepsmidler().intValue());
        assertEquals(baseValue + 8, regnskap.getEgenkapitalGjeld().getSumEgenkapitalGjeld().intValue());
        assertEquals(baseValue + 9, regnskap.getEgenkapitalGjeld().getEgenkapital().getSumEgenkapital().intValue());
        assertEquals(baseValue + 10, regnskap.getEgenkapitalGjeld().getEgenkapital().getInnskuttEgenkapital().getSumInnskuttEgenkaptial().intValue());
        assertEquals(baseValue + 11, regnskap.getEgenkapitalGjeld().getEgenkapital().getOpptjentEgenkapital().getSumOpptjentEgenkapital().intValue());
        assertEquals(baseValue + 12, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getSumGjeld().intValue());
        assertEquals(baseValue + 13, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getKortsiktigGjeld().getSumKortsiktigGjeld().intValue());
        assertEquals(baseValue + 14, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getLangsiktigGjeld().getSumLangsiktigGjeld().intValue());
        assertEquals(baseValue + 15, regnskap.getResultatregnskapResultat().getOrdinaertResultatFoerSkattekostnad().intValue());
        assertNull(regnskap.getResultatregnskapResultat().getOrdinaertResultatSkattekostnad());
        assertNull(regnskap.getResultatregnskapResultat().getEkstraordinaerePoster());
        assertNull(regnskap.getResultatregnskapResultat().getSkattekostnadEkstraordinaertResultat());
        assertEquals(baseValue + 19, regnskap.getResultatregnskapResultat().getAarsresultat().intValue());
        assertEquals(baseValue + 20, regnskap.getResultatregnskapResultat().getTotalresultat().intValue());
        assertEquals(baseValue + 21, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsresultat().intValue());
        assertNull(regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSalgsinntekter());
        assertEquals(baseValue + 23, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSumDriftsinntekter().intValue());
        assertNull(regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getLoennskostnad());
        assertEquals(baseValue + 25, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getSumDriftskostnad().intValue());
        assertEquals(baseValue + 26, regnskap.getResultatregnskapResultat().getFinansresultat().getNettoFinans().intValue());
        assertEquals(baseValue + 27, regnskap.getResultatregnskapResultat().getFinansresultat().getFinansinntekt().getSumFinansinntekter().intValue());
        assertNull(regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getRentekostnadSammeKonsern());
        assertNull(regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getAnnenRentekostnad());
        assertEquals(baseValue + 30, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getSumFinanskostnad().intValue());
    }

    @Test
    void correctSumsInPersistenceFieldsPartnerSelskap() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, "123456789", 2019, Regnskapstype.SELSKAP);
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        assertEquals(1, body.size());
        Regnskap regnskap = body.get(0);

        long baseValue = 2019L;
        assertEquals(baseValue, regnskap.getEiendeler().getGoodwill().intValue());
        assertEquals(TestData.TEST_SELSKAP_VARER, regnskap.getEiendeler().getSumVarer().intValue());
        assertEquals(TestData.TEST_SELSKAP_FORDRINGER, regnskap.getEiendeler().getSumFordringer().intValue());
        assertNull(regnskap.getEiendeler().getSumInvesteringer());
        assertEquals(baseValue + 4, regnskap.getEiendeler().getSumBankinnskuddOgKontanter().intValue());
        assertEquals(baseValue + 5, regnskap.getEiendeler().getSumEiendeler().intValue());
        assertEquals(baseValue + 6, regnskap.getEiendeler().getAnleggsmidler().getSumAnleggsmidler().intValue());
        assertEquals(baseValue + 7, regnskap.getEiendeler().getOmloepsmidler().getSumOmloepsmidler().intValue());
        assertEquals(baseValue + 8, regnskap.getEgenkapitalGjeld().getSumEgenkapitalGjeld().intValue());
        assertEquals(baseValue + 9, regnskap.getEgenkapitalGjeld().getEgenkapital().getSumEgenkapital().intValue());
        assertEquals(baseValue + 10, regnskap.getEgenkapitalGjeld().getEgenkapital().getInnskuttEgenkapital().getSumInnskuttEgenkaptial().intValue());
        assertEquals(baseValue + 11, regnskap.getEgenkapitalGjeld().getEgenkapital().getOpptjentEgenkapital().getSumOpptjentEgenkapital().intValue());
        assertEquals(baseValue + 12, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getSumGjeld().intValue());
        assertEquals(baseValue + 13, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getKortsiktigGjeld().getSumKortsiktigGjeld().intValue());
        assertEquals(baseValue + 14, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getLangsiktigGjeld().getSumLangsiktigGjeld().intValue());
        assertEquals(baseValue + 15, regnskap.getResultatregnskapResultat().getOrdinaertResultatFoerSkattekostnad().intValue());
        assertEquals(baseValue + 16, regnskap.getResultatregnskapResultat().getOrdinaertResultatSkattekostnad().intValue());
        assertEquals(baseValue + 17, regnskap.getResultatregnskapResultat().getEkstraordinaerePoster().intValue());
        assertEquals(baseValue + 18, regnskap.getResultatregnskapResultat().getSkattekostnadEkstraordinaertResultat().intValue());
        assertEquals(baseValue + 19, regnskap.getResultatregnskapResultat().getAarsresultat().intValue());
        assertEquals(baseValue + 25, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getSumDriftskostnad().intValue());
        assertEquals(baseValue + 26, regnskap.getResultatregnskapResultat().getFinansresultat().getNettoFinans().intValue());
        assertEquals(baseValue + 27, regnskap.getResultatregnskapResultat().getFinansresultat().getFinansinntekt().getSumFinansinntekter().intValue());
        assertEquals(baseValue + 28, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getRentekostnadSammeKonsern().intValue());
        assertEquals(baseValue + 29, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getAnnenRentekostnad().intValue());
        assertEquals(baseValue + 30, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getSumFinanskostnad().intValue());
    }

    @Test
    void correctSumsInPersistenceFieldsPartnerKonsern() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/xml");
        Mockito.when(httpServletRequestMock.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // "Basic test:test"
        ResponseEntity<Object> response = regnskapApiImpl.getRegnskap(httpServletRequestMock, "123456789", 2019, Regnskapstype.KONSERN);
        List<Regnskap> body = (List<Regnskap>) response.getBody();
        assertEquals(1, body.size());
        Regnskap regnskap = body.get(0);

        long baseValue = 2019L;
        assertEquals(baseValue, regnskap.getEiendeler().getGoodwill().intValue());
        assertEquals(TestData.TEST_KONSERN_VARER, regnskap.getEiendeler().getSumVarer().intValue());
        assertNull(regnskap.getEiendeler().getSumFordringer());
        assertEquals(TestData.TEST_KONSERN_INVESTERINGER, regnskap.getEiendeler().getSumInvesteringer().intValue());
        assertEquals(baseValue + 4, regnskap.getEiendeler().getSumBankinnskuddOgKontanter().intValue());
        assertEquals(baseValue + 5, regnskap.getEiendeler().getSumEiendeler().intValue());
        assertEquals(baseValue + 6, regnskap.getEiendeler().getAnleggsmidler().getSumAnleggsmidler().intValue());
        assertEquals(baseValue + 7, regnskap.getEiendeler().getOmloepsmidler().getSumOmloepsmidler().intValue());
        assertEquals(baseValue + 8, regnskap.getEgenkapitalGjeld().getSumEgenkapitalGjeld().intValue());
        assertEquals(baseValue + 9, regnskap.getEgenkapitalGjeld().getEgenkapital().getSumEgenkapital().intValue());
        assertEquals(baseValue + 10, regnskap.getEgenkapitalGjeld().getEgenkapital().getInnskuttEgenkapital().getSumInnskuttEgenkaptial().intValue());
        assertEquals(baseValue + 11, regnskap.getEgenkapitalGjeld().getEgenkapital().getOpptjentEgenkapital().getSumOpptjentEgenkapital().intValue());
        assertEquals(baseValue + 12, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getSumGjeld().intValue());
        assertEquals(baseValue + 13, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getKortsiktigGjeld().getSumKortsiktigGjeld().intValue());
        assertEquals(baseValue + 14, regnskap.getEgenkapitalGjeld().getGjeldOversikt().getLangsiktigGjeld().getSumLangsiktigGjeld().intValue());
        assertEquals(baseValue + 15, regnskap.getResultatregnskapResultat().getOrdinaertResultatFoerSkattekostnad().intValue());
        assertEquals(baseValue + 16, regnskap.getResultatregnskapResultat().getOrdinaertResultatSkattekostnad().intValue());
        assertEquals(baseValue + 17, regnskap.getResultatregnskapResultat().getEkstraordinaerePoster().intValue());
        assertEquals(baseValue + 18, regnskap.getResultatregnskapResultat().getSkattekostnadEkstraordinaertResultat().intValue());
        assertEquals(baseValue + 19, regnskap.getResultatregnskapResultat().getAarsresultat().intValue());
        assertEquals(baseValue + 20, regnskap.getResultatregnskapResultat().getTotalresultat().intValue());
        assertEquals(baseValue + 21, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsresultat().intValue());
        assertEquals(baseValue + 22, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSalgsinntekter().intValue());
        assertEquals(baseValue + 23, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSumDriftsinntekter().intValue());
        assertEquals(baseValue + 24, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getLoennskostnad().intValue());
        assertEquals(baseValue + 25, regnskap.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getSumDriftskostnad().intValue());
        assertEquals(baseValue + 26, regnskap.getResultatregnskapResultat().getFinansresultat().getNettoFinans().intValue());
        assertEquals(baseValue + 27, regnskap.getResultatregnskapResultat().getFinansresultat().getFinansinntekt().getSumFinansinntekter().intValue());
        assertEquals(baseValue + 28, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getRentekostnadSammeKonsern().intValue());
        assertEquals(baseValue + 29, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getAnnenRentekostnad().intValue());
        assertEquals(baseValue + 30, regnskap.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getSumFinanskostnad().intValue());
    }

    @Test
    public void getLogTest() {
        ResponseEntity<List<String>> response = regnskapApiImpl.getLog(httpServletRequestMock);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> body = response.getBody();
        assertTrue(body.contains(TESTDATA_FILENAME));
    }

}
