package io.omoli822.fly.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class AdminConfigCommand(private val plugin: JavaPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("fly.admin")) {
            sender.sendMessage("§cYou do not have permission to run this command!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§eUsage: /setjoinmessage <message>")
            return true
        }

        // Combine all args into one message
        val newMessage = args.joinToString(" ")

        // Update config
        plugin.config.set("settings.message_at_join", newMessage)
        plugin.saveConfig() // Saves to config.yml

        sender.sendMessage("§aJoin message updated successfully!")
        return true
    }
}
