package pd.sensor.reactive.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SensorR2dbcApplication

fun main(args: Array<String>) {
    runApplication<SensorR2dbcApplication>(*args)
}
