package no.brreg.regnskap.repository;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import no.brreg.regnskap.model.RegnskapXml;
import no.brreg.regnskap.model.RegnskapXmlWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Component
public class RegnskapLogRepository {
    private static Logger LOGGER = LoggerFactory.getLogger(RegnskapLogRepository.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private RegnskapRepository regnskapRepository;


    public boolean hasLogged(final String filename) throws SQLException {
        boolean hasLogged = false;
        try (Connection connection = connectionManager.getConnection()) {
            try {
                final String sql = "SELECT COUNT(filename) FROM rregapi.regnskaplog WHERE filename=?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, filename);
                    ResultSet rs = stmt.executeQuery();
                    hasLogged = rs.next() && rs.getInt(1)>0;
                    LOGGER.info("Has " + (hasLogged?"":"NOT ") + "logged " + filename);
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

        return hasLogged;
    }

    public void persistRegnskapFile(final String filename, final InputStream regnskapStream) throws IOException, SQLException {
        File tmpFile = null;
        File tmpZipFile = null;
        try (Connection connection = connectionManager.getConnection()) {
            { //Just a scope for buffer[] ...
                byte[] buffer = new byte[100 * 1024]; //100KB chunks
                int bytesRead;

                //Dump regnskapStream to temp file
                tmpFile = File.createTempFile("rreg-", ".xml");
                try (OutputStream os = new FileOutputStream(tmpFile)) {
                    while ((bytesRead = regnskapStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                //Zip file
                tmpZipFile = File.createTempFile("rreg-", ".zip");
                try (FileInputStream fis = new FileInputStream(tmpFile);
                     FileOutputStream fos = new FileOutputStream(tmpZipFile);
                     ZipOutputStream zos = new ZipOutputStream(fos)) {
                    zos.putNextEntry(new ZipEntry(filename));
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                }
            }

            try (FileInputStream xmlFis = new FileInputStream(tmpFile);
                 FileInputStream zipFis = new FileInputStream(tmpZipFile)) {
                XmlMapper xmlMapper = new XmlMapper();
                RegnskapXmlWrap regnskapXmlWrap = xmlMapper.readValue(xmlFis, RegnskapXmlWrap.class);

                //Persist filename log entry
                Integer regnskapLogId = null;
                final String sql = "INSERT INTO rregapi.regnskaplog (filename,logtime,zipfile) " +
                        "VALUES (?,?,?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, filename);
                    stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setBinaryStream(3, zipFis);
                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        regnskapLogId = rs.getInt(1);
                    }
                }

                //Persist all regnskap
                long persisCount = 0;
                for (RegnskapXml regnskapXml : regnskapXmlWrap.getList()) {
                    if (regnskapXml.essentialFieldsIncluded()) {
                        regnskapRepository.persistRegnskap(connection, regnskapXml, regnskapLogId);
                        persisCount++;
                    } else {
                        LOGGER.info("Skipping " + regnskapXml.getReference() + " from file " + filename + ". Missing essential fields.");
                    }
                }

                LOGGER.info("Persisted " + persisCount + " regnskap from " + filename);
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                    throw e;
                } catch (SQLException e2) {
                    throw e2;
                }
            }
        } finally {
            if (tmpFile != null) {
                if(!tmpFile.delete()) {
                    LOGGER.error("Temporary file could not be deleted: ", tmpFile.getName());
                }
            }

            if (tmpZipFile != null) {
                if(!tmpZipFile.delete()) {
                    LOGGER.error("Temporary file could not be deleted: ", tmpZipFile.getName());
                }
            }
        }
    }

}
