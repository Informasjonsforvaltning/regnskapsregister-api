package no.regnskap.integration;

import no.regnskap.controller.StatistikkApiImpl;
import no.regnskap.model.dbo.RestcallLog;
import no.regnskap.repository.RestcallLogRepository;
import no.regnskap.utils.TestContainersBase;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ContextConfiguration(initializers = {RestcallLogApiTest.Initializer.class})
@Tag("service")
class RestcallLogApiTest extends TestContainersBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestcallLogApiTest.class);

    @Autowired
    private StatistikkApiImpl statistikkApi;

    @Autowired
    private RestcallLogRepository restcallLogRepository;
    private static boolean hasImportedTestdata = false;

    @Mock
    HttpServletRequest httpServletRequestMock;


    List<RestcallLog> testData = Collections.unmodifiableList(Arrays.asList(
            new RestcallLog("10.0.0.1","getRegnskapById","123456789"),
            new RestcallLog("10.0.0.1","getRegnskap","234567890"),
            new RestcallLog("10.0.0.2","getRegnskap","123456789")));

    @BeforeEach
    void resetMocks() throws SQLException, NoSuchAlgorithmException {
        Mockito.reset(
                httpServletRequestMock
        );

        if (!hasImportedTestdata) {
            for (RestcallLog restcallLog : testData) {
                restcallLogRepository.persistRestcall(restcallLog);
            }
            hasImportedTestdata = true;
        }
    }

    @Test
    public void getStatisticsByIpTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/json");
        ResponseEntity<List<String>> response = statistikkApi.getStatisticsByIp(httpServletRequestMock, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> body = response.getBody();
        assertEquals(2, body.size());
        assertEquals("2;7RZlwZAUbE3Ljrhx8dJJnWG7KTs=", body.get(0)); //Should have two calls from most active IP
        assertEquals("1;WrGH47xjdblaAlDy2MZnI96X3Lk=", body.get(1)); //Should have one call from least active IP
    }

    @Test
    public void getStatisticsByMethodTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/json");
        ResponseEntity<List<String>> response = statistikkApi.getStatisticsByMethod(httpServletRequestMock, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> body = response.getBody();
        assertEquals(2, body.size());
        assertEquals("2;getRegnskap", body.get(0)); //Should have two calls for getRegnskap
        assertEquals("1;getRegnskapById", body.get(1)); //Should have one call for getRegnskapById
    }

    @Test
    public void getStatisticsByOrgnrTest() {
        Mockito.when(httpServletRequestMock.getHeader("Accept")).thenReturn("application/json");
        ResponseEntity<List<String>> response = statistikkApi.getStatisticsByOrgnr(httpServletRequestMock, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> body = response.getBody();
        assertEquals(2, body.size());
        assertEquals("2;123456789", body.get(0)); //Should have two calls for 123456789
        assertEquals("1;234567890", body.get(1)); //Should have one call for 234567890
    }

}