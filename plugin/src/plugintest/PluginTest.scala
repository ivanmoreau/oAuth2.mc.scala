package plugintest

import org.bukkit.plugin.java.JavaPlugin

class PluginTest extends JavaPlugin:
  override def onEnable(): Unit =
    getLogger.info("Hello, world!")

  override def onDisable(): Unit =
    getLogger.info("Goodbye, world!")
