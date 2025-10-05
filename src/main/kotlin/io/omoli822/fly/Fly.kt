package io.omoli822.fly

import io.omoli822.fly.listeners.MyPlayerListener
import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    lateinit var welcomeMessageTemplate: String
    var featureEnabled: Boolean = false

    override fun onEnable() {
        saveDefaultConfig()

        // Load config values
        welcomeMessageTemplate = config.getString("settings.message_at_join") ?: "Welcome!"
        featureEnabled = config.getBoolean("settings.enable_feature")

        // Register the listener
        server.pluginManager.registerEvents(MyPlayerListener(this), this)

        logger.info("Fly plugin loaded successfully!")
    }

    override fun onDisable() {
        logger.info("Fly plugin unloaded successfully!")
    }
}
