@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package dev.zlagi.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.zlagi.data.database.table.Blogs
import dev.zlagi.data.database.table.Tokens
import dev.zlagi.data.database.table.Users
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import java.net.URI
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class)
class DatabaseProvider : DatabaseProviderContract, KoinComponent {

    private val dispatcher: CoroutineContext

    init {
        dispatcher = newFixedThreadPoolContext(5, "database-pool")
    }

    override fun init() {
        Database.connect(hikari())
        transaction {
            create(Users)
            create(Blogs)
            create(Tokens)
        }
    }

/*    private fun hikari(): HikariDataSource {
        HikariConfig().run {
            driverClassName = driverClass
            jdbcUrl = "jdbc:postgresql://${host}:${port}/${dbname}"
            username = user
            password = dbpassword
            isAutoCommit = false
            maximumPoolSize = 5
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
            return HikariDataSource(this)
        }
    }

    companion object DatabaseConfig {
        const val driverClass = "org.postgresql.Driver"
        const val host = "localhost"
        const val port = 3500
        const val dbname = "blogfy"
        const val user = "postgres"
        const val dbpassword = "root"
    }*/
    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = System.getenv("JDBC_DRIVER")
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        val uri = URI(System.getenv("DATABASE_URL"))
        val username = uri.userInfo.split(":").toTypedArray()[0]
        val password = uri.userInfo.split(":").toTypedArray()[1]

        config.jdbcUrl =
            "jdbc:postgresql://" + uri.host + ":" + uri.port + uri.path + "?sslmode=require" + "&user=$username&password=$password"


        config.validate()

        return HikariDataSource(config)
    }

}

interface DatabaseProviderContract {
    fun init()
}