package client

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = "client")
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}