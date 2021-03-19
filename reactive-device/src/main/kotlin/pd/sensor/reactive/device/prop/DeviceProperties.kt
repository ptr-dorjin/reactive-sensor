package pd.sensor.reactive.device.prop

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("sensor.device")
data class DeviceProperties(val interval: Int)
