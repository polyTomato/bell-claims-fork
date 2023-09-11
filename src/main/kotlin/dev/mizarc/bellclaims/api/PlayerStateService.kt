package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

interface PlayerStateService {
    fun isPlayerRegistered(player: OfflinePlayer): Boolean
    fun getAllOnline(): Set<PlayerState>
    fun getById(id: UUID): PlayerState?
    fun getByPlayer(player: OfflinePlayer): PlayerState?
    fun registerPlayer(player: Player)
    fun unregisterPlayer(player: OfflinePlayer)
}