package client

import com.fasterxml.jackson.databind.JsonNode
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate

class ExampleController {

    KeycloakRestTemplate keycloakRestTemplate
    
    def index() {
        render contentType: 'application/json', encoding: 'UTF-8', 
            text: keycloakRestTemplate.getForObject("http://localhost:8081/resource", JsonNode.class)
    }
}
