package no.regnskap.jena

import org.apache.jena.rdf.model.ModelFactory

object BR {

    val uri = "https://github.com/Informasjonsforvaltning/regnskapsregister-api/blob/develop/src/main/resources/ontology/regnskapsregister.owl#"

    private val model = ModelFactory.createDefaultModel()

    val Regnskap = model.createResource(uri + "Regnskap")
    val Revisjon = model.createResource(uri + "Revisjon")
    val Regnskapsprinsipper = model.createResource(uri + "Regnskapsprinsipper")
    val Virksomhet = model.createResource(uri + "Virksomhet")

    val Eiendeler = model.createResource(uri + "Eiendeler")
    val Anleggsmidler = model.createResource(uri + "Anleggsmidler")
    val Omloepsmidler = model.createResource(uri + "Omloepsmidler")

    val EgenkapitalGjeld = model.createResource(uri + "EgenkapitalGjeld")
    val Egenkapital = model.createResource(uri + "Egenkapital")
    val InnskuttEgenkapital = model.createResource(uri + "InnskuttEgenkapital")
    val OpptjentEgenkapital = model.createResource(uri + "OpptjentEgenkapital")
    val GjeldOversikt = model.createResource(uri + "GjeldOversikt")
    val LangsiktigGjeld = model.createResource(uri + "LangsiktigGjeld")
    val KortsiktigGjeld = model.createResource(uri + "KortsiktigGjeld")

    val ResultatregnskapResultat = model.createResource(uri + "ResultatregnskapResultat")
    val Driftsresultat = model.createResource(uri + "Driftsresultat")
    val Driftsinntekter = model.createResource(uri + "Driftsinntekter")
    val Driftskostnad = model.createResource(uri + "Driftskostnad")
    val Finansresultat = model.createResource(uri + "Finansresultat")
    val Finansinntekt = model.createResource(uri + "Finansinntekt")
    val Finanskostnad = model.createResource(uri + "Finanskostnad")

    val avviklingsregnskap = model.createProperty(uri, "avviklingsregnskap")
    val oppstillingsplan = model.createProperty(uri, "oppstillingsplan")

    val revisjon = model.createProperty(uri, "revisjon")
    val ikkeRevidertAarsregnskap = model.createProperty(uri, "ikkeRevidertAarsregnskap")

    val regnskapsperiode = model.createProperty(uri, "regnskapsperiode")

    val regnskapsprinsipper = model.createProperty(uri, "regnskapsprinsipper")
    val smaaForetak = model.createProperty(uri, "smaaForetak")
    val regnskapsregler = model.createProperty(uri, "regnskapsregler")

    val virksomhet = model.createProperty(uri, "virksomhet")
    val organisasjonsnummer = model.createProperty(uri, "organisasjonsnummer")
    val organisasjonsform = model.createProperty(uri, "organisasjonsform")
    val morselskap = model.createProperty(uri, "morselskap")

    val eiendeler = model.createProperty(uri, "eiendeler")
    val sumEiendeler = model.createProperty(uri, "sumEiendeler")
    val anleggsmidler = model.createProperty(uri, "anleggsmidler")
    val sumAnleggsmidler = model.createProperty(uri, "sumAnleggsmidler")
    val omloepsmidler = model.createProperty(uri, "omloepsmidler")
    val sumOmloepsmidler = model.createProperty(uri, "sumOmloepsmidler")

    val egenkapitalGjeld = model.createProperty(uri, "egenkapitalGjeld")
    val sumEgenkapitalGjeld = model.createProperty(uri, "sumEgenkapitalGjeld")
    val egenkapital = model.createProperty(uri, "egenkapital")
    val sumEgenkapital = model.createProperty(uri, "sumEgenkapital")
    val innskuttEgenkapital = model.createProperty(uri, "innskuttEgenkapital")
    val sumInnskuttEgenkaptial = model.createProperty(uri, "sumInnskuttEgenkaptial")
    val opptjentEgenkapital = model.createProperty(uri, "opptjentEgenkapital")
    val sumOpptjentEgenkapital = model.createProperty(uri, "sumOpptjentEgenkapital")
    val gjeldOversikt = model.createProperty(uri, "gjeldOversikt")
    val sumGjeld = model.createProperty(uri, "sumGjeld")
    val langsiktigGjeld = model.createProperty(uri, "langsiktigGjeld")
    val sumLangsiktigGjeld = model.createProperty(uri, "sumLangsiktigGjeld")
    val kortsiktigGjeld = model.createProperty(uri, "kortsiktigGjeld")
    val sumKortsiktigGjeld = model.createProperty(uri, "sumKortsiktigGjeld")

    val resultatregnskapResultat = model.createProperty(uri, "resultatregnskapResultat")
    val aarsresultat = model.createProperty(uri, "aarsresultat")
    val totalresultat = model.createProperty(uri, "totalresultat")
    val ordinaertResultatFoerSkattekostnad = model.createProperty(uri, "ordinaertResultatFoerSkattekostnad")
    val driftsresultat = model.createProperty(uri, "driftsresultat")
    val sumDriftsresultat = model.createProperty(uri, "sumDriftsresultat")
    val driftsinntekter = model.createProperty(uri, "driftsinntekter")
    val sumDriftsinntekter = model.createProperty(uri, "sumDriftsinntekter")
    val driftskostnad = model.createProperty(uri, "driftskostnad")
    val annenDriftskostnad = model.createProperty(uri, "annenDriftskostnad")
    val sumDriftskostnad = model.createProperty(uri, "sumDriftskostnad")
    val finansresultat = model.createProperty(uri, "finansresultat")
    val nettoFinans = model.createProperty(uri, "nettoFinans")
    val finansinntekt = model.createProperty(uri, "finansinntekt")
    val sumFinansinntekter = model.createProperty(uri, "sumFinansinntekter")
    val finanskostnad = model.createProperty(uri, "finanskostnad")
    val sumFinanskostnad = model.createProperty(uri, "sumFinanskostnad")

}