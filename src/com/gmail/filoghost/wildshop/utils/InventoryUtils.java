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
package com.gmail.filoghost.wildshop.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	public static boolean containsAtLeast(Inventory inv, ItemStack itemToCheck, int amount) {
		if (amount <= 0) {
		      return true;
		}
		
		for (ItemStack i : inv.getContents()) {
		      if (i != null && i.isSimilar(itemToCheck)) {
		    	  amount -= i.getAmount();
		    	  if (amount <= 0) {
		    		  return true;
		    	  }
		      }
		}
		
		return false;
	}
	
	// ritorna gli oggetti rimossi
	public static List<ItemStack> remove(Inventory inv, ItemStack itemToCheck, int amount) {
		List<ItemStack> removedItems = new ArrayList<ItemStack>();
		
		ItemStack[] items = inv.getContents();
		ItemStack current = null;
		
		for (int i = items.length - 1; i >= 0; i--) {
			current = items[i];
			
			if (current != null && current.isSimilar(itemToCheck)) {
				if (current.getAmount() > amount) {
					
					ItemStack clone = current.clone();
					clone.setAmount(amount); // quello che abbiamo levato
					removedItems.add(clone);
					
					current.setAmount(current.getAmount() - amount);
					return removedItems;
				} else {
					removedItems.add(current);
					amount -= current.getAmount();
					inv.clear(i);
				}
			}
			
			if (amount <= 0) return removedItems;
		}
		
		return removedItems;
	}
	
}
