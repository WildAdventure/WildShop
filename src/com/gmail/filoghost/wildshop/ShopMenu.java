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
package com.gmail.filoghost.wildshop;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.wildshop.utils.InventoryUtils;
import com.gmail.filoghost.wildshop.utils.NumberUtils;
import com.google.common.collect.Lists;

import wild.api.bridges.EconomyBridge;
import wild.api.bridges.EconomyBridge.PlayerNotFoundException;
import wild.api.menu.Icon;
import wild.api.menu.IconBuilder;
import wild.api.menu.IconMenu;
import wild.api.menu.StaticIcon;
import wild.api.sound.EasySound;
import wild.api.translation.Translation;

public class ShopMenu extends IconMenu {
	
	private Block shopSign;
	private ShopObject shop;

	public ShopMenu(ShopObject shop, Block shopSign) {
		super("Negozio di " + shop.getOwnerName(), 5);
		this.shopSign = shopSign;
		this.shop = shop;
		
		ItemStack sellItemIcon = shop.getItem().clone();
		sellItemIcon.setAmount(1);
		ItemMeta meta = sellItemIcon.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = Lists.newArrayList();
		}
		lore.add(0, ChatColor.GRAY + "Prezzo individuale: " + ChatColor.GOLD + shop.getPricePerItem() + "$");
		meta.setLore(lore);
		sellItemIcon.setItemMeta(meta);
		
		setIcon(5, 2, new StaticIcon(sellItemIcon, false));
		
		setIcon(3, 4, buyOption(1,  Material.STAINED_GLASS_PANE, 4));
		setIcon(5, 4, buyOption(16, Material.STAINED_GLASS_PANE, 1));
		setIcon(7, 4, buyOption(64, Material.STAINED_GLASS_PANE, 14));
		
		refresh();
	}

	private Icon buyOption(int buyAmount, Material icon, int iconData) {
		double totalPrice = shop.getPricePerItem() * buyAmount;
		
		return new IconBuilder(icon).dataValue(iconData).amount(buyAmount).name(ChatColor.GREEN + "Compra " + buyAmount + " " + Translation.of(shop.getItem().getType())).lore(ChatColor.GRAY + "Prezzo: " + ChatColor.GOLD + NumberUtils.format(totalPrice) + "$").clickHandler(clicker -> {
			
			if (shop.isDestroyed()) {
				clicker.sendMessage("§cQuesto negozio non esiste più.");
				return;
			}
			
			if (shop.isOwner(clicker)) {
				clicker.sendMessage("§cNon puoi comprare dal tuo stesso negozio!");
				return;
			}
			
			Chest chestState = (Chest) WildShop.getBlockAttachedTo(shopSign).getState();
			Inventory shopInventory = chestState.getInventory();
					
			if (!InventoryUtils.containsAtLeast(shopInventory, shop.getItem(), buyAmount)) {
				clicker.sendMessage("§eIl negozio non ha " + buyAmount + " di questa merce.");
				return;
			}
						
			if (!EconomyBridge.takeMoney(clicker, totalPrice)) {
				clicker.sendMessage("§cNon hai abbastanza soldi.");
				return;
			}

			try {
				EconomyBridge.giveMoney(shop.getOwner(), totalPrice);
			} catch (PlayerNotFoundException e) {
				WildShop.instance.getLogger().log(Level.SEVERE, "Impossibile dare i soldi a " + shop.getOwnerName() + "/" + shop.getOwner() + ": banca del giocatore non trovata", e);
			}
					
			List<ItemStack> removedItems = InventoryUtils.remove(shopInventory, shop.getItem(), buyAmount);
			for (ItemStack removedItem : removedItems) {
				giveSafe(clicker, removedItem);
			}
			
			clicker.sendMessage("§7Hai comprato " + Translation.of(shop.getItem().getType()) + " x" + buyAmount + " da " + shop.getOwnerName() + ".");
			EasySound.quickPlay(clicker, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
		}).build();
	}
	
	private void giveSafe(Player to, ItemStack item) {
		HashMap<Integer, ItemStack> remaining = to.getInventory().addItem(item);
		if (remaining != null && remaining.size() > 0) {
			for (ItemStack i : remaining.values()) {
				to.getWorld().dropItem(to.getEyeLocation().add(to.getLocation().getDirection().multiply(0.5)), i);
			}
			to.sendMessage("§cNel tuo inventario non c'era abbastanza spazio e alcuni oggetti sono stati gettati a terra.");
			EasySound.quickPlay(to, Sound.BLOCK_NOTE_BASS);
			to.getOpenInventory().close();
		}
	}

}
