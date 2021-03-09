package no.regnskap.integration;

import no.regnskap.controller.HealthController;
import no.regnskap.utils.EmbeddedPostgresBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ContextConfiguration(initializers = {EmbeddedPostgresBase.Initializer.class})
public class HealthControllerTest extends EmbeddedPostgresBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(HealthControllerTest.class);

    @Autowired
    private HealthController healthController;


    @Test
    public void getPingTest() {
        ResponseEntity<String> response = healthController.getPing();
        assertEquals("pong", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getReadyTest() {
        ResponseEntity<Void> response = healthController.getReady();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
