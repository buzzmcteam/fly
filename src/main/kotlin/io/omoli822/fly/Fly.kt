package io.omoli822.fly

import org.bukkit.plugin.java.JavaPlugin

class Fly : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Fly plugin loaded successfully!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Fly plugin unloaded successfully!")
        logger.info("it worked")
    }
}
