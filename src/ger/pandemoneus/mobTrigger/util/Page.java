package ger.pandemoneus.mobTrigger.util;

import org.bukkit.entity.Player;

/**
 * This class represents a page for the MineCraft chat window.
 * 
 * It is made for users that do not use any chat modifications.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class Page {
	private static final int MAX_LINES = 8;
	private final String[] lines = new String[MAX_LINES];
	
	/**
	 * Constructs a page containing the passed lines.
	 * 
	 * @param lines the lines of the page
	 */
	public Page(String[] lines) {
		int n = lines.length;
		
		if (n <= MAX_LINES) {
			System.arraycopy(lines, 0, this.lines, 0, n);
		}
	}
	
	/**
	 * Constructs an empty page.
	 */
	public Page() {}
	
	/**
	 * Adds a line to this page.
	 * If the page is already full, no line will be added.
	 * 
	 * @param text the line to add
	 * @return true if successful, false if it was full
	 */
	public boolean addLine(String text) {
		if (!isFull()) {
			boolean notDone = true;
			
			for (int i = 0; i < MAX_LINES && notDone; i++) {
				if (lines[i] == null) {
					lines[i] = text;
					notDone = false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether the page already has the maximum amount of lines.
	 * 
	 * @return true if the page is full
	 */
	public boolean isFull() {
		boolean result = true;
		
		for (int i = 0; i < MAX_LINES; i++) {
			if (lines[i] == null) {
				result = false;
			}
		}
		
		return result;
	}
	
	/**
	 * Displays the page to the given player with a prefix before each line.
	 * 
	 * @param sender the player to display the page to
	 * @param prefix the prefix that goes in front of each line
	 */
	public void displayPage(Player sender, String prefix) {
		if (sender != null && prefix != null) {
			for (int i = 0; i < MAX_LINES; i++) {
				if (lines[i] != null) {
					sender.sendMessage(prefix + lines[i]);
				}
			}
		}
	}
}
