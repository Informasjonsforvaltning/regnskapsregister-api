package no.regnskap.controller;

import io.swagger.annotations.ApiParam;
import no.regnskap.generated.model.Regnskap;
import no.regnskap.service.RegnskapService;
import no.regnskap.service.Task;
import no.regnskap.service.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class RegnskapApiImpl implements no.regnskap.generated.api.RegnskapApi {
    private static Logger LOGGER = LoggerFactory.getLogger(RegnskapApiImpl.class);

    @Autowired
    private RegnskapService regnskapService;

    @RequestMapping(value="/ping", method=GET, produces={"text/plain"})
    public ResponseEntity<String> getPing() {
        return ResponseEntity.ok("pong");
    }

    @RequestMapping(value="/ready", method=GET)
    public ResponseEntity getReady() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/regnskap/update", method=GET)
    public ResponseEntity queueUpdateTask(HttpServletRequest httpServletRequest) {
        UpdateService.addTask(Task.UPDATE_ACCOUNTING_DATA);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<List<Regnskap>> getRegnskap(HttpServletRequest httpServletRequest, @NotNull @ApiParam(value = "Virksomhetens organisasjonsnummer", required = true) @Valid @RequestParam(value = "orgNummer", required = true) String orgNummer) {
        List<Regnskap> regnskap;

        try {
            regnskap = regnskapService.getByOrgnr(orgNummer);
        } catch (Exception e) {
            LOGGER.error("getRegnskap failed:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(regnskap, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Regnskap> getRegnskapById(HttpServletRequest httpServletRequest, @ApiParam(value = "id",required=true) @PathVariable("id") String id) {
        ResponseEntity<Regnskap> response;

        try {
            response = regnskapService.getById(id)
                .map(regnskap -> new ResponseEntity<>(regnskap, HttpStatus.OK))
                .orElse( new ResponseEntity<>(HttpStatus.NOT_FOUND) );
        } catch (Exception e) {
            LOGGER.error("getRegnskapById failed:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}