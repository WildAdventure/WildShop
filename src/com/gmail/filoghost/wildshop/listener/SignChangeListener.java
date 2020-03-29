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

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.wildshop.Database;
import com.gmail.filoghost.wildshop.ShopObject;
import com.gmail.filoghost.wildshop.SignLocation;
import com.gmail.filoghost.wildshop.WildShop;
import com.gmail.filoghost.wildshop.WildTownsBridge;
import com.gmail.filoghost.wildshop.utils.MaterialUtils;
import com.gmail.filoghost.wildshop.utils.NumberUtils;

import wild.api.translation.Translation;

public class SignChangeListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		
		if (event.getBlock().getType() == Material.WALL_SIGN && event.getLine(0).equalsIgnoreCase("[shop]")) {

			Block attachedTo = WildShop.getBlockAttachedTo(event.getBlock());
			
			if (attachedTo.getType() == Material.CHEST) {
				
				if (!WildShop.ALLOWED_WORLDS.contains(attachedTo.getWorld().getName())) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cPuoi creare negozi solo nel mondo città o nelle zone apposite.");
					return;
				}
				
				Inventory chestInventory = ((Chest) attachedTo.getState()).getInventory(); // Se la cassa è doppia l'inventario sarà giusto
				ItemStack firstItem = chestInventory.getItem(0);
				
				if (!WildTownsBridge.canBuildShop(event.getPlayer(), chestInventory)) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cLa cassa deve trovarsi in territori dove puoi costruire.");
					return;
				}
				
				if (WildShop.getShopFromChest(attachedTo) != null) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cC'è già un negozio qui!");
					return;
				}
				
				if (Database.howManyShops(event.getPlayer().getUniqueId()) >= WildShop.MAX_SHOPS) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cHai già creato troppi negozi, il limite è " + WildShop.MAX_SHOPS + ". Puoi vedere i tuoi negozi con il comando §e/wildshop list");
					return;
				}
				
				if (event.getLine(2) != null && !event.getLine(2).isEmpty()) {
					event.getPlayer().sendMessage("§cLa terza riga deve essere vuota, il prezzo deve essere nella seconda riga.");
					return;
				}

				String priceString = event.getLine(1);
				
				if (firstItem == null || firstItem.getType() == Material.AIR) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cDevi inserire l'oggetto che vuoi vendere nel primo slot della cassa.");
					return;
				}
				
				if (priceString == null || priceString.isEmpty()) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cDevi inserire il prezzo nella seconda riga.");
					return;
				}

				double price;
				
				try {
					String formattedPriceString = priceString.trim().replace("$", "").replace(",", ".");
					price = Double.parseDouble(formattedPriceString); // Prima vede se il numero è valido, dopo controlla le cifre decimali
					
					if (formattedPriceString.contains(".") && formattedPriceString.substring(formattedPriceString.indexOf('.') + 1).length() > 2) {
						event.setCancelled(true);
						event.getPlayer().sendMessage("§cPuoi inserire massimo 2 cifre decimali.");
						return;
					}
					
				} catch (NumberFormatException ex) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cNumero non valido: " + priceString.replace("$", "") + ".");
					return;
				}
				
				price = NumberUtils.roundTwoDecimals(price);
				
				if (price <= 0) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cPrezzo non valido: deve essere maggiore di zero.");
					return;
				}
				
				if (price > 100000) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cIl prezzo massimo è $100.000.");
					return;
				}
				
				for (int i = 0; i < chestInventory.getSize(); i++) {
					ItemStack current = chestInventory.getItem(i);
					if (current != null && !firstItem.isSimilar(current)) {
						event.setCancelled(true);
						event.getPlayer().sendMessage("§cPuoi vendere un solo tipo di oggetto, e deve essere posizionato nel primo slot della cassa.");
						return;
						
					}
				}

				UUID owner = event.getPlayer().getUniqueId();
				String ownerName = event.getPlayer().getName();
				
				// Setta le 4 righe
				event.setLine(0, WildShop.SIGN_BUY);
				event.setLine(1, MaterialUtils.cutTo(Translation.of(firstItem.getType()), 16));
				event.setLine(2, NumberUtils.format(price) + "$");
				event.setLine(3, ownerName);
			
				Database.registerShop(new SignLocation(event.getBlock()), new ShopObject(owner, price, firstItem));
				event.getPlayer().sendMessage("§aHai creato un negozio che vende §6" + Translation.of(firstItem.getType()) + "§a per §6" + price + "$§a.");
				event.getPlayer().sendMessage("§4Attenzione! §cIl negozio può essere distrutto da chiunque possa costruire in questo territorio. Assicurati che sia in territorio protetto.");
				
			} else if (attachedTo.getType() == Material.TRAPPED_CHEST) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("§cNon puoi usare casse trappola, devi usare casse normali.");

			} else {
				event.getPlayer().sendMessage("§cImpossibile trovare una cassa vicino! Il cartello va messo direttamente sulla cassa.");
			}
		}
	}

}
