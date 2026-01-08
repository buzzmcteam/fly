package io.omoli822.fly

import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.commands.ReloadConfig
import io.omoli822.fly.commands.GuiOfEco
import io.omoli822.fly.listeners.MyPlayerListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    private var economy: Economy? = null
    private var ecoGuiInstance: GuiOfEco? = null

    lateinit var welcomeMessageTemplate: String
    var featureEnabled: Boolean = false

    override fun onEnable() {
        saveDefaultConfig()
        logger.info("🪶 Fly plugin enabled!")

        if (!setupEconomy()) {
            logger.severe("❌ Vault not found or no economy plugin linked! Disabling Fly.")
            server.pluginManager.disablePlugin(this)
            return
        }

        loadConfigValues()
        registerCommands()
        registerListeners()

        logger.info("⚙️ Commands and listeners loaded successfully!")
    }

    override fun onDisable() {
        logger.info("🪶 Fly plugin unloaded successfully!")
    }

    private fun loadConfigValues() {
        welcomeMessageTemplate =
            config.getString("settings.message_at_join") ?: "Welcome!"
        featureEnabled =
            config.getBoolean("settings.enable_feature", false)
    }

    private fun registerCommands() {
        ecoGuiInstance = GuiOfEco(this, economy!!)


        getCommand("flyreload")?.setExecutor(ReloadConfig(this))
        getCommand("flyadmin")?.setExecutor(AdminConfigCommand(this))
        getCommand("flyeco")?.setExecutor(ecoGuiInstance)

        logger.info("📦 Commands registered.")
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(MyPlayerListener(this), this)

        ecoGuiInstance?.let {
            server.pluginManager.registerEvents(it, this)
            logger.info("⚡ GuiOfEco listener registered.")
        }
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) return false

        val rsp = server.servicesManager
            .getRegistration(Economy::class.java) ?: return false

        economy = rsp.provider
        return true
    }

    fun getEconomy(): Economy? = economy
}
