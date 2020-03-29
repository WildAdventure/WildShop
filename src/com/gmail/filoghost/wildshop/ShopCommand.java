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

import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.val;
import wild.api.translation.Translation;
import wild.api.uuid.UUIDRegistry;

public class ShopCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length == 0) {
			sender.sendMessage("§aComandi shop:");
			sender.sendMessage("/wildshop list §7- lista dei tuoi negozi.");
			return true;
		}
		
		
		if (args[0].equalsIgnoreCase("list")) {
			UUID owner;
			
			if (args.length >= 2) {
				if (sender.hasPermission("wildshop.list.others")) {
					String playerName = args[1];
					owner = UUIDRegistry.getUUID(playerName);
					
					if (owner == null) {
						sender.sendMessage("§cImpossibile trovare l'UUID di " + playerName + ".");
						return true;
					}
					
				} else {
					sender.sendMessage("§cNon hai il permesso.");
					return true;
				}
			} else {
				if (sender instanceof Player) {
					owner = ((Player) sender).getUniqueId();
				} else {
					sender.sendMessage("§cSpecifica un giocatore: /wildshop list [giocatore]");
					return true;
				}
			}
			
			
			Map<SignLocation, ShopObject> shops = Database.getOwnedShops(owner);
			if (shops.size() == 0) {
				sender.sendMessage("§eNon hai ancora creato negozi.");
			} else {
				sender.sendMessage("§aLista dei tuoi negozi:");
				int index = 1;
				for (val entry : shops.entrySet()) {
					SignLocation location = entry.getKey();
					sender.sendMessage(index + ") " + Translation.of(entry.getValue().getItem().getType()) + " §7X: " + location.getX() + ", Y: " + location.getY() + ", Z: " + location.getZ());
					index++;
				}
			}
			return true;
		}
		
		sender.sendMessage("§cComando sconosciuto. Scrivi /wildshop per una lista dei comandi.");
		return true;
	}
}
