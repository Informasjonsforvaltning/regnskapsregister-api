package no.regnskap.mapper;

import no.regnskap.XmlTestData;
import no.regnskap.generated.model.Regnskap;
import no.regnskap.generated.model.Regnskapsprinsipper;
import no.regnskap.model.RegnskapDB;
import no.regnskap.model.RegnskapFieldsDB;
import no.regnskap.model.RegnskapXmlWrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static no.regnskap.mapper.RegnskapMapperKt.essentialFieldsIncluded;
import static no.regnskap.mapper.RegnskapXmlMapperKt.deserializeXmlString;
import static no.regnskap.mapper.RegnskapMapperKt.mapXmlListForPersistence;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@Tag("unit")
class MapperTest {

    private static RegnskapXmlWrap xmlWrap;
    private static List<RegnskapDB> listRegnskapDB;
    private static List<Regnskap> listRegnskap;

    @BeforeAll
    static void mappingFromXmlToResponse() {
        xmlWrap = deserializeXmlString(XmlTestData.xmlTestString);
        listRegnskapDB = mapXmlListForPersistence(xmlWrap);

        listRegnskap = listRegnskapDB
            .stream()
            .map(RegnskapMapperKt::mapPersistenceToGenerated)
            .collect(Collectors.toList());
    }

    @Test
    void allFieldsAreMapped() {
        assertEquals(44, xmlWrap.getList().get(0).getPosts().size());
        assertEquals(60, xmlWrap.getList().get(1).getPosts().size());
        assertEquals(40, xmlWrap.getList().get(2).getPosts().size());
        assertEquals(45, xmlWrap.getList().get(3).getPosts().size());
    }

    @Test
    void equalOrgnrAndRegnaarIsMerged() {
        assertEquals(4, xmlWrap.getList().size());
        assertEquals(xmlWrap.getList().get(0).getHead().getOrgnr(),xmlWrap.getList().get(1).getHead().getOrgnr() );
        assertEquals(xmlWrap.getList().get(2).getHead().getOrgnr(),xmlWrap.getList().get(3).getHead().getOrgnr() );
        assertNotEquals(xmlWrap.getList().get(0).getHead().getOrgnr(),xmlWrap.getList().get(2).getHead().getOrgnr() );
        assertEquals(xmlWrap.getList().get(0).getHead().getRegnaar(),xmlWrap.getList().get(1).getHead().getRegnaar() );
        assertEquals(xmlWrap.getList().get(2).getHead().getRegnaar(),xmlWrap.getList().get(3).getHead().getRegnaar() );
        assertEquals(xmlWrap.getList().get(1).getHead().getRegnaar(),xmlWrap.getList().get(3).getHead().getRegnaar() );
        assertEquals(2, listRegnskapDB.size());
    }

    @Test
    void correctSumsInPersistenceFields() {
        RegnskapFieldsDB fieldsAlpha = listRegnskapDB.get(0).getFields();
        RegnskapFieldsDB fieldsBravo = listRegnskapDB.get(1).getFields();

        assertEquals(XmlTestData.ALPHA_FIELD_219, fieldsAlpha.getEiendeler().getSumEiendeler().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_217, fieldsAlpha.getEiendeler().getAnleggsmidler().getSumAnleggsmidler().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_194, fieldsAlpha.getEiendeler().getOmloepsmidler().getSumOmloepsmidler().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_251, fieldsAlpha.getEgenkapitalGjeld().getSumEgenkapitalGjeld().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_250, fieldsAlpha.getEgenkapitalGjeld().getEgenkapital().getSumEgenkapital().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_3730, fieldsAlpha.getEgenkapitalGjeld().getEgenkapital().getInnskuttEgenkapital().getSumInnskuttEgenkaptial().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_9702, fieldsAlpha.getEgenkapitalGjeld().getEgenkapital().getOpptjentEgenkapital().getSumOpptjentEgenkapital().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_1119, fieldsAlpha.getEgenkapitalGjeld().getGjeldOversikt().getSumGjeld().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_86, fieldsAlpha.getEgenkapitalGjeld().getGjeldOversikt().getLangsiktigGjeld().getSumLangsiktigGjeld().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_85, fieldsAlpha.getEgenkapitalGjeld().getGjeldOversikt().getKortsiktigGjeld().getSumKortsiktigGjeld().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_172, fieldsAlpha.getResultatregnskapResultat().getAarsresultat().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_36633, fieldsAlpha.getResultatregnskapResultat().getTotalresultat().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_167, fieldsAlpha.getResultatregnskapResultat().getOrdinaertResultatFoerSkattekostnad().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_146, fieldsAlpha.getResultatregnskapResultat().getDriftsresultat().getDriftsresultat().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_72, fieldsAlpha.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSumDriftsinntekter().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_82, fieldsAlpha.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getAnnenDriftskostnad().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_17126, fieldsAlpha.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getSumDriftskostnad().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_158, fieldsAlpha.getResultatregnskapResultat().getFinansresultat().getNettoFinans().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_153, fieldsAlpha.getResultatregnskapResultat().getFinansresultat().getFinansinntekt().getSumFinansinntekter().intValue());
        assertEquals(XmlTestData.ALPHA_FIELD_17130, fieldsAlpha.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getSumFinanskostnad().intValue());

        assertEquals(XmlTestData.BRAVO_FIELD_219, fieldsBravo.getEiendeler().getSumEiendeler().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_217, fieldsBravo.getEiendeler().getAnleggsmidler().getSumAnleggsmidler().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_194, fieldsBravo.getEiendeler().getOmloepsmidler().getSumOmloepsmidler().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_251, fieldsBravo.getEgenkapitalGjeld().getSumEgenkapitalGjeld().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_250, fieldsBravo.getEgenkapitalGjeld().getEgenkapital().getSumEgenkapital().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_3730, fieldsBravo.getEgenkapitalGjeld().getEgenkapital().getInnskuttEgenkapital().getSumInnskuttEgenkaptial().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_9702, fieldsBravo.getEgenkapitalGjeld().getEgenkapital().getOpptjentEgenkapital().getSumOpptjentEgenkapital().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_1119, fieldsBravo.getEgenkapitalGjeld().getGjeldOversikt().getSumGjeld().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_86, fieldsBravo.getEgenkapitalGjeld().getGjeldOversikt().getLangsiktigGjeld().getSumLangsiktigGjeld().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_85, fieldsBravo.getEgenkapitalGjeld().getGjeldOversikt().getKortsiktigGjeld().getSumKortsiktigGjeld().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_172, fieldsBravo.getResultatregnskapResultat().getAarsresultat().intValue());
        assertNull(fieldsBravo.getResultatregnskapResultat().getTotalresultat());
        assertEquals(XmlTestData.BRAVO_FIELD_167, fieldsBravo.getResultatregnskapResultat().getOrdinaertResultatFoerSkattekostnad().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_146, fieldsBravo.getResultatregnskapResultat().getDriftsresultat().getDriftsresultat().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_72, fieldsBravo.getResultatregnskapResultat().getDriftsresultat().getDriftsinntekter().getSumDriftsinntekter().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_82, fieldsBravo.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getAnnenDriftskostnad().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_17126, fieldsBravo.getResultatregnskapResultat().getDriftsresultat().getDriftskostnad().getSumDriftskostnad().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_158, fieldsBravo.getResultatregnskapResultat().getFinansresultat().getNettoFinans().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_153, fieldsBravo.getResultatregnskapResultat().getFinansresultat().getFinansinntekt().getSumFinansinntekter().intValue());
        assertEquals(XmlTestData.BRAVO_FIELD_17130, fieldsBravo.getResultatregnskapResultat().getFinansresultat().getFinanskostnad().getSumFinanskostnad().intValue());
    }

    @Test
    void correctMappingFromPersistence() {
        RegnskapDB dbAlpha = listRegnskapDB.get(0);
        RegnskapDB dbBravo = listRegnskapDB.get(1);

        Regnskap alpha = listRegnskap.get(0);
        Regnskap bravo = listRegnskap.get(1);

        assertEquals(dbAlpha.getAvviklingsregnskap(), alpha.getAvviklingsregnskap());
        assertEquals(dbAlpha.getValutakode(), alpha.getValuta());
        assertEquals(dbAlpha.getAarsregnskapstype().toLowerCase(), alpha.getOppstillingsplan().getValue());
        assertEquals(dbAlpha.getRevisorberetningIkkeLevert(), alpha.getAvviklingsregnskap());
        assertEquals(dbAlpha.getStartdato(), alpha.getRegnskapsperiode().getFraDato());
        assertEquals(dbAlpha.getAvslutningsdato(), alpha.getRegnskapsperiode().getTilDato());
        assertEquals(dbAlpha.getReglerSmaa(), alpha.getRegnkapsprinsipper().getSmaaForetak());
        assertEquals(Regnskapsprinsipper.RegnskapsreglerEnum.IFRS, alpha.getRegnkapsprinsipper().getRegnskapsregler());
        assertEquals(dbAlpha.getOrgnr(), alpha.getVirksomhet().getOrganisasjonsnummer());
        assertEquals(dbAlpha.getOrgform(), alpha.getVirksomhet().getOrganisasjonsform());
        assertEquals(dbAlpha.getMorselskap(), alpha.getVirksomhet().getMorselskap());
        assertEquals(dbAlpha.getAvviklingsregnskap(), alpha.getAvviklingsregnskap());

        assertEquals(dbBravo.getAvviklingsregnskap(), bravo.getAvviklingsregnskap());
        assertEquals(dbBravo.getValutakode(), bravo.getValuta());
        assertEquals(dbBravo.getAarsregnskapstype().toLowerCase(), bravo.getOppstillingsplan().getValue());
        assertEquals(dbBravo.getRevisorberetningIkkeLevert(), bravo.getAvviklingsregnskap());
        assertEquals(dbBravo.getStartdato(), bravo.getRegnskapsperiode().getFraDato());
        assertEquals(dbBravo.getAvslutningsdato(), bravo.getRegnskapsperiode().getTilDato());
        assertEquals(dbBravo.getReglerSmaa(), bravo.getRegnkapsprinsipper().getSmaaForetak());
        assertEquals(Regnskapsprinsipper.RegnskapsreglerEnum.REGNSKAPSLOVENALMINNELIGREGLER, bravo.getRegnkapsprinsipper().getRegnskapsregler());
        assertEquals(dbBravo.getOrgnr(), bravo.getVirksomhet().getOrganisasjonsnummer());
        assertEquals(dbBravo.getOrgform(), bravo.getVirksomhet().getOrganisasjonsform());
        assertEquals(dbBravo.getMorselskap(), bravo.getVirksomhet().getMorselskap());
        assertEquals(dbBravo.getAvviklingsregnskap(), bravo.getAvviklingsregnskap());
    }

    @Test
    void essentialFieldsTest() {
        RegnskapDB regnskapDB = new RegnskapDB();
        assertFalse(essentialFieldsIncluded(regnskapDB));

        regnskapDB.setOrgnr("orgnr");
        assertFalse(essentialFieldsIncluded(regnskapDB));

        regnskapDB.setAvslutningsdato(LocalDate.now());
        assertFalse(essentialFieldsIncluded(regnskapDB));

        regnskapDB.setStartdato(LocalDate.now());
        assertFalse(essentialFieldsIncluded(regnskapDB));

        regnskapDB.setJournalnr("journalnr");
        assertTrue(essentialFieldsIncluded(regnskapDB));
    }
}
