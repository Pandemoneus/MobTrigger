package ger.pandemoneus.mobTrigger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

/**
 * BlockListener for the MobTrigger plug-in.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class MTBlockListener extends BlockListener {
	
	private final MobTrigger plugin;
	private final String pluginName;
	
	private final String chatPrefix;
	
	private static final int REDSTONE_ON = 1;

	private boolean permissionsFound = false;
	
	/**
	 * Associates this object with a plug-in.
	 * 
	 * @param plugin
	 *            the plug-in
	 */
	public MTBlockListener(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		chatPrefix = ChatColor.WHITE + "[" + ChatColor.GOLD + pluginName + ChatColor.WHITE + "] ";
		permissionsFound = plugin.getPermissionsFound();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		final Block b = event.getBlock();
		final Location loc = b.getLocation();
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		
		// only trigger when the current rises
		if (isValidType(b.getType()) && event.getNewCurrent() == REDSTONE_ON) {
			if (tc.getTrigger(loc) != null) {
				tc.getTrigger(loc).execute();
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			final Block b = event.getBlock();		
			final Player p = event.getPlayer();
			final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
			
			Object[] o = null;
			
			do {
				o = getNearbyTriggerAndLocation(b);
							
				if (o != null) {
					final Trigger t = (Trigger) o[0];
					final Location triggerBlock = (Location) o[1];
					
					if ((permissionsFound && plugin.getPermissionsHandler().has(p, pluginName.toLowerCase() + ".trigger.destroy")) || (p.hasPermission(pluginName.toLowerCase() + ".trigger.destroy"))) {
						p.sendMessage(chatPrefix + "You destroyed a trigger block at " + ChatColor.GREEN + "(" + triggerBlock.getBlockX() + ", " + triggerBlock.getBlockY() + ", " + triggerBlock.getBlockZ() + ").");
						tc.removeReferenceToTrigger(triggerBlock, t);
					} else {
						p.sendMessage(chatPrefix + ChatColor.RED + "You are not allowed to destroy trigger blocks!");
						event.setCancelled(true);
					}
				}
			} while (o != null);
		}
	}
	
	private Object[] getNearbyTriggerAndLocation(Block b) {
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final Location loc = b.getLocation();
		
		final Block southBlock = b.getRelative(BlockFace.SOUTH);
		final Location southBlockLoc = southBlock.getLocation();
		
		final Block westBlock = b.getRelative(BlockFace.WEST);
		final Location westBlockLoc = westBlock.getLocation();
		
		final Block northBlock = b.getRelative(BlockFace.NORTH);
		final Location northBlockLoc = northBlock.getLocation();
		
		final Block eastBlock = b.getRelative(BlockFace.EAST);
		final Location eastBlockLoc = eastBlock.getLocation();
		
		final Block topBlock = b.getRelative(BlockFace.UP);
		final Location topBlockLoc = topBlock.getLocation();
		
		Object[] result = null;
		
		if (isValidType(b.getType()) && tc.getTrigger(loc) != null) {
			result = new Object[]{tc.getTrigger(loc), loc};
		} else if (isButtonOrLever(southBlock.getType()) && southBlock.getRelative(getAttached(southBlock).getAttachedFace()).equals(b) && tc.getTrigger(southBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(southBlockLoc), southBlockLoc};
		} else if (isButtonOrLever(westBlock.getType()) && westBlock.getRelative(getAttached(westBlock).getAttachedFace()).equals(b) && tc.getTrigger(westBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(westBlockLoc), westBlockLoc};
		} else if (isButtonOrLever(northBlock.getType()) && northBlock.getRelative(getAttached(northBlock).getAttachedFace()).equals(b) && tc.getTrigger(northBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(northBlockLoc), northBlockLoc};
		} else if (isButtonOrLever(eastBlock.getType()) && eastBlock.getRelative(getAttached(eastBlock).getAttachedFace()).equals(b) && tc.getTrigger(eastBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(eastBlockLoc), eastBlockLoc};
		} else if ((isPressurePlate(topBlock.getType()) || (isLever(topBlock.getType())) && topBlock.getRelative(getAttached(topBlock).getAttachedFace()).equals(b)) && tc.getTrigger(topBlockLoc) != null)
			result = new Object[]{tc.getTrigger(topBlockLoc), topBlockLoc};

		return result;
	}
	
	private boolean isButtonOrLever(Material mat) {
		return isButton(mat) || isLever(mat);
	}
	
	private boolean isButton(Material mat) {
		return mat == Material.STONE_BUTTON;
	}
	
	private boolean isLever(Material mat) {
		return mat == Material.LEVER;
	}
	
	private boolean isPressurePlate(Material mat) {
		return (mat == Material.STONE_PLATE) || (mat == Material.WOOD_PLATE);
	}
	
	private boolean isValidType(Material mat) {
		return isButtonOrLever(mat) || isPressurePlate(mat);
	}
	
	private Attachable getAttached(Block b) {
		final Material type = b.getType();
		Attachable result = null;
		
		if (isLever(type)) {
			result = (Lever) b.getState().getData();
		} else if (isButton(type)) {
			result = (Button) b.getState().getData();
		}
		
		return result;
	}
}
