package ger.pandemoneus.mobTrigger.util;

import ger.pandemoneus.mobTrigger.Trigger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.material.Attachable;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

public class Util {
	/**
	 * Returns whether the passed material is a button or lever material.
	 * 
	 * @param mat the material to check
	 * @return true if mat is a button or lever material
	 */
	public static boolean isButtonOrLever(Material mat) {
		return isButton(mat) || isLever(mat);
	}
	
	/**
	 * Returns whether the passed material is a button material.
	 * 
	 * @param mat the material to check
	 * @return true if mat is a button material
	 */
	public static boolean isButton(Material mat) {
		return mat == Material.STONE_BUTTON;
	}
	
	/**
	 * Returns whether the passed material is lever material.
	 * 
	 * @param mat the material to check
	 * @return true if mat is a lever material
	 */
	public static boolean isLever(Material mat) {
		return mat == Material.LEVER;
	}
	
	/**
	 * Returns whether the passed material pressure plate material.
	 * 
	 * @param mat the material to check
	 * @return true if mat is a pressure plate material
	 */
	public static boolean isPressurePlate(Material mat) {
		return (mat == Material.STONE_PLATE) || (mat == Material.WOOD_PLATE);
	}
	
	/**
	 * Returns whether the passed material is a button, lever or pressure plate material.
	 * 
	 * @param mat the material to check
	 * @return true if mat is a button, lever or pressure plate material
	 */
	public static boolean isValidType(Material mat) {
		return isButtonOrLever(mat) || isPressurePlate(mat);
	}
	
	/**
	 * Gets the block that b is attached to.
	 * 
	 * @param b the block
	 * @return the block that b is attached to
	 */
	public static Attachable getAttached(Block b) {
		final Material type = b.getType();
		Attachable result = null;
		
		if (isLever(type)) {
			result = (Lever) b.getState().getData();
		} else if (isButton(type)) {
			result = (Button) b.getState().getData();
		}
		
		return result;
	}
	
	/**
	 * Returns the mob name associated with the ID.
	 * 
	 * @param id the ID
	 * @return the mob name associated with the ID
	 */
	public static CreatureType getMobNameById(int id) {
		CreatureType result = null;
		
		switch (id) {
		case 0:
			result = CreatureType.CAVE_SPIDER;
			break;
		case 1:
			result = CreatureType.CHICKEN;
			break;
		case 2:
			result = CreatureType.COW;
			break;
		case 3:
			result = CreatureType.CREEPER;
			break;
		case 4:
			result = CreatureType.ENDERMAN;
			break;
		case 5:
			result = CreatureType.GHAST;
			break;
		case 6:
			result = CreatureType.PIG;
			break;
		case 7:
			result = CreatureType.PIG_ZOMBIE;
			break;
		case 8:
			result = CreatureType.MONSTER;
			break;
		case 9:
			result = CreatureType.SHEEP;
			break;
		case 10:
			result = CreatureType.SILVERFISH;
			break;
		case 11:
			result = CreatureType.SKELETON;
			break;
		case 12:
			result = CreatureType.SLIME;
			break;
		case 13:
			result = CreatureType.SPIDER;
			break;
		case 14:
			result = CreatureType.SQUID;
			break;
		case 15:
			result = CreatureType.WOLF;
			break;
		case 16:
			result = CreatureType.ZOMBIE;
			break;
		default:
			break;
		}
		
		return result;
	}
	
	/**
	 * Returns a string that equals one of the names in CreatureType if the mobName matches one of them.
	 * 
	 * Otherwise an empty string will be returned.
	 * 
	 * @param mobName the name of the mob to get the CreatureType string from
	 * @return a string that equals one of the names in CreatureType if the mobName matches one of them
	 */
	public static String convertFromFriendlyMobString(String mobName) {
		String result = "";
		final int n = Trigger.NUMBER_OF_CREATURE_TYPES;
		
		if (mobName != null && !mobName.equals("")) {
			for (int i = 0; i < n; i++) {
				if (getMobNameById(i).getName().equalsIgnoreCase(mobName.trim())) {
					result = getMobNameById(i).getName();
				}
			}
		}
		
		return result;
	}
}
