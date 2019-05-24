package no.regnskap.controller;

import io.swagger.annotations.ApiParam;
import no.regnskap.generated.model.Regnskap;
import no.regnskap.service.RegnskapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class RegnskapsApiControllerImpl implements no.regnskap.generated.api.RegnskapsApi {
    private static Logger LOGGER = LoggerFactory.getLogger(RegnskapsApiControllerImpl.class);

    @Autowired
    private RegnskapService regnskapService;

    @Override
    public ResponseEntity<Regnskap> getRegnskapById(HttpServletRequest httpServletRequest, @ApiParam(value = "id",required=true) @PathVariable("id") String id) {
        ResponseEntity<Regnskap> response;

        try {
            response = regnskapService.getById(id)
                .map(regnskap -> new ResponseEntity<>(regnskap, HttpStatus.OK))
                .orElse( new ResponseEntity<>(HttpStatus.NO_CONTENT) );
        } catch (Exception e) {
            LOGGER.error("getRegnskapById failed:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}