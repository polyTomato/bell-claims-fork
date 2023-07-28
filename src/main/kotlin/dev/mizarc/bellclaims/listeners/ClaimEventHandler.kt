package dev.mizarc.bellclaims.listeners

import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.PartitionService
import dev.mizarc.bellclaims.BellClaims
import dev.mizarc.bellclaims.claims.ClaimPermissionRepository
import dev.mizarc.bellclaims.claims.ClaimRepository
import dev.mizarc.bellclaims.claims.ClaimRuleRepository
import dev.mizarc.bellclaims.claims.PlayerAccessRepository
import dev.mizarc.bellclaims.partitions.PartitionRepository
import dev.mizarc.bellclaims.players.PlayerStateRepository
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit

/**
 * Handles the registration of defined events with their associated actions.
 * @property plugin A reference to the plugin instance
 * @property claimContainer A reference to the ClaimContainer instance
 */
class ClaimEventHandler(var plugin: BellClaims,
                        val claims: ClaimRepository,
                        val partitions: PartitionRepository,
                        val claimRuleRepository: ClaimRuleRepository,
                        val claimPermissionRepository: ClaimPermissionRepository,
                        val playerAccessRepository: PlayerAccessRepository,
                        val playerStates: PlayerStateRepository,
                        val claimService: ClaimService,
                        val partitionService: PartitionService) : Listener {
    init {
        for (perm in ClaimPermission.values()) {
            for (e in perm.events) {
                registerEvent(e.eventClass, ::handleClaimPermission)
            }
        }
        for (rule in ClaimRule.values()) {
            for (r in rule.rules) {
                registerEvent(r.eventClass, ::handleClaimRule)
            }
        }
    }

    /**
     * A wrapper function to determine if an event has an appropriate RuleExecutor, and if so, uses it to determine
     * if the event happened inside of claim boundaries, then passes off its handling to the executor if those checks
     * pass.
     */
    private fun handleClaimRule(listener: Listener, event: Event) {
        val rule = ClaimRule.getRuleForEvent(event::class.java) ?: return // Get the rule to deal with this event
        val executor = ClaimRule.getRuleExecutorForEvent(event::class.java, rule) ?: return  // Get the executor from the rule that deals with this event
        val claims = executor.getClaims(event, claimService, partitionService) // Get all claims that this event affects
        if (claims.isEmpty()) return // Check if any claims are affected by the event
        for (claim in claims) { // If they are, check if they do not allow this event
            if (!claimRuleRepository.doesClaimHaveRule(claim, rule)) {
                executor.handler.invoke(event, claimService, partitionService) // If they do not, invoke the handler
                return
            }
        }
    }

    /**
     * A wrapper function to abstract the business logic of determining if an event occurs within a claim, if the
     * player that the event originated from has permissions within that claim, and if not, which permission event
     * executor has the highest priority, then invoke that executor.
     */
    private fun handleClaimPermission(listener: Listener, event: Event) {
        //Bukkit.getLogger().info("$event")
        val eventPerms = ClaimPermission.getPermissionsForEvent(event::class.java) // Get all ClaimPermissions that deal with this event

        // Get the top PermissionExecutor that deals with this event.
        // NOTE: This assumes that any PermissionExecutor that deals with this event will always return the same values
        // for location and player as any other for this event would
        val tempExecutor = ClaimPermission.getPermissionExecutorForEvent(event::class.java) ?: return

        val player: Player = tempExecutor.source.invoke(event) ?: return // The player that caused this event, if any
        val location: Location = tempExecutor.location.invoke(event) ?: return // If no location was found, do nothing

        // Determine if this event happened inside a claim's boundaries
        val partition = partitionService.getByLocation(location) ?: return
        val claim = claims.getById(partition.claimId) ?: return

        // If player has override, do nothing.
        val playerState = playerStates.get(player)
        if (playerState!!.claimOverride) {
            return
        }

        // If player is owner, do nothing.
        if (player.uniqueId == claim.owner.uniqueId) {
            return
        }

        // Get the claim permissions to use, whether it's the trustee's individual permissions, or the claim's default permissions
        var playerPermissions = playerAccessRepository.getByPlayerInClaim(claim, player)
        if (playerPermissions.isEmpty()) {
            playerPermissions = claimPermissionRepository.getByClaim(claim)
        }

        for (perm in eventPerms) {
            if (playerPermissions.contains(perm)) return
        }

        var executor: ((l: Listener, e: Event) -> Boolean)? = null // The function that handles the result of this event

        // Determine if the claim permissions contains any of the parent permissions to this one
        fun checkPermissionParents(permission: ClaimPermission): Boolean {
            var permissionRef: ClaimPermission? = permission
            while (permissionRef?.parent != null) {
                if (playerPermissions.contains(permissionRef.parent)) {
                    return true
                }
                permissionRef = permissionRef.parent
            }
            return false
        }

        // Determine the highest priority permission for the event and sets the executor to the one found, if any
        for (e in eventPerms) {
            if (!checkPermissionParents(e)) { // First check if claimPerms does not contain the parent of this permission
                if (!playerPermissions.contains(e)) { // If not, check if it does not contain this permission
                    for (ee in e.events) { // If so, determine the executor to use
                        if (ee.eventClass == event::class.java) {
                            executor = ee.handler
                            // If nothing was executed then the player has permissions to enact this event, so do not send a warning.
                            if (executor.invoke(listener, event)) {
                                player.sendActionBar(
                                    Component.text("You can't do that in ${claim.owner.name}'s claim!")
                                        .color(TextColor.color(255, 85, 85)))
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) =
        plugin.server.pluginManager.registerEvent(event, this, EventPriority.NORMAL, executor,
            plugin, true)
}
