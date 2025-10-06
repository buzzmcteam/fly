package io.omoli822.fly.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class ReloadConfig(private val plugin: JavaPlugin) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        // Permission check
        if (!sender.hasPermission("fly.reload")) {
            sender.sendMessage("§cYou do not have permission to run this command!")
            return true
        }

        // Reload the plugin config
        plugin.reloadConfig()
        sender.sendMessage("§aFly plugin config reloaded successfully!")

        return true
    }
}
