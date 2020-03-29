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

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.wildshop.listener.BuySellListener;
import com.gmail.filoghost.wildshop.listener.ProtectionListener;
import com.gmail.filoghost.wildshop.listener.SignChangeListener;
import com.google.common.collect.Lists;

public class WildShop extends JavaPlugin {

	public static final String DESTROY_PERM = 				"wildshop.destroy";
	public static final String OPEN_PERM = 					"wildshop.open";
	public static final String SIGN_BUY = 					"§1§l[Compra]";
	
	public static final List<String> ALLOWED_WORLDS = 		Arrays.asList("world", "world_void");
	public static final int MAX_SHOPS = 					25;

	public static boolean error;
	public static String errorMessage = "§cErrore interno, il plugin è temporaneamente sospeso.";
	public static WildShop instance;

	public static final BlockFace[] CARDINAL_DIRECTIONS = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(new BuySellListener(), this);
		Bukkit.getPluginManager().registerEvents(new SignChangeListener(), this);
		Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
		
		getCommand("wildshop").setExecutor(new ShopCommand());
		
		try {
			Database.load();
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("Impossibile leggere il database! Il plugin verrà messo in modalità di sicurezza per evitare la distruzione dei negozi!");
			error = true;
		}
		
		try {
			WildTownsBridge.setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			List<SignLocation> deletedShops = Lists.newArrayList();
			
			for (SignLocation signLocation : Database.getAllShops().keySet()) {
				World world = Bukkit.getWorld(signLocation.getWorldName());
				
				if (world == null) {
					deletedShops.add(signLocation);
					Bukkit.getConsoleSender().sendMessage("§cCancellato negozio in un mondo non trovato: " + signLocation.getWorldName());
					continue;
				}
				
				if (world.getBlockAt(signLocation.getX(), signLocation.getY(), signLocation.getZ()).getType() != Material.WALL_SIGN) {
					deletedShops.add(signLocation);
					Bukkit.getConsoleSender().sendMessage("§cCancellato negozio che non aveva più un cartello: " + signLocation.serialize());
				}
			}
			
			if (!deletedShops.isEmpty()) {
				for (SignLocation deletedShop : deletedShops) {
					Database.unregisterShop(deletedShop);
				}
				Database.trySave();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("Impossibile controllare i cartelli!");
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				Database.trySave();
			}
		}.runTaskTimerAsynchronously(this, 5 * 60 * 20, 5 * 60 * 20);
	}

	@Override
	public void onDisable() {
		Database.trySave();
	}

	// Ottiene il cartello che controlla questo shop (se block appartiene ad uno shop). Può ritornare null.
	public static ShopObject getShopFromChest(Block block) {
		if (block.getType() != Material.CHEST) {
			throw new IllegalArgumentException("Not a chest: " + block);
		}
		
		for (BlockFace firstChestFace : CARDINAL_DIRECTIONS) {
			Block firstChestRelative = block.getRelative(firstChestFace);

			if (firstChestRelative.getType() == Material.WALL_SIGN && isSignAttachedTo(firstChestRelative, block)) {

				Sign sign = (Sign) firstChestRelative.getState();
				if (isShopSign(sign)) {
					// Subito attaccato alla cassa c'era un cartello ed era dello shop
					return Database.fromSignBlock(firstChestRelative);
				}

			} else if (firstChestRelative.getType() == Material.CHEST) {

				for (BlockFace secondChestFace : CARDINAL_DIRECTIONS) {
					// Relativo del relativo
					Block secondChestRelative = firstChestRelative.getRelative(secondChestFace);

					if (secondChestRelative.getType() == Material.WALL_SIGN && isSignAttachedTo(secondChestRelative, firstChestRelative)) {

						Sign sign = (Sign) secondChestRelative.getState();
						if (isShopSign(sign)) {
							// Attaccato alla seconda cassa c'era il cartello per lo shop
							return Database.fromSignBlock(secondChestRelative);
						}
					}
				}
			}
		}

		return null;
	}

	public static boolean isSignAttachedTo(Block sign, Block to) {
		return getBlockAttachedTo(sign).equals(to);
	}
	
	// può ritornare null
	public static ShopObject getShopFromInventory(Inventory inventory) {
		InventoryHolder holder = inventory.getHolder();
		Block block = null;
		
		if (holder instanceof Chest) {
			block = ((Chest) holder).getBlock();
		} else if (holder instanceof DoubleChest) {
			InventoryHolder leftSide = ((DoubleChest) holder).getLeftSide();
			block = ((BlockState) leftSide).getBlock();
		}
		
		if (block != null && block.getType() == Material.CHEST) {
			return getShopFromChest(block);
		} else {
			return null;
		}
	}

	public static boolean isShopSign(Sign sign) {
		return sign.getLine(0).equals(SIGN_BUY);
	}

	// Il blocco che sostiene un dato cartello
	@SuppressWarnings("deprecation")
	public static Block getBlockAttachedTo(Block sign) {
		switch (sign.getData() % 4) {
			case 0:
				return sign.getRelative(BlockFace.EAST);
			case 1:
				return sign.getRelative(BlockFace.WEST);
			case 2:
				return sign.getRelative(BlockFace.SOUTH);
			case 3:
				return sign.getRelative(BlockFace.NORTH);
		}

		return null;
	}

	public static boolean isShopInventory(Inventory inventory) {
		return getShopFromInventory(inventory) != null;
	}
}
