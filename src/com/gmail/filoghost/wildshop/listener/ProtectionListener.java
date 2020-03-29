/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.wildshop.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import com.gmail.filoghost.wildshop.Database;
import com.gmail.filoghost.wildshop.ShopObject;
import com.gmail.filoghost.wildshop.SignLocation;
import com.gmail.filoghost.wildshop.WildShop;

public class ProtectionListener implements Listener {
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDestroy(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();
		
		if (brokenBlock.getType() == Material.CHEST) {
			ShopObject shop = WildShop.getShopFromChest(brokenBlock);
			if (shop != null) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("§cPer distruggere il negozio elimina il cartello.");
			}
			
		} else if (brokenBlock.getType() == Material.WALL_SIGN) {
			ShopObject shop = Database.fromSignBlock(brokenBlock);
			if (shop != null) {
				if (WildShop.error) {
					event.getPlayer().sendMessage(WildShop.errorMessage);
					event.setCancelled(true);
					return;
				}
				
				Database.unregisterShop(new SignLocation(event.getBlock()));
				if (shop.isOwner(event.getPlayer())) {
					event.getPlayer().sendMessage("§aHai distrutto il tuo negozio!");
				} else {
					event.getPlayer().sendMessage("§aHai distrutto il negozio di " + shop.getOwnerName() + "!");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		Block blockPlaced = event.getBlockPlaced();
		
		if (blockPlaced.getType() != Material.CHEST) {
			return;
		}
		
		for (BlockFace face : WildShop.CARDINAL_DIRECTIONS) {
			Block relative = blockPlaced.getRelative(face);
			
			if (relative.getType() == Material.CHEST) {
			
				ShopObject existingNearShop = WildShop.getShopFromChest(relative);
			
				if (existingNearShop != null && existingNearShop.isOwner(event.getPlayer())) {
					event.getPlayer().sendMessage("§cNon puoi piazzare la cassa vicino ad un negozio.");
					event.setCancelled(true);
				}
			}
		}
	}


	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		ShopObject shop = WildShop.getShopFromInventory(event.getInventory());
		if (shop != null) {
			Player player = (Player) event.getPlayer();
			
			if (WildShop.error) {
				player.sendMessage(WildShop.errorMessage);
				event.setCancelled(true);
				return;
			}
			
			if (shop.isOwner(player.getUniqueId()) || player.hasPermission(WildShop.OPEN_PERM)) {
				// Autorizzato
			} else {
				player.sendMessage("§cNon puoi aprire il negozio degli altri (usa il cartello per aprirlo)");
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMove(InventoryMoveItemEvent event) {
		if (WildShop.isShopInventory(event.getSource()) || WildShop.isShopInventory(event.getDestination())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			return;
		}
		
		ShopObject shop = WildShop.getShopFromInventory(event.getInventory());
		
		if (shop != null) {
			if (!shop.getItem().isSimilar(event.getCurrentItem())) {
				((Player) event.getWhoClicked()).sendMessage("§cNon puoi mettere altri materiali nel negozio!");
				event.setCancelled(true);
			}
		}
	}
}
