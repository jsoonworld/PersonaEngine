package ai.vip.personaengine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class PersonaEngineApplication

fun main(args: Array<String>) {
	runApplication<PersonaEngineApplication>(*args)
}
