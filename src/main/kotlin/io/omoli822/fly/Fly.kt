package io.omoli822.fly

import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.commands.ReloadConfig
import io.omoli822.fly.listeners.MyPlayerListener
import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    lateinit var welcomeMessageTemplate: String
    var featureEnabled: Boolean = false

    override fun onEnable() {
        // Save default config.yml if it doesn't exist
        saveResource("config.yml", false)
        saveDefaultConfig()
        logger.info("Fly plugin enabled")

        // Load config values
        welcomeMessageTemplate = config.getString("settings.message_at_join") ?: "Welcome!"
        featureEnabled = config.getBoolean("settings.enable_feature")

        // Register commands
        registerCommands()

        // Register listeners
        server.pluginManager.registerEvents(MyPlayerListener(this), this)
    }

    private fun registerCommands() {
        getCommand("setjoinmessage")?.setExecutor(AdminConfigCommand(this))
        getCommand("ReloadConfig")?.setExecutor(ReloadConfig(this))
        logger.info("Commands loaded successfully!")
    }
// commands /flyreloadconfig reloads the fly config
// commands /setjoinmessage <message> sets fly join message



    override fun onDisable() {
        logger.info("Fly plugin unloaded successfully!")
    }
}
