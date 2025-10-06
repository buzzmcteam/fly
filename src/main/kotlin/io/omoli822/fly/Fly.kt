package io.omoli822.fly
import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.listeners.MyPlayerListener
import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    lateinit var welcomeMessageTemplate: String
    var featureEnabled: Boolean = false

    override fun onEnable() {
        // Copy default config.yml from JAR if it doesn't exist
        saveResource("config.yml", false)
        logger.info("Fly plugin enabled")

        registerCommands()
        // Load config values
        welcomeMessageTemplate = config.getString("settings.message_at_join") ?: "Welcome!"
        featureEnabled = config.getBoolean("settings.enable_feature")

        // Register the listener
        server.pluginManager.registerEvents(MyPlayerListener(this), this)

    }
    private fun registerCommands() {
        getCommand("setjoinmessage")?.setExecutor(AdminConfigCommand(this))
        logger.info("commands loaded")
        logger.info("Fly plugin loaded successfully! Have a nice time, admin or owner")
    }




    override fun onDisable() {
        logger.info("Fly plugin unloaded successfully!")
    }
}
