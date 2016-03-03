package service

import grails.converters.JSON
import org.springframework.security.core.context.SecurityContextHolder

class PrincipalController {

    def index() { 
        render SecurityContextHolder.context.authentication.principal as JSON
    }
}
