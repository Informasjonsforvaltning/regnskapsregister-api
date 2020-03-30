package no.regnskap.mapper

import no.regnskap.model.RegnskapFieldsDB
import no.regnskap.model.RegnskapXmlInfo

private const val FELTKODE_EIENDELER = "219"
private const val FELTKODE_ANLEGGSMIDLER = "217"
private const val FELTKODE_OMLOEPSMIDLER = "194"
private const val FELTKODE_EGENKAPITAL_GJELD = "251"
private const val FELTKODE_EGENKAPITAL = "250"
private const val FELTKODE_INNSKUTT_EGENKAPITAL = "3730"
private const val FELTKODE_OPPTJENT_EGENKAPITAL = "9702"
private const val FELTKODE_GJELD = "1119"
private const val FELTKODE_LANGSIKTIG_GJELD = "86"
private const val FELTKODE_KORTSIKTIG_GJELD = "85"
private const val FELTKODE_RESULTAT_AAR = "172"
private const val FELTKODE_RESULTAT_TOTAL = "36633"
private const val FELTKODE_RESULTAT_ORDINAERT = "167"
private const val FELTKODE_DRIFTSRESULTAT = "146"
private const val FELTKODE_DRIFTSINNTEKTER = "72"
private const val FELTKODE_ANNEN_DRIFTSKOSTNAD = "82"
private const val FELTKODE_DRIFTSKOSTNAD = "17126"
private const val FELTKODE_FINANSRESULTAT = "158"
private const val FELTKODE_FINANSINNTEKT = "153"
private const val FELTKODE_FINANSKOSTNAD = "17130"

fun mapFieldsFromXmlData(fields: RegnskapFieldsDB, xmlData: List<RegnskapXmlInfo>): RegnskapFieldsDB {
    xmlData.forEach {
        when (it.feltkode) {
            FELTKODE_EIENDELER -> fields.eiendeler.sumEiendeler = it.sum
            FELTKODE_ANLEGGSMIDLER -> fields.eiendeler.anleggsmidler.sumAnleggsmidler = it.sum
            FELTKODE_OMLOEPSMIDLER -> fields.eiendeler.omloepsmidler.sumOmloepsmidler = it.sum
            FELTKODE_EGENKAPITAL_GJELD -> fields.egenkapitalGjeld.sumEgenkapitalGjeld = it.sum
            FELTKODE_EGENKAPITAL -> fields.egenkapitalGjeld.egenkapital.sumEgenkapital = it.sum
            FELTKODE_INNSKUTT_EGENKAPITAL -> fields.egenkapitalGjeld.egenkapital.innskuttEgenkapital.sumInnskuttEgenkaptial = it.sum
            FELTKODE_OPPTJENT_EGENKAPITAL -> fields.egenkapitalGjeld.egenkapital.opptjentEgenkapital.sumOpptjentEgenkapital = it.sum
            FELTKODE_GJELD -> fields.egenkapitalGjeld.gjeldOversikt.sumGjeld = it.sum
            FELTKODE_LANGSIKTIG_GJELD -> fields.egenkapitalGjeld.gjeldOversikt.langsiktigGjeld.sumLangsiktigGjeld = it.sum
            FELTKODE_KORTSIKTIG_GJELD -> fields.egenkapitalGjeld.gjeldOversikt.kortsiktigGjeld.sumKortsiktigGjeld = it.sum
            FELTKODE_RESULTAT_AAR -> fields.resultatregnskapResultat.aarsresultat = it.sum
            FELTKODE_RESULTAT_TOTAL -> fields.resultatregnskapResultat.totalresultat = it.sum
            FELTKODE_RESULTAT_ORDINAERT -> fields.resultatregnskapResultat.ordinaertResultatFoerSkattekostnad = it.sum
            FELTKODE_DRIFTSRESULTAT -> fields.resultatregnskapResultat.driftsresultat.driftsresultat = it.sum
            FELTKODE_DRIFTSINNTEKTER -> fields.resultatregnskapResultat.driftsresultat.driftsinntekter.sumDriftsinntekter = it.sum
            FELTKODE_ANNEN_DRIFTSKOSTNAD -> fields.resultatregnskapResultat.driftsresultat.driftskostnad.annenDriftskostnad = it.sum
            FELTKODE_DRIFTSKOSTNAD -> fields.resultatregnskapResultat.driftsresultat.driftskostnad.sumDriftskostnad = it.sum
            FELTKODE_FINANSRESULTAT -> fields.resultatregnskapResultat.finansresultat.nettoFinans = it.sum
            FELTKODE_FINANSINNTEKT -> fields.resultatregnskapResultat.finansresultat.finansinntekt.sumFinansinntekter = it.sum
            FELTKODE_FINANSKOSTNAD -> fields.resultatregnskapResultat.finansresultat.finanskostnad.sumFinanskostnad = it.sum
        }
    }

    return fields
}
