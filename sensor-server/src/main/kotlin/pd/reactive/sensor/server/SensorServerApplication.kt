package pd.reactive.sensor.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SensorServerApplication

fun main(args: Array<String>) {
    runApplication<SensorServerApplication>(*args)
}
