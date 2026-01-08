package io.omoli822.fly

import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.commands.ReloadConfig
import io.omoli822.fly.commands.GuiOfEco
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

    // Keep a single instance of GuiOfEco so we can both set it as a command executor and register it as a listener
    private var ecoGuiInstance: GuiOfEco? = null

    override fun onEnable() {
        // Save default config if not existing
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

        // Register event listeners (including the GuiOfEco listener)
        registerListeners()
    }

    private fun registerCommands() {
        try {
            // Create one instance of GuiOfEco and use it for both the command executor and listener registration.
            ecoGuiInstance = GuiOfEco(this, economy!!)

            // Ensure these command names exist in plugin.yml
            getCommand("ecoadmin")?.setExecutor(ecoGuiInstance)
            getCommand("adminconfig")?.setExecutor(AdminConfigCommand(this))
            getCommand("reloadconfig")?.setExecutor(ReloadConfig(this))
        } catch (e: Exception) {
            logger.severe("Oh no! There was an error while registering the commands: ${e.message}")
            e.printStackTrace()
        }

        logger.info("⚙️ Commands loaded successfully!")
    }

    private fun registerListeners() {
        // Existing listener
        server.pluginManager.registerEvents(MyPlayerListener(this), this)

        // Register the GuiOfEco instance (if created)
        ecoGuiInstance?.let {
            server.pluginManager.registerEvents(it, this)
            logger.info("⚡ GuiOfEco listener registered.")
        } ?: run {
            logger.warning("⚠️ Could not retrieve GuiOfEco command executor for listener registration. Inventory events may fail.")
        }
    }

    private fun setupEconomy(): Boolean {
        val vault = server.pluginManager.getPlugin("Vault") ?: return false
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false

        logger.info("Vault found. Attempting to hook economy provider.")

        economy = rsp.provider
        return economy != null
    }

    fun getEconomy(): Economy? = economy

    override fun onDisable() {
        logger.info("🪶 Fly plugin unloaded successfully!")
    }
} 
