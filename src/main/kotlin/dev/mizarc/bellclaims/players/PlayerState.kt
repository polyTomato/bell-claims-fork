package dev.mizarc.bellclaims.players

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.Config
import dev.mizarc.bellclaims.claims.Claim
import dev.mizarc.bellclaims.partitions.Position3D
import org.apache.commons.lang.mutable.Mutable
import java.util.*

class PlayerState(val player: OfflinePlayer, val config: Config, val metadata: Chat) {
    var claimOverride = false
    var claimToolMode = 0
    var isHoldingClaimTool = false
    var isVisualisingClaims = false
    var visualisedBlockPositions: MutableMap<Claim, MutableSet<Position3D>> = mutableMapOf()

    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }

    fun getClaimLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_limit", config.claimLimit)
            .takeIf { it > -1 } ?: -1

    fun getClaimBlockLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_block_limit", config.claimBlockLimit)
            .takeIf { it > -1 } ?: -1
}