package oauth2

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.entity.Player

import org.bukkit.event.player.PlayerQuitEvent
import oauth2.commands.Login

case class PositionPlayer(
    val id: Int,
    val x: Double,
    val y: Double,
    val z: Double
)
case class PlayerDb(val name: String, val id: Int)
case class LoggedPlayer(val id: Int, val logged: Boolean)
case class GoogleId(val id: Int, val googleId: String)

class Auth extends JavaPlugin with Listener:
  override def onEnable(): Unit =
    this.saveDefaultConfig()
    getServer.getPluginManager.registerEvents(new Joined, this)
    this.getCommand("login").setExecutor(new Login)
    Service.run()

  override def onDisable(): Unit =
    getLogger.info("Goodbye, world!")

class Joined extends Listener:
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit =
    event.getPlayer.logout
    if event.getPlayer.isNewPlayer then
      Queries.makePlayer(event.getPlayer.getName())
    event.getPlayer.disableActions

  @EventHandler
  def onPlayerQuit(event: PlayerQuitEvent): Unit =
    if event.getPlayer.isLogged then
      event.getPlayer.saveLocationToDB
      event.getPlayer.logout
