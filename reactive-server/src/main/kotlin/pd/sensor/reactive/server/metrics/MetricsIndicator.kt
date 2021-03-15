package pd.sensor.reactive.server.metrics

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MetricsIndicator : ReactiveHealthIndicator {

    var counter = 0

    override fun health(): Mono<Health> {
        return Mono.just(
            Health.Builder().up()
                .withDetail("Data counter", counter)
                .build()
        )
    }
}