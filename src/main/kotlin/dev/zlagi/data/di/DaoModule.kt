package dev.zlagi.data.di

import dev.zlagi.data.dao.BlogsDao
import dev.zlagi.data.dao.TokenDao
import dev.zlagi.data.dao.UserDao
import dev.zlagi.data.database.DatabaseProvider
import dev.zlagi.data.database.DatabaseProviderContract
import dev.zlagi.data.database.table.Blogs
import dev.zlagi.data.database.table.Tokens
import dev.zlagi.data.database.table.Users
import org.koin.dsl.module

object DaoModule {
    val koinBeans = module {
        single<TokenDao> { Tokens }
        single<UserDao> { Users }
        single<BlogsDao> { Blogs }
        single<DatabaseProviderContract> { DatabaseProvider() }
    }
}