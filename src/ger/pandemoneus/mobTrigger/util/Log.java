package ger.pandemoneus.mobTrigger.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Server logger class
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class Log {
	private String pre;
	private static final Logger LOG = Logger.getLogger("Minecraft");
	
	public Log() {
		pre = "";
	}

	public Log(JavaPlugin plugin) {
		if (plugin != null) {
			pre = "[" + plugin.toString() + "] ";
		}
	}

	/**
	 * Displays a info message in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public void info(String message) {
		LOG.log(Level.INFO, pre + message);
	}

	/**
	 * Displays a warning message in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public void warning(String message) {
		LOG.log(Level.WARNING, pre + message);
	}

	/**
	 * Displays a message with severe level in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public void severe(String message) {
		LOG.log(Level.SEVERE, pre + message);
	}
}
