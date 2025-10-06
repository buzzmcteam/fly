package io.omoli822.fly

import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.commands.ReloadConfig
import io.omoli822.fly.listeners.MyPlayerListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    // Vault economy reference
    private var economy: Economy? = null

    // Config values
    lateinit var welcomeMessageTemplate: String
    var featureEnabled: Boolean = false

    override fun onEnable() {
        // Save default config if not existing
        saveResource("config.yml", false)
        saveDefaultConfig()

        logger.info("🪶 Fly plugin enabled!")

        // Setup Vault economy
        if (!setupEconomy()) {
            logger.severe("❌ Vault not found or no economy plugin linked! Disabling Fly.")
            server.pluginManager.disablePlugin(this)
            return
        } else {
            logger.info("✅ Vault economy hooked successfully!")
        }

        // Load config values
        welcomeMessageTemplate = config.getString("settings.message_at_join") ?: "Welcome!"
        featureEnabled = config.getBoolean("settings.enable_feature")

        // Register commands
        registerCommands()

        // Register event listeners
        server.pluginManager.registerEvents(MyPlayerListener(this), this)
    }

    private fun registerCommands() {
        getCommand("setjoinmessage")?.setExecutor(AdminConfigCommand(this))
        getCommand("flyreloadconfig")?.setExecutor(ReloadConfig(this))
        logger.info("⚙️ Commands loaded successfully!")
    }

    private fun setupEconomy(): Boolean {
        val vault = server.pluginManager.getPlugin("Vault") ?: return false
        val rsp: RegisteredServiceProvider<Economy> =
            server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return economy != null
    }

    fun getEconomy(): Economy? = economy

    override fun onDisable() {
        logger.info("🪶 Fly plugin unloaded successfully!")
    }
}
