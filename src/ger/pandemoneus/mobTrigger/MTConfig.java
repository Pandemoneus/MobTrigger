package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Log;
import ger.pandemoneus.mobTrigger.util.YMLHelper;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;


/**
 * The configuration file for the MobTrigger plug-in, uses YML.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class MTConfig {

	private final MobTrigger plugin;
	private final String pluginName;
	private final String pluginVersion;
	private final Log logger;

	/**
	 * File handling
	 */
	private final String directory;
	private final File configFile;
	private final File cuboidFile;
	private final File triggerFile;
	private final Configuration bukkitConfig; 
	
	private YMLHelper cuboidManager = null;
	private TriggerCollection triggerCol = null;

	/**
	 * Default settings
	 */
	private int selectionItemId = 69;
	private boolean forceBukkitPerm = false;
	
	/**
	 * Associates this object with a plugin.
	 * 
	 * @param plugin the plugin
	 */
	public MTConfig(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		pluginVersion = plugin.getVersion();
		logger = plugin.getLogger();
		directory = "plugins" + File.separator + pluginName + File.separator;
		configFile = new File(directory + File.separator + "config.yml");
		cuboidFile = new File(directory + File.separator + "cuboids.yml");
		triggerFile = new File(directory + File.separator + "triggers.yml");
		bukkitConfig = new Configuration(configFile);
		
		setupCuboidManager();
		setupTriggerCollection();
	}

	/**
	 * Loads the configuration used by this plugin.
	 * 
	 * @return true if config loaded without errors
	 */
	public boolean loadConfig() {
		boolean isErrorFree = true;

		new File(directory).mkdirs();

		if (configFile.isFile()) {
			bukkitConfig.load();
			if (bukkitConfig.getString("Version", "").equals(pluginVersion)) {
				// config file exists and is up to date
				logger.info(pluginName + " config file found, loading config...");
				loadData();
			} else {
				// config file exists but is outdated
				logger.info(pluginName
						+ " config file outdated, adding old data and creating new values. "
						+ "Make sure you change those!");
				loadData();
				writeDefault();
			}
		} else {
			// config file does not exist
			try {
				logger.info(pluginName
						+ " config file not found, creating new config file...");
				configFile.createNewFile();
				writeDefault();
			} catch (IOException ioe) {
				logger.severe("Could not create the config file for " + pluginName + "!");
				ioe.printStackTrace();
				isErrorFree = false;
			}
		}

		return isErrorFree;
	}

	private void loadData() {
		selectionItemId = bukkitConfig.getInt("Trigger.Region.SelectionItemId", 69);
		forceBukkitPerm = bukkitConfig.getBoolean("ForceBukkitPermissions", false);
	}

	private void writeDefault() {
		bukkitConfig.setHeader("### Learn more about how this config can be edited and changed to your preference on the forum page. ###");
		
		write("Version", pluginVersion);
		write("ForceBukkitPermissions", forceBukkitPerm);
		write("Trigger.Region.SelectionItemId", selectionItemId);
		
		loadData();
	}
	
	/**
	 * Returns the id of the item that is used to select trigger regions.
	 * 
	 * @return the id of the item that is used to select trigger regions
	 */
	public int getSelectionItemId() {
		return selectionItemId;
	}
	
	/**
	 * Determines whether the Bukkit permission system is forced to be used.
	 * 
	 * @return whether the Bukkit permission system is forced to be used
	 */
	public boolean forceBukkitPermissions() {
		return forceBukkitPerm;
	}

	/**
	 * Reads a string representing a long from the config file.
	 * 
	 * Returns '0' when an exception occurs.
	 * 
	 * @param key
	 *            the key
	 * @param def
	 *            default value
	 * @return the long specified in 'key' from the config file, '0' on errors
	 */
	@SuppressWarnings("unused")
	private long readLong(String key, String def) {
		bukkitConfig.load();

		// Bukkit Config has no getLong(..)-method, so we are using Strings
		String value = bukkitConfig.getString(key, def);

		long tmp = 0;

		try {
			tmp = Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			logger.warning("Error parsing a long from the config file. Key=" + key);
			nfe.printStackTrace();
		}

		return tmp;
	}

	private void write(String key, Object o) {
		bukkitConfig.load();
		bukkitConfig.setProperty(key, o);
		bukkitConfig.save();
	}

	/**
	 * Returns a list containing all loaded keys.
	 * 
	 * @return a list containing all loaded keys
	 */
	public String[] printLoadedConfig() {
		bukkitConfig.load();

		String[] tmp = bukkitConfig.getAll().toString().split(",");
		int n = tmp.length;

		tmp[0] = tmp[0].substring(1);
		tmp[n - 1] = tmp[n - 1].substring(0, tmp[n - 1].length() - 1);

		for (String s : tmp) {
			s = s.trim();
		}

		return tmp;
	}

	/**
	 * Returns the config file.
	 * 
	 * @return the config file
	 */
	public File getConfigFile() {
		return configFile;
	}

	/**
	 * Returns the associated plugin.
	 * 
	 * @return the associated plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	private void setupCuboidManager() {
		new File(directory).mkdirs();
		
		if (cuboidFile.isFile()) {
			cuboidManager = new YMLHelper(cuboidFile, plugin);
			
			cuboidManager.load();
		} else {
			try {
				cuboidFile.createNewFile();
				cuboidManager = new YMLHelper(cuboidFile, plugin);
			} catch (IOException ioe) {
				logger.severe("Could not create the cuboid file for " + pluginName + "!");
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the cuboid manager.
	 * 
	 * @return the cuboid manager
	 */
	public YMLHelper getCuboidManager() {
		return cuboidManager;
	}
	
	private void setupTriggerCollection() {
		new File(directory).mkdirs();
		
		if (triggerFile.isFile()) {
			triggerCol = new TriggerCollection(triggerFile, plugin);
			
			triggerCol.load();
			triggerCol.loadTriggers();
		} else {
			try {
				triggerFile.createNewFile();
				triggerCol = new TriggerCollection(triggerFile, plugin);
			} catch (IOException ioe) {
				logger.severe("Could not create the trigger file for " + pluginName + "!");
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the trigger collection.
	 * 
	 * @return the trigger collection
	 */
	public TriggerCollection getTriggerCollection() {
		return triggerCol;
	}
}
