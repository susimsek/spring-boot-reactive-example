package io.github.susimsek.springbootreactiveexample.config

import io.github.susimsek.springbootreactiveexample.config.Constants.SPRING_PROFILE_DEVELOPMENT
import io.github.susimsek.springbootreactiveexample.config.h2.H2ConfigurationHelper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.sql.SQLException
import java.time.Clock
import java.time.Instant
import java.util.*

@Configuration(proxyBeanMethods = false)
@EnableR2dbcRepositories(basePackages = ["io.github.susimsek.springbootreactiveexample.repository"])
@EnableTransactionManagement
@EnableR2dbcAuditing(dateTimeProviderRef = "dateTimeProvider")
class DatabaseConfig {

    private val log = LoggerFactory.getLogger(DatabaseConfig::class.java)

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun dateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider {
        Optional.of(Instant.now(clock))
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(SPRING_PROFILE_DEVELOPMENT)
    @Throws(SQLException::class)
    fun h2TCPServer(environment: Environment): Any {
        val port = getValidPortForH2(environment)
        log.debug("Starting H2 database on port {}", port)
        return H2ConfigurationHelper.createServer(port)
    }

    private fun getValidPortForH2(env: Environment): String {
        var port = env.getProperty("server.port")?.toIntOrNull() ?: 8080
        port = when {
            port < 10000 -> 10000 + port
            port < 63536 -> port + 2000
            else -> port - 2000
        }
        return port.toString()
    }
}
