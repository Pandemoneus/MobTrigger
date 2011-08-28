package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Cuboid;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * PlayerListener for the MobTrigger plug-in.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class MTPlayerListener extends PlayerListener {
	
	private final MobTrigger plugin;
	private final String pluginName;
	
	private final String chatPrefix;
	
	private boolean permissionsFound = false;
	
	public final HashSet<String> playersInSelectionMode = new HashSet<String>();
	public final HashMap<String, Location> selectedTriggerBlock = new HashMap<String, Location>();
	public final HashMap<String, Cuboid> triggerCuboid = new HashMap<String, Cuboid>();
	
	private final HashMap<String, Location> selectedFirstPoint = new HashMap<String, Location>();
	
	/**
	 * Associates this object with a plug-in.
	 * 
	 * @param plugin
	 *            the plug-in
	 */
	public MTPlayerListener(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		chatPrefix = new StringBuilder("" + ChatColor.WHITE).append("[").append(ChatColor.GOLD).append(pluginName).append(ChatColor.WHITE).append("] ").toString();
		permissionsFound = plugin.getPermissionsFound();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			final Player p = event.getPlayer();
			final String playerName = p.getName();
			
			final Action action = event.getAction();
			final Block b = event.getClickedBlock();
			final Location loc = b.getLocation();
			final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
			
			if (playersInSelectionMode.contains(playerName)) {
				if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
					// check whether is wielding the selection tool for regions
					if (p.getItemInHand().getTypeId() == plugin.getConfig().getSelectionItemId()) {	
						event.setCancelled(true);
						// player already selected first point of the cuboid, so this will set the second
						if (selectedFirstPoint.containsKey(playerName)) {
							final Location loc1 = selectedFirstPoint.get(playerName);
							final Location loc2 = loc;
							selectedFirstPoint.remove(playerName);
							
							final Cuboid c = new Cuboid(playerName, loc1, loc2);
							triggerCuboid.put(playerName, c);
							p.sendMessage(new StringBuilder(chatPrefix).append("Second point: ").append(ChatColor.GREEN).append("(").append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(")").toString());
						// select the first point of the cuboid
						} else {
							selectedFirstPoint.put(playerName, loc);
							p.sendMessage(new StringBuilder(chatPrefix).append("First point: ").append(ChatColor.GREEN).append("(").append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(")").toString());
						}
					} else {
						// check whether the selected block is valid one for a trigger
						if (isValidType(b.getType())) {
							event.setCancelled(true);
							selectedTriggerBlock.put(playerName, loc);
							
							// the selected block already is a trigger, so we show the ID of it
							if (tc.getTrigger(loc) != null) {
								p.sendMessage(new StringBuilder(chatPrefix).append("Selected ").append(ChatColor.GREEN).append(b.getType().toString()).append(ChatColor.WHITE).append(" containing Trigger (ID=").append(ChatColor.GREEN).append(tc.getTrigger(loc).getID()).append(ChatColor.WHITE).append(") at ").append(ChatColor.GREEN).append("(").append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(")").toString());
							// otherwise we just show a selection info
							} else {
								p.sendMessage(new StringBuilder(chatPrefix).append("Selected ").append(ChatColor.GREEN).append(b.getType().toString()).append(ChatColor.WHITE).append(" at ").append(ChatColor.GREEN).append("(").append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(")").toString());
							}
						}
					}
				}
			} else if (isValidType(b.getType()) && tc.getTrigger(loc) != null) {
				if (!((permissionsFound && plugin.getPermissionsHandler().has(p, pluginName.toLowerCase() + ".trigger.use")) || (p.hasPermission(pluginName.toLowerCase() + ".trigger.use")))) {
					p.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not allowed to use triggers!").toString());
					event.setCancelled(true);
				}
			}
		}
	}
	
	public boolean isValidType(Material mat) {
		return (mat == null) ? false : (mat == Material.LEVER) || (mat == Material.STONE_BUTTON) || (mat == Material.STONE_PLATE) || (mat == Material.WOOD_PLATE);
	}
}
