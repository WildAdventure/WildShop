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

import org.bukkit.Material;

public class MaterialUtils {
	
	// Attenzione, può ritornare null
	public static Material match(String input) {
		String formattedInput = input.toLowerCase().replace(" ", "").replace("_", "");
		for (Material mat : Material.values()) {
			if (formattedInput.equals(mat.toString().replace(" _", "").toLowerCase())) {
				return mat;
			}
		}
		
		return null;
	}
	
	public static Material matchFirstChars(String input, int chars) {
	
		String formattedInput = cutTo(keepAlphanumericToLowercase(input), chars);
		
		for (Material mat : Material.values()) {
			if (formattedInput.equals(keepAlphanumericToLowercase(mat.toString()))) {
				// Perfect match
				return mat;
			}
		}
		
		for (Material mat : Material.values()) {
			
			if (formattedInput.equals( cutTo(keepAlphanumericToLowercase(mat.toString()), chars) )) {
				// Partial match
				return mat;
			}
		}
		
		return null;
	}
	
	public static String cutTo(String input, int chars) {
		if (input.length() > chars) {
			return input.substring(0, chars);
		} else {
			return input;
		}
	}
	
	public static String keepAlphanumericToLowercase(String input) {
		StringBuilder sb = new StringBuilder();
		for (char c : input.toCharArray()) {
			if (Character.isLetter(c)) {
				sb.append(Character.toLowerCase(c));
			} else if (Character.isDigit(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	private static Material[] armorsAndTools = {
		
		Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
		Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET,
		Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET,
		Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
		
		Material.WOOD_PICKAXE, Material.WOOD_HOE, Material.WOOD_AXE, Material.WOOD_SPADE, Material.WOOD_SWORD,
		Material.STONE_PICKAXE, Material.STONE_HOE, Material.STONE_AXE, Material.STONE_SPADE, Material.STONE_SWORD,
		Material.IRON_PICKAXE, Material.IRON_HOE, Material.IRON_AXE, Material.IRON_SPADE, Material.IRON_SWORD,
		Material.GOLD_PICKAXE, Material.GOLD_HOE, Material.GOLD_AXE, Material.GOLD_SPADE, Material.GOLD_SWORD,
		Material.DIAMOND_PICKAXE, Material.DIAMOND_HOE, Material.DIAMOND_AXE, Material.DIAMOND_SPADE, Material.DIAMOND_SWORD,
		
		Material.BOW, Material.FISHING_ROD, Material.ANVIL, Material.SHEARS, Material.FLINT_AND_STEEL
		
	};
	
	public static boolean isArmorOrTool(Material mat) {
		for (Material m : armorsAndTools) {
			if (m == mat) {
				return true;
			}
		}
		return false;
	}
}
