package io.omoli822.fly.commands

import org.bukkit.ban.ProfileBanList
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scheduler.BukkitRunnable
import io.omoli822.fly.Fly
import org.bukkit.ban.IpBanList
import org.bukkit.BanList
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID // Import UUID for explicit type handling

// Helper function to create an ItemStack with a display name and lore
fun createGuiItem(material: Material, name: String, lore: List<String>): ItemStack {
    val item = ItemStack(material)
    val meta: ItemMeta = item.itemMeta ?: Bukkit.getItemFactory().getItemMeta(material)!!
    meta.setDisplayName(name)
    meta.lore = lore
    item.itemMeta = meta
    return item
}

class GuiOfEco(private val plugin: Fly, private val economy: Economy) : CommandExecutor, Listener {

    private val guiTitle = "${ChatColor.GREEN}Player Hub & Stats"
    private val INVENTORY_SIZE = 45
    private val inventories = mutableMapOf<Player, Inventory>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // 1. CHECK IF SENDER IS A PLAYER (MUST BE FIRST)
        if (sender !is Player) {
            sender.sendMessage("Only players can open this GUI!")
            return true
        }

        val player = sender

        // 2. CHECK PERMISSION (Now that we know 'player' is valid)
        if (!player.hasPermission("fly.ecoadmin"))  {
            player.sendMessage("${ChatColor.RED}You do not have permission to use the Eco Admin GUI.")
            return true
        }

        // --- GUI OPENING LOGIC ---
        val inv = Bukkit.createInventory(null, INVENTORY_SIZE, guiTitle)

        updateInventory(player, inv)
        player.openInventory(inv)
        inventories[player] = inv

        // Start a repeating task to update the inventory every second (20 ticks)
        object : BukkitRunnable() {
            override fun run() {
                // Cancel the task if the player closes this GUI or logs out
                if (!player.isOnline || player.openInventory.topInventory != inv) {
                    inventories.remove(player)
                    cancel()
                    return
                }
                updateInventory(player, inv)
            }
        }.runTaskTimer(plugin, 0L, 20L)

        return true
    }

    private fun updateInventory(player: Player, inv: Inventory) {
        inv.clear()

        // -----------------------------------------------------------------------------------
        // 1. STATS SECTION (Row 1)
        // -----------------------------------------------------------------------------------

        // Player Head & Basic Stats (Slot 4)
        val statsItem = ItemStack(Material.PLAYER_HEAD)
        val statsMeta = statsItem.itemMeta as? SkullMeta
        if (statsMeta != null) {
            statsMeta.owningPlayer = player
            statsMeta.setDisplayName("${ChatColor.AQUA}${ChatColor.BOLD}• ${player.name}'s Profile ${ChatColor.BOLD}•")
            statsMeta.lore = listOf(
                "${ChatColor.YELLOW}» ${ChatColor.GRAY}Health: ${ChatColor.WHITE}%.1f/%.1f".format(player.health, player.maxHealth),
                "${ChatColor.YELLOW}» ${ChatColor.GRAY}Level: ${ChatColor.WHITE}${player.level}",
                "${ChatColor.YELLOW}» ${ChatColor.GRAY}Food: ${ChatColor.WHITE}${player.foodLevel}/20"
            )
            statsItem.itemMeta = statsMeta
        }
        inv.setItem(4, statsItem)


        // Fly Status (Slot 0)
        val flyStatusItem = createGuiItem(
            if (player.allowFlight) Material.FEATHER else Material.LEAD,
            "${ChatColor.LIGHT_PURPLE}Flight Status",
            listOf(if (player.isFlying) "${ChatColor.GREEN}Flying (Enabled)" else "${ChatColor.RED}Not Flying (Allowed: ${player.allowFlight})")
        )
        inv.setItem(0, flyStatusItem)

        // Gamemode (Slot 8)
        val gameModeName = player.gameMode.name.replace("_", " ").lowercase().capitalize() // Using lowercase() and capitalize()
        val gamemodeItem = createGuiItem(
            Material.COMPASS,
            "${ChatColor.LIGHT_PURPLE}Gamemode",
            listOf("${ChatColor.YELLOW}Current: ${ChatColor.WHITE}${gameModeName}")
        )
        inv.setItem(8, gamemodeItem)

        // -----------------------------------------------------------------------------------
        // 2. ECONOMY SECTION (Row 3)
        // -----------------------------------------------------------------------------------

        val balance = economy.getBalance(player)

        // Main Balance (Slot 21)
        val balanceItem = createGuiItem(
            Material.EMERALD,
            "${ChatColor.GOLD}${ChatColor.BOLD}Current Balance",
            listOf("${ChatColor.YELLOW}» ${ChatColor.AQUA}%.2f Coins".format(balance))
        )
        inv.setItem(21, balanceItem)

        // Deposit/Withdraw (Placeholders for future features)
        inv.setItem(20, createGuiItem(Material.CHEST, "${ChatColor.GREEN}Deposit", listOf("${ChatColor.GRAY}Click to access deposit options.")))
        inv.setItem(22, createGuiItem(Material.HOPPER, "${ChatColor.RED}Withdraw", listOf("${ChatColor.GRAY}Click to access withdrawal options.")))

        // -----------------------------------------------------------------------------------
        // 3. ADMINISTRATIVE STATUS (Row 5)
        // -----------------------------------------------------------------------------------

        // Ban Status (Slot 39)
        val banItem = ItemStack(Material.BARRIER)
        val banMeta = banItem.itemMeta!!
        banMeta.setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Ban Status")

        // FIX: Explicitly cast the BanList to a generic type that accepts UUID/OfflinePlayer
        @Suppress("UNCHECKED_CAST")
        val banList = plugin.server.getBanList(BanList.Type.PROFILE) as BanList<UUID>
        val isBanned = banList.isBanned(player.uniqueId)

        banMeta.lore = listOf(if (isBanned) "${ChatColor.RED}BANNED" else "${ChatColor.GREEN}Not Banned")
        banItem.itemMeta = banMeta
        inv.setItem(39, banItem)

        // OP Status (Slot 41)
        val opItem = createGuiItem(
            Material.REDSTONE_TORCH,
            "${ChatColor.DARK_RED}${ChatColor.BOLD}Operator Status",
            listOf(if (player.isOp) "${ChatColor.GREEN}OP" else "${ChatColor.RED}Not OP")
        )
        inv.setItem(41, opItem)


        // -----------------------------------------------------------------------------------
        // 4. FILLERS
        // -----------------------------------------------------------------------------------

        // Fill the rest of the inventory with glass panes
        val filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", emptyList())
        for (i in 0 until inv.size) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler)
            }
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (!inventories.containsKey(player) || event.inventory != inventories[player] || event.currentItem == null) return

        event.isCancelled = true

        val clickedItemType = event.currentItem?.type ?: return

        when (clickedItemType) {
            Material.EMERALD -> player.sendMessage("${ChatColor.GOLD}Your balance: ${ChatColor.AQUA}%.2f Coins".format(economy.getBalance(player)))
            Material.CHEST -> player.sendMessage("${ChatColor.YELLOW}Deposit functionality is coming soon!")
            Material.HOPPER -> player.sendMessage("${ChatColor.YELLOW}Withdraw functionality is coming soon!")
            Material.BARRIER -> player.sendMessage("${ChatColor.RED}Ban status is dynamically displayed.")
            Material.REDSTONE_TORCH -> player.sendMessage("${ChatColor.DARK_RED}Your OP status is: ${if (player.isOp) "OP" else "Not OP"}")
            Material.PLAYER_HEAD -> player.sendMessage("${ChatColor.AQUA}Profile information is updated automatically.")
            else -> {
                // Ignore clicks on filler items or other non-functional items
            }
        }
    }
}