package oauth2

import org.bukkit.Bukkit
import fabric.io.JsonParser

object Config:

  def plugin = Bukkit.getPluginManager.getPlugin("oauth2")

  def getHost = plugin.getConfig.getString("host")
  def getPort = plugin.getConfig.getInt("port")
  def getJson =
    val path = plugin.getConfig.getString("googlecreds")
    val source = scala.io.Source.fromFile(path).getLines().mkString("\n")
    JsonParser(source)("installed")
  def getClientId = getJson("client_id").asString
  def getClientSecret = getJson("client_secret").asString
