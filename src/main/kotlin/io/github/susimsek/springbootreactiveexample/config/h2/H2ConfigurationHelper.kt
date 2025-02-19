package io.github.susimsek.springbootreactiveexample.config.h2

import org.slf4j.LoggerFactory
import java.sql.SQLException

object H2ConfigurationHelper {
    private val log = LoggerFactory.getLogger(H2ConfigurationHelper::class.java)

    @Suppress("TooGenericExceptionCaught")
    @Throws(SQLException::class)
    fun createServer(port: String): Any {
        return try {
            val loader = Thread.currentThread().contextClassLoader
            val serverClass = Class.forName("org.h2.tools.Server", true, loader)
            val createServer = serverClass.getMethod("createTcpServer", Array<String>::class.java)
            createServer.invoke(null, arrayOf("-tcp", "-tcpAllowOthers", "-tcpPort", port))
        } catch (e: Exception) {
            log.error("Failed to initialize H2 database server", e)
            throw IllegalStateException("Failed to initialize H2 database server", e)
        }
    }
}
