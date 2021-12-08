package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims


@CommandAlias("unclaim")
class UnclaimCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @Default
    fun onUnclaim(player: Player) {
        onPartition(player)
    }

    @Subcommand("partition")
    fun onPartition(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return
        }

        plugin.claimContainer.removeClaimPartition(claimPartition)
        player.sendMessage("This claim partition has been removed")
    }

    @Subcommand("connected")
    fun onConnected(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        if (claimPartition == null) {
            player.sendMessage("There is no claim at your current location.")
            return
        }

        val claim = claimPartition.claim
        plugin.claimContainer.removePersistentClaim(claim)


        player.sendMessage("The entire claim has been removed.")
    }
}