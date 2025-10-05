package io.omoli822.fly.listeners

import io.omoli822.fly.Fly
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class MyPlayerListener(private val plugin: Fly) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Get message from config and replace placeholder
        val message = plugin.welcomeMessageTemplate.replace("\${player.name}", player.name)

        // Send message if feature is enabled
        if (plugin.featureEnabled) {
            player.sendMessage(message)
        }
    }
}
