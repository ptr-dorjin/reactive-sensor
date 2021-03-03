package pd.reactive.sensor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SensorApplication

fun main(args: Array<String>) {
    runApplication<SensorApplication>(*args)
}
