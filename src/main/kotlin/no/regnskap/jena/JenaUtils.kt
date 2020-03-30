package no.regnskap.jena

import no.regnskap.generated.model.Regnskap
import org.apache.jena.datatypes.xsd.XSDDateTime
import org.apache.jena.datatypes.xsd.impl.XSDDateType
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.ORG
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.XSD
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate

fun Regnskap.createJenaResponse(responseType: JenaType, urls: ExternalUrls) =
    listOf(this)
        .createModel(urls)
        .createResponseString(responseType)

fun List<Regnskap>.createJenaResponse(responseType: JenaType, urls: ExternalUrls) =
    createModel(urls)
        .createResponseString(responseType)

private fun List<Regnskap>.createModel(urls: ExternalUrls): Model {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("dct", DCTerms.getURI())
    model.setNsPrefix("br", BR.uri)
    model.setNsPrefix("xsd", XSD.getURI())
    model.setNsPrefix("org", ORG.getURI())
    model.setNsPrefix("schema", SCHEMA.uri)
    forEach {
        model.createResource(urls.self + it.id)
            .addProperty(RDF.type, BR.Regnskap)
            .addProperty(DCTerms.identifier, it.id)
            .addLiteral(BR.avviklingsregnskap, it.avviklingsregnskap)
            .safeAddLiteral(SCHEMA.currency, it.valuta)
            .addLiteral(BR.oppstillingsplan, it.oppstillingsplan.value)
            .addProperty(
                BR.revisjon,
                model.createResource(BR.Revisjon)
                    .addLiteral(BR.ikkeRevidertAarsregnskap, it.revisjon.ikkeRevidertAarsregnskap))
            .addProperty(
                BR.regnskapsprinsipper,
                model.createResource(BR.Regnskapsprinsipper)
                    .addLiteral(BR.smaaForetak, it.regnkapsprinsipper.smaaForetak)
                    .addLiteral(BR.regnskapsregler, it.regnkapsprinsipper.regnskapsregler.value))
            .addRegnskapsperiode(it.regnskapsperiode.fraDato, it.regnskapsperiode.tilDato)
            .addProperty(
                BR.virksomhet,
                model.createResource(BR.Virksomhet)
                    .safeAddLinkedProperty(ORG.organization, urls.organizationCatalogue, it.virksomhet.organisasjonsnummer)
                    .safeAddLiteral(BR.organisasjonsnummer, it.virksomhet.organisasjonsnummer)
                    .safeAddLiteral(BR.organisasjonsform, it.virksomhet.organisasjonsform)
                    .safeAddLiteral(BR.morselskap, it.virksomhet.morselskap))
            .addProperty(
                BR.eiendeler,
                model.createResource(BR.Eiendeler)
                    .addLiteral(BR.sumEiendeler, it.eiendeler.sumEiendeler ?: BigDecimal.ZERO)
                    .addProperty(
                        BR.anleggsmidler,
                        model.createResource(BR.Anleggsmidler)
                            .addLiteral(BR.sumAnleggsmidler, it.eiendeler.anleggsmidler.sumAnleggsmidler ?: BigDecimal.ZERO))
                    .addProperty(
                        BR.omloepsmidler,
                        model.createResource(BR.Omloepsmidler)
                            .addLiteral(BR.sumOmloepsmidler, it.eiendeler.omloepsmidler.sumOmloepsmidler ?: BigDecimal.ZERO)))
            .addProperty(
                BR.egenkapitalGjeld,
                model.createResource(BR.EgenkapitalGjeld)
                    .addLiteral(BR.sumEgenkapitalGjeld, it.egenkapitalGjeld.sumEgenkapitalGjeld ?: BigDecimal.ZERO)
                    .addProperty(
                        BR.egenkapital,
                        model.createResource(BR.Egenkapital)
                            .addLiteral(BR.sumEgenkapital, it.egenkapitalGjeld.egenkapital.sumEgenkapital ?: BigDecimal.ZERO)
                            .addProperty(
                                BR.innskuttEgenkapital,
                                model.createResource(BR.InnskuttEgenkapital)
                                    .addLiteral(BR.sumInnskuttEgenkaptial, it.egenkapitalGjeld.egenkapital.innskuttEgenkapital.sumInnskuttEgenkaptial ?: BigDecimal.ZERO))
                            .addProperty(
                                BR.opptjentEgenkapital,
                                model.createResource(BR.OpptjentEgenkapital)
                                    .addLiteral(BR.sumOpptjentEgenkapital, it.egenkapitalGjeld.egenkapital.opptjentEgenkapital.sumOpptjentEgenkapital ?: BigDecimal.ZERO)))
                    .addProperty(
                        BR.gjeldOversikt,
                        model.createResource(BR.GjeldOversikt)
                            .addLiteral(BR.sumGjeld, it.egenkapitalGjeld.gjeldOversikt.sumGjeld ?: BigDecimal.ZERO)
                            .addProperty(
                                BR.langsiktigGjeld,
                                model.createResource(BR.LangsiktigGjeld)
                                    .addLiteral(BR.sumLangsiktigGjeld, it.egenkapitalGjeld.gjeldOversikt.langsiktigGjeld.sumLangsiktigGjeld ?: BigDecimal.ZERO))
                            .addProperty(
                                BR.kortsiktigGjeld,
                                model.createResource(BR.KortsiktigGjeld)
                                    .addLiteral(BR.sumKortsiktigGjeld, it.egenkapitalGjeld.gjeldOversikt.kortsiktigGjeld.sumKortsiktigGjeld ?: BigDecimal.ZERO))))
            .addProperty(
                BR.resultatregnskapResultat,
                model.createResource(BR.ResultatregnskapResultat)
                    .addLiteral(BR.aarsresultat, it.resultatregnskapResultat.aarsresultat ?: BigDecimal.ZERO)
                    .addLiteral(BR.totalresultat, it.resultatregnskapResultat.totalresultat ?: BigDecimal.ZERO)
                    .addLiteral(BR.ordinaertResultatFoerSkattekostnad, it.resultatregnskapResultat.ordinaertResultatFoerSkattekostnad ?: BigDecimal.ZERO)
                    .addProperty(
                        BR.driftsresultat,
                        model.createResource(BR.Driftsresultat)
                            .addLiteral(BR.sumDriftsresultat, it.resultatregnskapResultat.driftsresultat.driftsresultat ?: BigDecimal.ZERO)
                            .addProperty(
                                BR.driftsinntekter,
                                model.createResource(BR.Driftsinntekter)
                                    .addLiteral(BR.sumDriftsinntekter, it.resultatregnskapResultat.driftsresultat.driftsinntekter.sumDriftsinntekter ?: BigDecimal.ZERO))
                            .addProperty(
                                BR.driftskostnad,
                                model.createResource(BR.Driftskostnad)
                                    .addLiteral(BR.annenDriftskostnad, it.resultatregnskapResultat.driftsresultat.driftskostnad.annenDriftskostnad ?: BigDecimal.ZERO)
                                    .addLiteral(BR.sumDriftskostnad, it.resultatregnskapResultat.driftsresultat.driftskostnad.sumDriftskostnad ?: BigDecimal.ZERO)))
                    .addProperty(
                        BR.finansresultat,
                        model.createResource(BR.Finansresultat)
                            .addLiteral(BR.nettoFinans, it.resultatregnskapResultat.finansresultat.nettoFinans ?: BigDecimal.ZERO)
                            .addProperty(
                                BR.finansinntekt,
                                model.createResource(BR.Finansinntekt)
                                    .addLiteral(BR.sumFinansinntekter, it.resultatregnskapResultat.finansresultat.finansinntekt.sumFinansinntekter ?: BigDecimal.ZERO))
                            .addProperty(
                                BR.finanskostnad,
                                model.createResource(BR.Finanskostnad)
                                    .addLiteral(BR.sumFinanskostnad, it.resultatregnskapResultat.finansresultat.finanskostnad.sumFinanskostnad ?: BigDecimal.ZERO))))
    }

    return model
}

private fun Resource.safeAddLinkedProperty(property: Property, baseUrl: String, value: String?): Resource =
    if(value == null) this
    else addProperty(property, model.createResource(baseUrl + value))

private fun Resource.safeAddLiteral(property: Property, value: Any?): Resource =
    if (value == null) this
    else addLiteral(property, value)

private fun Resource.addRegnskapsperiode(fraDato: LocalDate?, tilDato: LocalDate?): Resource =
    when {
        fraDato != null && tilDato != null -> addProperty(
            BR.regnskapsperiode,
            model.createResource(DCTerms.PeriodOfTime)
                .addLiteral(SCHEMA.startDate, model.createTypedLiteral(fraDato.toXSDDate(), XSDDateType.XSDdate))
                .addLiteral(SCHEMA.endDate, model.createTypedLiteral(tilDato.toXSDDate(), XSDDateType.XSDdate)))
        fraDato == null && tilDato != null -> addProperty(
            BR.regnskapsperiode,
            model.createResource(DCTerms.PeriodOfTime)
                .addLiteral(SCHEMA.endDate, model.createTypedLiteral(tilDato.toXSDDate(), XSDDateType.XSDdate)))
        fraDato != null && tilDato == null -> addProperty(
            BR.regnskapsperiode,
            model.createResource(DCTerms.PeriodOfTime)
                .addLiteral(SCHEMA.startDate, model.createTypedLiteral(fraDato.toXSDDate(), XSDDateType.XSDdate)))
        else -> this
    }

fun Model.createResponseString(responseType: JenaType):String =
    ByteArrayOutputStream().use{ stream ->
        write(stream, responseType.value)
        stream.flush()
        stream.toString("UTF-8")
    }

fun acceptHeaderToJenaType(accept: String?): JenaType =
    when (accept) {
        "text/turtle" -> JenaType.TURTLE
        "application/rdf+xml" -> JenaType.RDF_XML
        "application/rdf+json" -> JenaType.RDF_JSON
        "application/ld+json" -> JenaType.JSON_LD
        else -> JenaType.NOT_JENA
    }

enum class JenaType(val value: String){
    TURTLE("TURTLE"),
    RDF_XML("RDF/XML"),
    RDF_JSON("RDF/JSON"),
    JSON_LD("JSON-LD"),
    NOT_JENA("")
}

private fun LocalDate.toXSDDate(): XSDDateTime {
    val o = intArrayOf(year, monthValue, dayOfMonth, 0, 0, 0, 0, 0, 0)
    return XSDDateTime(o, XSDDateTime.YEAR_MASK.toInt() or XSDDateTime.MONTH_MASK.toInt() or XSDDateTime.DAY_MASK.toInt())
}

data class ExternalUrls(
    val self: String,
    val organizationCatalogue: String
)