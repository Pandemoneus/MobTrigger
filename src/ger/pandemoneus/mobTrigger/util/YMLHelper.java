package ger.pandemoneus.mobTrigger.util;

import ger.pandemoneus.mobTrigger.MobTrigger;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.util.config.Configuration;

/**
 * Modified by:
 * @author Pandemoneus - https://github.com/Pandemoneus
 * 
 * Original version by:
 * @author bekvon - https://github.com/bekvon
 * Taken from his plug-in Residence:
 * http://forums.bukkit.org/threads/sec-mech-econ-residence-v2-3-7-self-serve-area-protection-system-buy-sell-land-economy-1000.9358/
 */
public class YMLHelper extends Configuration {	
	
	private final File f;
	private final MobTrigger plugin;
	
	/**
	 * Constructs a new YMLHelper.
	 * 
	 * @param f the file the helper saves to
	 * @param plugin the plugin that owns the helper
	 */
	public YMLHelper(File f, MobTrigger plugin) {
		super(f);
		this.f = f;
		this.plugin = plugin;
		root = new LinkedHashMap<String, Object>();
	}

	/**
	 * Adds a map to save.
	 * 
	 * @param name the key it is saved under
	 * @param map the map
	 */
	public void addMap(String name, Map<String, Object> map) {
		load();
		root.put(name, map);
		save();
	}

	/**
	 * Gets a map by the given key.
	 * If the key is invalid, null will be returned.
	 * 
	 * @param name the key
	 * @return the map that is found at that key or null
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String name) {
		Map<String, Object> map = null;
		
		try {
			if (root.get(name) != null) {
				map = (Map<String, Object>) root.get(name);
			}
		} catch (ClassCastException cce) {
			plugin.getLogger().severe("Failed loading " + f.getName() + "! Was it manually edited?");
			cce.printStackTrace();
		}
		
		return map;
	}
}
