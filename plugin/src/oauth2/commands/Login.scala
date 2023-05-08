package oauth2.commands

import org.bukkit.command.CommandExecutor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import oauth2.enableActions

class Login extends CommandExecutor:

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean =
    if sender.isInstanceOf[Player] then
      val player = sender.asInstanceOf[Player]
      player.sendMessage("Go to http://localhost:8080/<your username>")
      true
    else false