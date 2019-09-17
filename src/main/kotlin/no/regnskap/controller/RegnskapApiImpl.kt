package no.regnskap.controller

import no.regnskap.jena.JenaType
import no.regnskap.jena.acceptHeaderToJenaType
import no.regnskap.jena.createJenaResponse
import no.regnskap.service.RegnskapService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

import javax.servlet.http.HttpServletRequest

import org.springframework.web.bind.annotation.RequestMethod.GET

private val LOGGER = LoggerFactory.getLogger(RegnskapApiImpl::class.java)

@Controller
open class RegnskapApiImpl(val regnskapService: RegnskapService): no.regnskap.generated.api.RegnskapApi {

    val ping: ResponseEntity<String>
        @RequestMapping(value = ["/ping"], method = [GET], produces = ["text/plain"])
        get() = ResponseEntity.ok("pong")

    val ready: ResponseEntity<*>
        @RequestMapping(value = ["/ready"], method = [GET])
        get() = ResponseEntity.ok().build<Any>()

    override fun getLog(httpServletRequest: HttpServletRequest): ResponseEntity<List<String>> {
        return ResponseEntity(regnskapService.getLog(), HttpStatus.OK)
    }

    override fun getRegnskap(httpServletRequest: HttpServletRequest, orgNummer: String): ResponseEntity<Any> =
        try {
            val regnskap = regnskapService.getByOrgnr(orgNummer)
            val jenaType = acceptHeaderToJenaType(httpServletRequest.getHeader("Accept"))

            if (jenaType == JenaType.NOT_JENA) {
                ResponseEntity(regnskap, HttpStatus.OK)
            } else {
                ResponseEntity(regnskap.createJenaResponse(jenaType), HttpStatus.OK)
            }

        } catch (e: Exception) {
            LOGGER.error("getRegnskap failed:", e)
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    override fun getRegnskapById(httpServletRequest: HttpServletRequest, id: String): ResponseEntity<Any> =
        try {
            val regnskap = regnskapService.getById(id)
            val jenaType = acceptHeaderToJenaType(httpServletRequest.getHeader("Accept"))

            if (regnskap == null) {
                ResponseEntity(HttpStatus.NOT_FOUND)
            } else if (jenaType == JenaType.NOT_JENA) {
                ResponseEntity<Any>(regnskap, HttpStatus.OK)
            } else {
                ResponseEntity<Any>(regnskap.createJenaResponse(jenaType), HttpStatus.OK)
            }

        } catch (e: Exception) {
            LOGGER.error("getRegnskapById failed:", e)
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
}