package pd.sensor.reactive.device

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("sensor.server")
data class ServerProperties(val url: String)