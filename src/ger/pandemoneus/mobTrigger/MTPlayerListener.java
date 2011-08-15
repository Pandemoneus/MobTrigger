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
	
	public final HashSet<Player> playersInSelectionMode = new HashSet<Player>();
	public final HashMap<Player, Location> selectedTriggerBlock = new HashMap<Player, Location>();
	public final HashMap<Player, Cuboid> triggerCuboid = new HashMap<Player, Cuboid>();
	
	private final HashMap<Player, Location> selectedFirstPoint = new HashMap<Player, Location>();
	
	/**
	 * Associates this object with a plug-in.
	 * 
	 * @param plugin
	 *            the plug-in
	 */
	public MTPlayerListener(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		chatPrefix = ChatColor.WHITE + "[" + ChatColor.GOLD + pluginName + ChatColor.WHITE + "] ";
		permissionsFound = plugin.getPermissionsFound();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			final Player p = event.getPlayer();
			
			final Action action = event.getAction();
			final Block b = event.getClickedBlock();
			final Location loc = b.getLocation();
			final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
			
			if (playersInSelectionMode.contains(p)) {
				if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
					// check whether is wielding the selection tool for regions
					if (p.getItemInHand().getTypeId() == plugin.getConfig().getSelectionItemId()) {	
						event.setCancelled(true);
						// player already selected first point of the cuboid, so this will set the second
						if (selectedFirstPoint.containsKey(p)) {
							final Location loc1 = selectedFirstPoint.get(p);
							final Location loc2 = loc;
							selectedFirstPoint.remove(p);
							
							final Cuboid c = new Cuboid(p, loc1, loc2);
							triggerCuboid.put(p, c);
							p.sendMessage(chatPrefix + "Second point: " + ChatColor.GREEN + "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
						// select the first point of the cuboid
						} else {
							selectedFirstPoint.put(p, loc);
							p.sendMessage(chatPrefix + "First point: " + ChatColor.GREEN + "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
						}
					} else {
						// check whether the selected block is valid one for a trigger
						if (isValidType(b.getType())) {
							event.setCancelled(true);
							selectedTriggerBlock.put(p, loc);
							
							// the selected block already is a trigger, so we show the ID of it
							if (tc.getTrigger(loc) != null) {
								p.sendMessage(chatPrefix + "Selected " + ChatColor.GREEN + b.getType().toString() + ChatColor.WHITE + " containing Trigger (ID=" + ChatColor.GREEN + tc.getTrigger(loc).getID() + ChatColor.WHITE + ") at " + ChatColor.GREEN + "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
							// otherwise we just show a selection info
							} else {
								p.sendMessage(chatPrefix + "Selected " + ChatColor.GREEN + b.getType().toString() + ChatColor.WHITE + " at " + ChatColor.GREEN + "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
							}
						}
					}
				}
			} else if (isValidType(b.getType()) && tc.getTrigger(loc) != null) {
				if (!((permissionsFound && plugin.getPermissionsHandler().has(p, pluginName.toLowerCase() + ".trigger.use")) || (p.hasPermission(pluginName.toLowerCase() + ".trigger.use")))) {
					p.sendMessage(chatPrefix + ChatColor.RED + "You are not allowed to use triggers!");
					event.setCancelled(true);
				}
			}
		}
	}
	
	public boolean isValidType(Material mat) {
		return (mat == null) ? false : (mat == Material.LEVER) || (mat == Material.STONE_BUTTON) || (mat == Material.STONE_PLATE) || (mat == Material.WOOD_PLATE);
	}
}
