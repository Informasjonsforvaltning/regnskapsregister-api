package no.regnskap.mapper

import no.regnskap.generated.model.Regnskap
import no.regnskap.generated.model.Regnskapsprinsipper
import no.regnskap.generated.model.Revisjon
import no.regnskap.generated.model.Tidsperiode
import no.regnskap.generated.model.Virksomhet
import no.regnskap.model.RegnskapDB
import no.regnskap.model.RegnskapFieldsDB
import no.regnskap.model.RegnskapXmlHead
import no.regnskap.model.RegnskapXmlWrap

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val XML_TRUE_STRING = "J"
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

fun RegnskapXmlWrap.mapXmlListForPersistence(): List<RegnskapDB> {
    val toPersist: MutableMap<Int, RegnskapDB> = HashMap()

    list.forEach {
        if(it.head != null) {
            val key = it.head.hashCode()
            val mapped = toPersist.getOrDefault(key, it.head.createRegnskapDB())

            mapped.fields = mapFieldsFromXmlData(mapped.fields, it.posts)
            toPersist[key] = mapped
        }
    }

    return ArrayList(toPersist.values)
}

private fun RegnskapXmlHead.createRegnskapDB(): RegnskapDB {
    val regnskapDB = RegnskapDB()

    regnskapDB.orgnr = orgnr
    regnskapDB.aarsregnskapstype = aarsregnskapstype
    regnskapDB.journalnr = journalnr
    regnskapDB.mottakstype = mottakstype
    regnskapDB.oppstillingsplanVersjonsnr = oppstillingsplanVersjonsnr
    regnskapDB.orgform = orgform
    regnskapDB.regnaar = regnaar
    regnskapDB.regnskapstype = regnskapstype
    regnskapDB.valutakode = valutakode

    regnskapDB.avviklingsregnskap = booleanFromXmlData(avviklingsregnskap)
    regnskapDB.bistandRegnskapsforer = booleanFromXmlData(bistandRegnskapsforer)
    regnskapDB.feilvaloer = booleanFromXmlData(feilvaloer)
    regnskapDB.fleksiblePoster = booleanFromXmlData(fleksiblePoster)
    regnskapDB.fravalgRevisjon = booleanFromXmlData(fravalgRevisjon)
    regnskapDB.landForLand = booleanFromXmlData(landForLand)
    regnskapDB.morselskap = booleanFromXmlData(morselskap)
    regnskapDB.reglerSmaa = booleanFromXmlData(reglerSmaa)
    regnskapDB.utarbeidetRegnskapsforer = booleanFromXmlData(utarbeidetRegnskapsforer)
    regnskapDB.revisorberetningIkkeLevert = booleanFromXmlData(revisorberetningIkkeLevert)
    regnskapDB.ifrsSelskap = booleanFromXmlData(ifrsSelskap)
    regnskapDB.forenkletIfrsSelskap = booleanFromXmlData(forenkletIfrsSelskap)
    regnskapDB.ifrsKonsern = booleanFromXmlData(ifrsKonsern)
    regnskapDB.forenkletIfrsKonsern = booleanFromXmlData(forenkletIfrsKonsern)

    regnskapDB.avslutningsdato = avslutningsdato?.localDateFromXmlDateString()
    regnskapDB.startdato = startdato?.localDateFromXmlDateString()

    regnskapDB.fields = RegnskapFieldsDB()

    return regnskapDB
}


private fun booleanFromXmlData(trueOrFalse: String?): Boolean =
    XML_TRUE_STRING == trueOrFalse

fun RegnskapDB.mapPersistenceToGenerated(): Regnskap =
    Regnskap()
        .id(id?.toHexString())
        .avviklingsregnskap(avviklingsregnskap)
        .valuta(valutakode)
        .oppstillingsplan(
            when(aarsregnskapstype.toLowerCase()){
                "store" -> Regnskap.OppstillingsplanEnum.STORE
                "smaa" -> Regnskap.OppstillingsplanEnum.SMAA
                else -> Regnskap.OppstillingsplanEnum.OEVRIGE
            })
        .revisjon(
            Revisjon()
                .ikkeRevidertAarsregnskap(revisorberetningIkkeLevert))
        .regnskapsperiode(
            Tidsperiode()
                .fraDato(startdato)
                .tilDato(avslutningsdato))
        .regnkapsprinsipper(
            Regnskapsprinsipper()
                .smaaForetak(reglerSmaa)
                .regnskapsregler(
                    when {
                        ifrsSelskap -> Regnskapsprinsipper.RegnskapsreglerEnum.IFRS
                        forenkletIfrsSelskap -> Regnskapsprinsipper.RegnskapsreglerEnum.FORENKLETANVENDELSEIFRS
                        else -> Regnskapsprinsipper.RegnskapsreglerEnum.REGNSKAPSLOVENALMINNELIGREGLER
                    }
                ))
        .virksomhet(
            Virksomhet()
                .organisasjonsnummer(orgnr)
                .organisasjonsform(orgform)
                .morselskap(morselskap))
        .egenkapitalGjeld(fields.egenkapitalGjeld)
        .eiendeler(fields.eiendeler)
        .resultatregnskapResultat(fields.resultatregnskapResultat)

private fun String.localDateFromXmlDateString(): LocalDate =
    LocalDate.parse(this, dateTimeFormatter)

fun RegnskapDB.essentialFieldsIncluded() =
    orgnr != null && avslutningsdato != null && startdato != null && journalnr != null