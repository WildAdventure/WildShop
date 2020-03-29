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

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import lombok.val;
import wild.api.config.PluginConfig;

public class Database {

	private static Map<SignLocation, ShopObject> shops;
	private static PluginConfig fileConfig;

	public static void load() throws IOException, InvalidConfigurationException {
		shops = Maps.newHashMap();
		fileConfig = new PluginConfig(WildShop.instance, "database.yml");
		
		for (String key : fileConfig.getKeys(false)) {
			try {
				SignLocation shopLocation = SignLocation.deserialize(key);
				ConfigurationSection shopSection = fileConfig.getConfigurationSection(key);
				UUID owner = UUID.fromString(shopSection.getString("owner"));
				double price = shopSection.getDouble("price");
				ItemStack item = ItemStack.deserialize(shopSection.getConfigurationSection("item").getValues(true));
				shops.put(shopLocation, new ShopObject(owner, price, item));
				
			} catch (Exception e) {
				WildShop.instance.getLogger().log(Level.SEVERE, "Errore durante il caricamento dello shop " + key, e);
			}
		}
	}
	
	public static boolean save() throws IOException {
		
		// Mai salvare se c'è un errore
		if (WildShop.error) {
			return false;
		}
		
		for (val entry : shops.entrySet()) {
			SignLocation shopLocation = entry.getKey();
			ShopObject shopObject = entry.getValue();
			ConfigurationSection shopSection = fileConfig.createSection(shopLocation.serialize());
			shopSection.set("owner", shopObject.getOwner().toString());
			shopSection.set("price", shopObject.getPricePerItem());
			Map<String, Object> serializedItem = shopObject.getItem().serialize();
			serializedItem.remove("amount"); // non necessario
			shopSection.set("item", serializedItem);
		}
		
		fileConfig.save();
		return true;
	}
	
	public static int howManyShops(UUID owner) {
		int shopsAmount = 0;
		for (ShopObject shop : shops.values()) {
			if (shop.isOwner(owner)) {
				shopsAmount++;
			}
		}
		return shopsAmount;
	}
	
	public static Map<SignLocation, ShopObject> getOwnedShops(UUID owner) {
		Map<SignLocation, ShopObject> ownedShops = Maps.newHashMap();
		for (val entry : shops.entrySet()) {
			if (entry.getValue().isOwner(owner)) {
				ownedShops.put(entry.getKey(), entry.getValue());
			}
		}
		return ownedShops;
	}
	
	public static ShopObject fromSignBlock(Block block) {
		return shops.get(new SignLocation(block));
	}
	
	public static void trySave() {
		try {
			if (save()) {
				WildShop.instance.getLogger().info("Database negozi salvato.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			WildShop.instance.getLogger().severe("Impossibile salvare il database!");
		}
	}

	public static void registerShop(SignLocation location, ShopObject shop) {
		shops.put(location, shop);
	}

	public static void unregisterShop(SignLocation location) {
		ShopObject shop = shops.remove(location);
		if (shop != null) {
			shop.setDestroyed(true);
			fileConfig.set(location.serialize(), null);
		}
	}

	public static Map<SignLocation, ShopObject> getAllShops() {
		return shops;
	}


}
