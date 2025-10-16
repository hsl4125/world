package com.aboveland.api.config

import com.typesafe.config.{Config, ConfigFactory}

case class ServerConfig(host: String, port: Int)
case class DatabaseConfig(url: String, username: String, password: String, driver: String)
case class JwtConfig(secret: String, expiration: String)

case class AppConfig(
  server: ServerConfig,
  database: DatabaseConfig,
  jwt: JwtConfig
)

object AppConfig {
  
  def load(): AppConfig = {
    val config = ConfigFactory.load()
    AppConfig(
      server = ServerConfig(
        host = config.getString("app.server.host"),
        port = config.getInt("app.server.port")
      ),
      database = DatabaseConfig(
        url = config.getString("app.database.url"),
        username = config.getString("app.database.username"),
        password = config.getString("app.database.password"),
        driver = config.getString("app.database.driver")
      ),
      jwt = JwtConfig(
        secret = config.getString("app.jwt.secret"),
        expiration = config.getString("app.jwt.expiration")
      )
    )
  }
}
