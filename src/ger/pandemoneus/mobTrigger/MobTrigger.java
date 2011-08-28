package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Log;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * MobTrigger plug-in.
 * 
 * Spawns mobs when pressing buttons, switching levers or stepping on pressure plates.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 * 
 */
public class MobTrigger extends JavaPlugin {
	/*
	 * Plug-in related stuff
	 */
	private String version;
	private String pluginName;
	private Log logger;
	
	private MTCommands cmdExecutor;
	private MTConfig config;
	private MTBlockListener blockListener;
	private MTPlayerListener playerListener;
	
	private PermissionHandler permissionsHandler = null;
	private boolean permissionsFound = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable() { 
		// cancel all scheduled triggers
		getServer().getScheduler().cancelTasks(this);
			
		logger.info(new StringBuilder(pluginName).append(" disabled").toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable() {
		// set the name and version of this plug-in according to the plugin.yml file
		PluginDescriptionFile pdfFile = getDescription();
		version = pdfFile.getVersion();
		pluginName = pdfFile.getName();
		
		// set the owner of the logger
		logger = new Log(this);
		logger.info(new StringBuilder(pluginName).append(" v").append(version).append(" enabled").toString());
		
		// load up the configuration
		config = new MTConfig(this);
		config.loadConfig();
		
		// check whether the permissions plug-in is installed
		setupPermissions();
		
		// associate all commands to our custom command executor
		cmdExecutor = new MTCommands(this);
		getCommand("mobtrigger").setExecutor(cmdExecutor);
		getCommand("mt").setExecutor(cmdExecutor);

		// register all events needed
		PluginManager pm = getServer().getPluginManager();
		
		blockListener = new MTBlockListener(this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
		
		playerListener = new MTPlayerListener(this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Lowest, this);
	}

	/**
	 * Returns the version of the plug-in.
	 * 
	 * @return the version of the plug-in
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the name of the plug-in.
	 * 
	 * @return the name of the plug-in
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return pluginName;
	}
	
	/**
	 * Returns the BlockListener of the plug-in.
	 * 
	 * @return the BlockListener of the plug-in.
	 */
	public MTBlockListener getBlockListener() {
		return blockListener;
	}
	
	/**
	 * Returns the PlayerListener of the plug-in.
	 * 
	 * @return the PlayerListener of the plug-in.
	 */
	public MTPlayerListener getPlayerListener() {
		return playerListener;
	}

	/**
	 * Returns whether the permissions plug.in could be found.
	 * 
	 * @return true if permissions plug-in could be found, otherwise false
	 */
	public boolean getPermissionsFound() {
		return permissionsFound;
	}

	/**
	 * Returns the permissionsHandler of this plug-in if it exists.
	 * 
	 * @return the permissionsHandler of this plug-in if it exists, otherwise
	 *         null
	 */
	public PermissionHandler getPermissionsHandler() {
		PermissionHandler ph = null;

		if (getPermissionsFound()) {
			ph = permissionsHandler;
		}

		return ph;
	}

	private void setupPermissions() {
		if (config.forceBukkitPermissions()) {
			logger.info("Force Bukkit Permissions enabled!");
			return;
		}
		
		if (permissionsHandler != null) {
			return;
		}

		Plugin permissionsPlugin = getServer().getPluginManager().getPlugin(
				"Permissions");

		if (permissionsPlugin == null) {
			logger.warning("Permissions not detected, using Bukkit Permissions.");
			return;
		}

		permissionsFound = true;
		permissionsHandler = ((Permissions) permissionsPlugin).getHandler();
	}
	
	/**
	 * Returns the plug-in's configuration object.
	 * 
	 * @return the plug-in's configuration object
	 */
	public MTConfig getConfig() {
		return config;
	}
	
	/**
	 * Returns the plug-in's logger object.
	 * 
	 * @return the plug-in's logger object
	 */
	public Log getLogger() {
		return logger;
	}
}
