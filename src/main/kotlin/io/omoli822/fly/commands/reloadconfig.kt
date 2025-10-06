package io.omoli822.fly.commands
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ReloadConfig(private val plugin: JavaPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("fly.reload")) {
            sender.sendMessage("§cYou do not have permission to run this command!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§eUsage: /flyreloadconfig")
            return true
        }

        // Combine all args into one message
        val newMessage = args.joinToString(" ")
        plugin.reloadConfig()


        sender.sendMessage("§aRELOADED CONFIG OF FLY!")
        return true
    }
}
