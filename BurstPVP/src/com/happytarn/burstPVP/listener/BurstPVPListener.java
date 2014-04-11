package com.happytarn.burstPVP.listener;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.happytarn.burstPVP.Main;
import com.happytarn.burstPVP.PVPData;

public class BurstPVPListener implements Listener {

	private Main plugin;

	public BurstPVPListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityDie(EntityDeathEvent event) {

		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}

		Player player = (Player) event.getEntity();

		if (plugin.getSankaList().containsKey(player.getName())) {
			plugin.getItemList().addAll(event.getDrops());
			event.getDrops().clear();
			plugin.getSankaList().remove(player.getName());
		}

		return;
	}

	@EventHandler
	public void onEntityDeath(PlayerDeathEvent event) {
		Player pDead = event.getEntity().getPlayer();
		if (event.getEntity() instanceof Player) {

			if(PVPData.isPlayer(pDead)){
				Player winner = PVPData.endGame(pDead,false,event.getDrops());
				plugin.setGame(false);
				event.getDrops().clear();
			}
		}
	}

	// @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (player.isOp()) {
			return;
		}

		if (block != null && block.getType() == Material.CHEST) {
			Location chestloc = block.getLocation();
			Location sublocation = null;
			if (block.getRelative(BlockFace.NORTH).getType() == Material.CHEST) {
				sublocation = block.getRelative(BlockFace.NORTH).getLocation();
			}
			if (block.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) {
				sublocation = block.getRelative(BlockFace.SOUTH).getLocation();
			}
			if (block.getRelative(BlockFace.EAST).getType() == Material.CHEST) {
				sublocation = block.getRelative(BlockFace.EAST).getLocation();
			}
			if (block.getRelative(BlockFace.WEST).getType() == Material.CHEST) {
				sublocation = block.getRelative(BlockFace.WEST).getLocation();
			}
			if (sublocation == null) {
				return;
			}
			if (plugin.isWinnerChestLoc(chestloc) || plugin.isWinnerChestLoc(sublocation)) {
				// チェストが報酬チェスト
				boolean lose = false;
				for (Player winner : plugin.getWinnerList()) {
					if (!winner.getName().equals(player.getName())) {
						// 勝利者じゃない場合
						lose = true;
					} else {
						return;
					}
				}

				if (lose) {
					event.setCancelled(true);
				}

			} else {
			}
		}
		return;
	}
}
