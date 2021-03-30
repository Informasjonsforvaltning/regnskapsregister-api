package no.brreg.regnskap.repository;

import no.brreg.regnskap.generated.model.*;
import no.brreg.regnskap.mapper.RegnskapFieldsMapper;
import no.brreg.regnskap.model.RegnskapFields;
import no.brreg.regnskap.model.RegnskapXml;
import no.brreg.regnskap.model.RegnskapXmlHead;
import no.brreg.regnskap.model.RegnskapXmlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


@Component
public class RegnskapRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegnskapRepository.class);

    @Autowired
    private ConnectionManager connectionManager;


    public List<Regnskap> getByOrgnr(final String orgnr, final Integer id, final Integer år, final Regnskapstype regnskapstype, final RegnskapFieldsMapper.RegnskapFieldIncludeMode regnskapFieldIncludeMode) throws SQLException {
        if (regnskapFieldIncludeMode == RegnskapFieldsMapper.RegnskapFieldIncludeMode.DEFAULT) {
            return getByOrgnrDefault(orgnr, id);
        } else if (regnskapFieldIncludeMode == RegnskapFieldsMapper.RegnskapFieldIncludeMode.PARTNER) {
            return getByOrgnrPartner(orgnr, id, år, regnskapstype);
        } else {
            throw new IllegalArgumentException("Unexpected RegnskapFieldIncludeMode " + regnskapFieldIncludeMode.name());
        }
    }

    private List<Regnskap> getByOrgnrDefault(final String orgnr, final Integer id) throws SQLException {
        List<Regnskap> regnskapList = new ArrayList<>();
        if (orgnr != null) {
            try (Connection connection = connectionManager.getConnection()) {
                try {
                    String sql =
                            "SELECT _id, orgnr, regnskapstype, regnaar, oppstillingsplan_versjonsnr, valutakode, startdato, " +
                             "avslutningsdato, mottakstype, avviklingsregnskap, feilvaloer, journalnr, mottatt_dato, " +
                             "orgform, mor_i_konsern, regler_smaa, fleksible_poster, fravalg_revisjon, utarbeidet_regnskapsforer, " +
                             "bistand_regnskapsforer, aarsregnskapstype, land_for_land, revisorberetning_ikke_levert, " +
                             "ifrs_selskap, forenklet_ifrs_selskap, ifrs_konsern, forenklet_ifrs_konsern, regnskap_dokumenttype " +
                            "FROM rregapi.regnskap ";

                    if (orgnr!=null && id!=null) {
                        sql += "WHERE orgnr=? and _id=?";
                    } else {
                        sql += "WHERE _id=" +
                               "(SELECT MAX(_id) FROM rregapi.regnskap WHERE orgnr=? AND LOWER(regnskapstype)=? " +
                                "AND regnaar=" +
                                 "(SELECT MAX(regnaar) FROM rregapi.regnskap WHERE orgnr=? AND LOWER(regnskapstype)=?)" +
                               ")";
                    }

                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        if (orgnr!=null && id!=null) {
                            stmt.setString(1, orgnr);
                            stmt.setInt(2, id.intValue());
                        } else {
                            stmt.setString(1, orgnr);
                            stmt.setString(2, no.brreg.regnskap.model.dbo.Regnskap.REGNSKAPSTYPE_SELSKAP.toLowerCase());
                            stmt.setString(3, orgnr);
                            stmt.setString(4, no.brreg.regnskap.model.dbo.Regnskap.REGNSKAPSTYPE_SELSKAP.toLowerCase());
                        }

                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            Regnskap regnskap = createRegnskap(readInteger(rs, "_id"),
                                    readString(rs, "orgnr"), readString(rs, "regnskapstype"),
                                    readInteger(rs, "regnaar"), readString(rs, "oppstillingsplan_versjonsnr"),
                                    readString(rs, "valutakode"), readDate(rs, "startdato"),
                                    readDate(rs, "avslutningsdato"), readString(rs, "mottakstype"),
                                    readBoolean(rs, "avviklingsregnskap"), readBoolean(rs, "feilvaloer"),
                                    readString(rs, "journalnr"), readDate(rs, "mottatt_dato"),
                                    readString(rs, "orgform"), readBoolean(rs, "mor_i_konsern"),
                                    readBoolean(rs, "regler_smaa"), readBoolean(rs, "fleksible_poster"),
                                    readBoolean(rs, "fravalg_revisjon"), readBoolean(rs, "utarbeidet_regnskapsforer"),
                                    readBoolean(rs, "bistand_regnskapsforer"), readString(rs, "aarsregnskapstype"),
                                    readBoolean(rs, "land_for_land"), readBoolean(rs, "revisorberetning_ikke_levert"),
                                    readBoolean(rs, "ifrs_selskap"), readBoolean(rs, "forenklet_ifrs_selskap"),
                                    readBoolean(rs, "ifrs_konsern"), readBoolean(rs, "forenklet_ifrs_konsern"),
                                    readString(rs, "regnskap_dokumenttype"));

                            regnskapList.add(regnskap);
                        }
                    }

                    populateRegnskapWithFields(connection, regnskapList, RegnskapFieldsMapper.RegnskapFieldIncludeMode.DEFAULT);

                    connection.commit();
                } catch (Exception e) {
                    try {
                        connection.rollback();
                        throw e;
                    } catch (SQLException e2) {
                        throw e2;
                    }
                }
            }
        }
        return regnskapList;
    }

    private List<Regnskap> getByOrgnrPartner(final String orgnr, final Integer id, final Integer år, final Regnskapstype regnskapstype) throws SQLException {
        List<Regnskap> regnskapList = new ArrayList<>();
        if (orgnr != null) {
            try (Connection connection = connectionManager.getConnection()) {
                try {
                    String sql =
                            "SELECT a._id, a.orgnr, a.regnskapstype, a.regnaar, a.oppstillingsplan_versjonsnr, a.valutakode, a.startdato, " +
                             "a.avslutningsdato, a.mottakstype, a.avviklingsregnskap, a.feilvaloer, a.journalnr, a.mottatt_dato, " +
                             "a.orgform, a.mor_i_konsern, a.regler_smaa, a.fleksible_poster, a.fravalg_revisjon, a.utarbeidet_regnskapsforer, " +
                             "a.bistand_regnskapsforer, a.aarsregnskapstype, a.land_for_land, a.revisorberetning_ikke_levert, " +
                             "a.ifrs_selskap, a.forenklet_ifrs_selskap, a.ifrs_konsern, a.forenklet_ifrs_konsern, a.regnskap_dokumenttype " +
                            "FROM rregapi.regnskap a ";

                    if (orgnr!=null && id!=null) {
                        sql += "WHERE orgnr=? and _id=?";
                    } else {
                        sql += "INNER JOIN " +
                                "(SELECT MAX(_id) AS _id, regnaar, regnskapstype FROM rregapi.regnskap " +
                                "WHERE orgnr=? GROUP BY regnskapstype, regnaar) b " +
                                "ON a._id=b._id " +
                                "INNER JOIN " +
                                "(SELECT MAX(regnaar) AS regnaar FROM rregapi.regnskap WHERE orgnr=?) c " +
                                "ON a.regnaar > (c.regnaar-3) ";

                        if (år != null) {
                            sql += "WHERE a.regnaar=? ";
                        }

                        if (regnskapstype != null) {
                            sql += (år == null ? "WHERE" : "AND") + " LOWER(a.regnskapstype)=? ";
                        }
                    }

                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        int i = 1;
                        if (orgnr!=null && id!=null) {
                            stmt.setString(1, orgnr);
                            stmt.setInt(2, id.intValue());
                        } else {
                            stmt.setString(i++, orgnr);
                            stmt.setString(i++, orgnr);

                            if (år != null) {
                                stmt.setInt(i++, år);
                            }

                            if (regnskapstype != null) {
                                stmt.setString(i++, no.brreg.regnskap.model.dbo.Regnskap.regnskapstypeToString(regnskapstype).toLowerCase());
                            }
                        }

                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            Regnskap regnskap = createRegnskap(readInteger(rs, "_id"),
                                    readString(rs, "orgnr"), readString(rs, "regnskapstype"),
                                    readInteger(rs, "regnaar"), readString(rs, "oppstillingsplan_versjonsnr"),
                                    readString(rs, "valutakode"), readDate(rs, "startdato"),
                                    readDate(rs, "avslutningsdato"), readString(rs, "mottakstype"),
                                    readBoolean(rs, "avviklingsregnskap"), readBoolean(rs, "feilvaloer"),
                                    readString(rs, "journalnr"), readDate(rs, "mottatt_dato"),
                                    readString(rs, "orgform"), readBoolean(rs, "mor_i_konsern"),
                                    readBoolean(rs, "regler_smaa"), readBoolean(rs, "fleksible_poster"),
                                    readBoolean(rs, "fravalg_revisjon"), readBoolean(rs, "utarbeidet_regnskapsforer"),
                                    readBoolean(rs, "bistand_regnskapsforer"), readString(rs, "aarsregnskapstype"),
                                    readBoolean(rs, "land_for_land"), readBoolean(rs, "revisorberetning_ikke_levert"),
                                    readBoolean(rs, "ifrs_selskap"), readBoolean(rs, "forenklet_ifrs_selskap"),
                                    readBoolean(rs, "ifrs_konsern"), readBoolean(rs, "forenklet_ifrs_konsern"),
                                    readString(rs, "regnskap_dokumenttype"));

                            regnskapList.add(regnskap);
                        }
                    }

                    populateRegnskapWithFields(connection, regnskapList, RegnskapFieldsMapper.RegnskapFieldIncludeMode.PARTNER);

                    connection.commit();
                } catch (Exception e) {
                    try {
                        connection.rollback();
                        throw e;
                    } catch (SQLException e2) {
                        throw e2;
                    }
                }
            }
        }
        return regnskapList;
    }

    public List<String> getLog() throws SQLException {
        List<String> logList = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection()) {
            try {
                String sql = "SELECT filename FROM rregapi.regnskaplog ORDER BY filename ASC";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        logList.add(readString(rs, "filename"));
                    }
                }
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                    throw e;
                } catch (SQLException e2) {
                    throw e2;
                }
            }
        }
        return logList;
    }

    public void persistRegnskap(final Connection connection, final RegnskapXml regnskapXml, final Integer regnskapLogId) throws SQLException {
        RegnskapXmlHead regnskapXmlHead = regnskapXml.getHead();
        String sql = "INSERT INTO rregapi.regnskap " +
                "(orgnr, regnskapstype, regnaar, oppstillingsplan_versjonsnr, valutakode, startdato, "+
                 "avslutningsdato, mottakstype, avviklingsregnskap, feilvaloer, journalnr, mottatt_dato, "+
                 "orgform, mor_i_konsern, regler_smaa, fleksible_poster, fravalg_revisjon, utarbeidet_regnskapsforer, "+
                 "bistand_regnskapsforer, aarsregnskapstype, land_for_land, revisorberetning_ikke_levert, "+
                 "ifrs_selskap, forenklet_ifrs_selskap, ifrs_konsern, forenklet_ifrs_konsern, regnskap_dokumenttype, _id_regnskaplog) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Integer regnskapId = null;
        if (regnskapXmlHead.getMottattDato()==null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            Date today = new Date(System.currentTimeMillis());
            regnskapXmlHead.setMottattDato(formatter.format(today));
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, regnskapXmlHead.getOrgnr());
            stmt.setString(2, regnskapXmlHead.getRegnskapstype());
            stmt.setInt(3, regnskapXmlHead.getRegnaar());
            stmt.setString(4, regnskapXmlHead.getOppstillingsplanVersjonsnr());
            stmt.setString(5, regnskapXmlHead.getValutakode());
            stmt.setDate(6, ConnectionManager.toSqlDate("yyyyMMdd", regnskapXmlHead.getStartdato()));
            stmt.setDate(7, ConnectionManager.toSqlDate("yyyyMMdd", regnskapXmlHead.getAvslutningsdato()));
            stmt.setString(8, regnskapXmlHead.getMottakstype());
            setBoolean(stmt, 9, kodeToBoolean(regnskapXmlHead.getAvviklingsregnskap()));
            setBoolean(stmt, 10, kodeToBoolean(regnskapXmlHead.getFeilvaloer()));
            stmt.setString(11, regnskapXmlHead.getJournalnr());
            stmt.setDate(12, ConnectionManager.toSqlDate("yyyyMMdd", regnskapXmlHead.getMottattDato()));
            stmt.setString(13, regnskapXmlHead.getOrgform());
            setBoolean(stmt, 14, kodeToBoolean(regnskapXmlHead.getMorselskap()));
            setBoolean(stmt, 15, kodeToBoolean(regnskapXmlHead.getReglerSmaa()));
            setBoolean(stmt, 16, kodeToBoolean(regnskapXmlHead.getFleksiblePoster()));
            setBoolean(stmt, 17, kodeToBoolean(regnskapXmlHead.getFravalgRevisjon()));
            setBoolean(stmt, 18, kodeToBoolean(regnskapXmlHead.getUtarbeidetRegnskapsforer()));
            setBoolean(stmt, 19, kodeToBoolean(regnskapXmlHead.getBistandRegnskapsforer()));
            stmt.setString(20, regnskapXmlHead.getAarsregnskapstype());
            setBoolean(stmt, 21, kodeToBoolean(regnskapXmlHead.getLandForLand()));
            setBoolean(stmt, 22, kodeToBoolean(regnskapXmlHead.getRevisorberetningIkkeLevert()));
            setBoolean(stmt, 23, kodeToBoolean(regnskapXmlHead.getIfrsSelskap()));
            setBoolean(stmt, 24, kodeToBoolean(regnskapXmlHead.getForenkletIfrsSelskap()));
            setBoolean(stmt, 25, kodeToBoolean(regnskapXmlHead.getIfrsKonsern()));
            setBoolean(stmt, 26, kodeToBoolean(regnskapXmlHead.getForenkletIfrsKonsern()));
            stmt.setString(27, regnskapXmlHead.getRegnskapDokumenttype());
            setInteger(stmt, 28, regnskapLogId);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                regnskapId = rs.getInt(1);
            }
        }

        sql = "INSERT INTO rregapi.felt " +
                "(_id_regnskap, kode, sum) " +
                "VALUES (?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (RegnskapXmlInfo post : regnskapXml.getPosts()) {
                stmt.setInt(1, regnskapId);
                stmt.setString(2, post.getFeltkode());
                stmt.setBigDecimal(3, post.getSum());
                stmt.executeUpdate();
            }
        }
    }

    public Integer persistRegnskap(final Regnskap regnskap) throws SQLException {
        try (Connection connection = connectionManager.getConnection()) {
            try {
                Integer regnskapId = persistRegnskap(connection, regnskap);
                connection.commit();
                return regnskapId;
            } catch (Exception e) {
                try {
                    connection.rollback();
                    throw e;
                } catch (SQLException e2) {
                    throw e2;
                }
            }
        }
    }

    public Integer persistRegnskap(final Connection connection, final Regnskap regnskap) throws SQLException {
        String sql = "INSERT INTO rregapi.regnskap " +
                "(orgnr, regnskapstype, regnaar, oppstillingsplan_versjonsnr, valutakode, startdato, "+
                "avslutningsdato, mottakstype, avviklingsregnskap, feilvaloer, journalnr, mottatt_dato, "+
                "orgform, mor_i_konsern, regler_smaa, fleksible_poster, fravalg_revisjon, utarbeidet_regnskapsforer, "+
                "bistand_regnskapsforer, aarsregnskapstype, land_for_land, revisorberetning_ikke_levert, "+
                "ifrs_selskap, forenklet_ifrs_selskap, ifrs_konsern, forenklet_ifrs_konsern, regnskap_dokumenttype, _id_regnskaplog) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Integer regnskapId = null;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, regnskap.getVirksomhet().getOrganisasjonsnummer());
            stmt.setString(2, no.brreg.regnskap.model.dbo.Regnskap.regnskapstypeToString(regnskap.getRegnskapstype()));
            stmt.setInt(3, regnskap.getRegnskapsperiode().getFraDato().getYear());
            stmt.setNull(4, Types.VARCHAR);//stmt.setString(4, regnskapXmlHead.getOppstillingsplanVersjonsnr());
            stmt.setString(5, regnskap.getValuta());
            stmt.setDate(6, Date.valueOf(regnskap.getRegnskapsperiode().getFraDato()));
            stmt.setDate(7, Date.valueOf(regnskap.getRegnskapsperiode().getTilDato()));
            stmt.setNull(8, Types.VARCHAR);//stmt.setString(8, regnskapXmlHead.getMottakstype());
            setBoolean(stmt, 9, regnskap.getAvviklingsregnskap());
            stmt.setNull(10, Types.BOOLEAN);//stmt.setBoolean(10, kodeToBoolean(regnskapXmlHead.getFeilvaloer()));
            stmt.setString(11, regnskap.getJournalnr());
            stmt.setNull(12, Types.DATE);//stmt.setDate(12, ConnectionManager.toSqlDate("yyyyMMdd", regnskapXmlHead.getMottattDato()));
            stmt.setString(13, regnskap.getVirksomhet().getOrganisasjonsform());
            setBoolean(stmt, 14, regnskap.getVirksomhet().getMorselskap());
            setBoolean(stmt, 15, regnskap.getRegnkapsprinsipper().getSmaaForetak());
            stmt.setNull(16, Types.BOOLEAN);//stmt.setBoolean(16, kodeToBoolean(regnskapXmlHead.getFleksiblePoster()));
            setBoolean(stmt, 17, regnskap.getRevisjon().getFravalgRevisjon());
            stmt.setNull(18, Types.BOOLEAN);//stmt.setBoolean(18, kodeToBoolean(regnskapXmlHead.getUtarbeidetRegnskapsforer()));
            stmt.setNull(19, Types.BOOLEAN);//stmt.setBoolean(19, kodeToBoolean(regnskapXmlHead.getBistandRegnskapsforer()));
            stmt.setString(20, regnskap.getOppstillingsplan().getValue());
            stmt.setNull(21, Types.BOOLEAN);//stmt.setBoolean(21, kodeToBoolean(regnskapXmlHead.getLandForLand()));
            setBoolean(stmt, 22, regnskap.getRevisjon().getIkkeRevidertAarsregnskap());
            setBoolean(stmt, 23, regnskap.getRegnkapsprinsipper().getRegnskapsregler() == Regnskapsprinsipper.RegnskapsreglerEnum.IFRS);
            setBoolean(stmt, 24, regnskap.getRegnkapsprinsipper().getRegnskapsregler() == Regnskapsprinsipper.RegnskapsreglerEnum.FORENKLETANVENDELSEIFRS);
            stmt.setNull(25, Types.BOOLEAN);//stmt.setBoolean(25, kodeToBoolean(regnskapXmlHead.getIfrsKonsern()));
            stmt.setNull(26, Types.BOOLEAN);//stmt.setBoolean(26, kodeToBoolean(regnskapXmlHead.getForenkletIfrsKonsern()));
            stmt.setString(27, regnskap.getRegnskapDokumenttype());
            stmt.setNull(28, Types.INTEGER);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                regnskapId = rs.getInt(1);
                LOGGER.info("Generated regnskapId: " + regnskapId);
            }
        }

        sql = "INSERT INTO rregapi.felt " +
                "(_id_regnskap, kode, sum) " +
                "VALUES (?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Map<String,BigDecimal> fields = RegnskapFieldsMapper.regnskapFields(regnskap);
            for (Map.Entry<String,BigDecimal> field : fields.entrySet()) {
                stmt.setInt(1, regnskapId);
                stmt.setString(2, field.getKey());
                stmt.setBigDecimal(3, field.getValue());
                stmt.executeUpdate();
            }
        }

        return regnskapId;
    }

    public static Regnskap createRegnskap(final Integer _id, final String orgnr, final String regnskapstype, final Integer regnaar,
          final String oppstillingsplan_versjonsnr, final String valutakode, final LocalDate startdato,
          final LocalDate avslutningsdato, final String mottakstype, final Boolean avviklingsregnskap, final Boolean feilvaloer,
          final String journalnr, final LocalDate mottatt_dato, final String orgform, final Boolean mor_i_konsern,
          final Boolean regler_smaa, final Boolean fleksible_poster, final Boolean fravalg_revisjon,
          final Boolean utarbeidet_regnskapsforer, final Boolean bistand_regnskapsforer, final String aarsregnskapstype,
          final Boolean land_for_land, final Boolean revisorberetning_ikke_levert, final Boolean ifrs_selskap,
          final Boolean forenklet_ifrs_selskap, final Boolean ifrs_konsern, final Boolean forenklet_ifrs_konsern,
          final String regnskap_dokumenttype) {

        Regnskapsprinsipper.RegnskapsreglerEnum regnskapsregler = Regnskapsprinsipper.RegnskapsreglerEnum.REGNSKAPSLOVENALMINNELIGREGLER;
        Regnskapstype regnskapsType = no.brreg.regnskap.model.dbo.Regnskap.regnskapstypeFromString(regnskapstype);

        if (regnskapsType == Regnskapstype.SELSKAP) {
            if (ifrs_selskap!=null && ifrs_selskap) {
                regnskapsregler = Regnskapsprinsipper.RegnskapsreglerEnum.IFRS;
            }
            if (forenklet_ifrs_selskap!=null && forenklet_ifrs_selskap) {
                regnskapsregler = Regnskapsprinsipper.RegnskapsreglerEnum.FORENKLETANVENDELSEIFRS;
            }
        } else if (regnskapsType == Regnskapstype.KONSERN) {
            if (ifrs_konsern!=null && ifrs_konsern) {
                regnskapsregler = Regnskapsprinsipper.RegnskapsreglerEnum.IFRS;
            }
            if (forenklet_ifrs_konsern!=null && forenklet_ifrs_konsern) {
                regnskapsregler = Regnskapsprinsipper.RegnskapsreglerEnum.FORENKLETANVENDELSEIFRS;
            }
        }

        Regnskap regnskap = new Regnskap().id(_id).journalnr(journalnr)
            .regnskapstype(regnskapsType)
            .valuta(valutakode)
            .oppstillingsplan(Regnskap.OppstillingsplanEnum.fromValue(aarsregnskapstype.toLowerCase()))
            .regnskapsperiode(new Tidsperiode().fraDato(startdato)
                                               .tilDato(avslutningsdato))
            .regnskapDokumenttype(regnskap_dokumenttype);

        if (avviklingsregnskap!=null) {regnskap.avviklingsregnskap(avviklingsregnskap);}

        //Revisjon
        Revisjon revisjon = new Revisjon();
        if (revisorberetning_ikke_levert!=null) {revisjon.ikkeRevidertAarsregnskap(revisorberetning_ikke_levert);}
        if (fravalg_revisjon!=null) {revisjon.fravalgRevisjon(fravalg_revisjon);}
        regnskap.revisjon(revisjon);

        //Regnskapsprinsipper
        Regnskapsprinsipper regnskapsprinsipper = new Regnskapsprinsipper().regnskapsregler(regnskapsregler);
        if (regler_smaa!=null) {regnskapsprinsipper.smaaForetak(regler_smaa);}
        regnskap.regnkapsprinsipper(regnskapsprinsipper);

        //Virksomhet
        Virksomhet virksomhet = new Virksomhet().organisasjonsnummer(orgnr)
                                                .organisasjonsform(orgform);
        if (mor_i_konsern!=null) {virksomhet.morselskap(mor_i_konsern);}
        regnskap.virksomhet(virksomhet);

        return regnskap;
    }

    private void populateRegnskapWithFields(final Connection connection, final Regnskap regnskap, final RegnskapFieldsMapper.RegnskapFieldIncludeMode regnskapFieldIncludeMode) throws SQLException {
        if (regnskap != null) {
            populateRegnskapWithFields(connection, Collections.singletonList(regnskap), regnskapFieldIncludeMode);
        }
    }

    private void populateRegnskapWithFields(final Connection connection, final List<Regnskap> regnskapList, final RegnskapFieldsMapper.RegnskapFieldIncludeMode regnskapFieldIncludeMode) throws SQLException {
        final String sql = "SELECT kode, sum FROM rregapi.felt WHERE _id_regnskap IN"+
                          " (SELECT _id FROM rregapi.regnskap WHERE journalnr=? AND LOWER(regnskapstype)=?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Regnskap regnskap : regnskapList) {
                stmt.setString(1, regnskap.getJournalnr());
                stmt.setString(2, no.brreg.regnskap.model.dbo.Regnskap.regnskapstypeToString(regnskap.getRegnskapstype()));
                ResultSet rs = stmt.executeQuery();

                List<RegnskapXmlInfo> feltList = new ArrayList<>();
                while (rs.next()) {
                    RegnskapXmlInfo regnskapXmlInfo = new RegnskapXmlInfo();
                    regnskapXmlInfo.setFeltkode(readString(rs, "kode"));
                    regnskapXmlInfo.setSum(readBigDecimal(rs, "sum"));
                    feltList.add(regnskapXmlInfo);
                }

                RegnskapFields fields = new RegnskapFields();
                RegnskapFieldsMapper.mapFieldsFromXmlData(feltList, fields, regnskapFieldIncludeMode);
                regnskap.egenkapitalGjeld(fields.getEgenkapitalGjeld());
                regnskap.eiendeler(fields.getEiendeler());
                regnskap.resultatregnskapResultat(fields.getResultatregnskapResultat());
                regnskap.setRegnskapDokumenttype(null); //We may have mixed RES and BAL. Just clear regnskap_dokumenttype
            }
        }
    }

    private String readString(final ResultSet rs, final String column) throws SQLException {
        String result = rs.getString(column);
        if (rs.wasNull()) {
            return null;
        }
        return result;
    }

    private void setInteger(final PreparedStatement stmt, final int index, final Integer value) throws SQLException {
        if (value==null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    private Integer readInteger(final ResultSet rs, final String column) throws SQLException {
        Integer result = rs.getInt(column);
        if (rs.wasNull()) {
            return null;
        }
        return result;
    }

    private void setBoolean(final PreparedStatement stmt, final int index, final Boolean value) throws SQLException {
        if (value==null) {
            stmt.setNull(index, Types.BOOLEAN);
        } else {
            stmt.setBoolean(index, value);
        }
    }

    private Boolean readBoolean(final ResultSet rs, final String column) throws SQLException {
        Boolean result = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return result;
    }

    private BigDecimal readBigDecimal(final ResultSet rs, final String column) throws SQLException {
        BigDecimal result = rs.getBigDecimal(column);
        if (rs.wasNull()) {
            return null;
        }
        return result;
    }

    private LocalDate readDate(final ResultSet rs, final String column) throws SQLException {
        Date date = rs.getDate(column);
        if (rs.wasNull()) {
            return null;
        }
        return date.toLocalDate();
    }

    private Boolean kodeToBoolean(final String kode) {
        return kode==null?null:!"N".equalsIgnoreCase(kode);
    }

    private String booleanToKode(final boolean value, final ResultSet rs) throws SQLException {
        return booleanToKode(rs.wasNull() ? null : value);
    }

    private String booleanToKode(final Boolean value) {
        if (value==null) {
            return null;
        }
        return value ? "J" : "N";
    }

}
