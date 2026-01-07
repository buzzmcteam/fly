package io.omoli822.fly

import io.omoli822.fly.commands.AdminConfigCommand
import io.omoli822.fly.commands.ReloadConfig
import io.omoli822.fly.commands.GuiOfEco // <-- NEW IMPORT
import io.omoli822.fly.listeners.MyPlayerListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.mineacademy.fo.plugin.SimplePlugin;

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

        // Register event listeners (including the GuiOfEco listener)
        registerListeners() // <-- Use a separate method for clarity
    }

    private fun registerCommands() {
        // Existing commands
        getCommand("setjoinmessage")?.setExecutor(AdminConfigCommand(this))
        getCommand("flyreloadconfig")?.setExecutor(ReloadConfig(this))

        // --- NEW COMMAND REGISTRATION ---
        val ecoGuiExecutor = GuiOfEco(this, economy!!) // The GuiOfEco class requires the plugin and the non-null Economy
        getCommand("admintools")?.setExecutor(ecoGuiExecutor) // Register a command like /admintools
        // --------------------------------

        logger.info("⚙️ Commands loaded successfully!")
    }

    private fun registerListeners() {
        // Existing listener
        server.pluginManager.registerEvents(MyPlayerListener(this), this)

        // --- NEW LISTENER REGISTRATION ---
        // GuiOfEco must also be registered as a listener for its InventoryClickEvents to fire
        val ecoGuiListener = GuiOfEco(this, economy!!)
        // Note: We should ideally pass the same *instance* used for the command registration,
        // but since it only holds configuration/state and not user data, re-instantiation here is technically fine.
        // A better approach is often to handle listener registration inside the registerCommands block
        // and reuse the 'ecoGuiExecutor' variable. Let's do that for maximum efficiency.

        // Re-using the instance from registerCommands for efficiency:
        val ecoGuiInstance = getCommand("admintools")?.executor as? GuiOfEco
        if (ecoGuiInstance != null) {
            server.pluginManager.registerEvents(ecoGuiInstance, this)
            logger.info("⚡ GuiOfEco listener registered.")
        } else {
            logger.warning("⚠️ Could not retrieve GuiOfEco command executor for listener registration. Inventory events may fail.")
        }
    }

    private fun setupEconomy(): Boolean {
        val vault = server.pluginManager.getPlugin("Vault") ?: return false
        val rsp: RegisteredServiceProvider<Economy> =
            server.servicesManager.getRegistration(Economy::class.java) ?: return false

        // Check if a logging call is reachable. This logger.info was unreachable in your original code.
        logger.info("Vault found. Attempting to hook economy provider.")

        economy = rsp.provider
        return economy != null
    }

    fun getEconomy(): Economy? = economy

    override fun onDisable() {
        logger.info("🪶 Fly plugin unloaded successfully!")
    }
}