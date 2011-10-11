package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

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
		chatPrefix = new StringBuilder("" + ChatColor.WHITE).append("[").append(ChatColor.GOLD).append(pluginName).append(ChatColor.WHITE).append("] ").toString();
		permissionsFound = plugin.getPermissionsFound();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		final Block b = event.getBlock();
		final Location loc = b.getLocation();
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		
		// only trigger when the current rises
		if (Util.isValidType(b.getType()) && event.getNewCurrent() == REDSTONE_ON) {
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
			final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
			
			Object[] o = null;
			
			do {
				o = getNearbyTriggerAndLocation(b);
							
				if (o != null) {
					final Trigger t = (Trigger) o[0];
					final Location triggerBlock = (Location) o[1];
					
					if ((permissionsFound && plugin.getPermissionsHandler().has(p, pluginName.toLowerCase() + ".trigger.destroy")) || (p.hasPermission(pluginName.toLowerCase() + ".trigger.destroy"))) {
						p.sendMessage(new StringBuilder(chatPrefix).append("You destroyed a trigger block at ").append(ChatColor.GREEN).append("(").append(triggerBlock.getBlockX()).append(", ").append(triggerBlock.getBlockY()).append(", ").append(triggerBlock.getBlockZ()).append(").").toString());
						tc.removeReferenceToTrigger(triggerBlock, t);
					} else {
						p.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not allowed to destroy trigger blocks!").toString());
						event.setCancelled(true);
					}
				}
			} while (o != null);
		}
	}
	
	private Object[] getNearbyTriggerAndLocation(Block b) {
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
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
		
		if (Util.isValidType(b.getType()) && tc.getTrigger(loc) != null) {
			result = new Object[]{tc.getTrigger(loc), loc};
		} else if (Util.isButtonOrLever(southBlock.getType()) && southBlock.getRelative(Util.getAttached(southBlock).getAttachedFace()).equals(b) && tc.getTrigger(southBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(southBlockLoc), southBlockLoc};
		} else if (Util.isButtonOrLever(westBlock.getType()) && westBlock.getRelative(Util.getAttached(westBlock).getAttachedFace()).equals(b) && tc.getTrigger(westBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(westBlockLoc), westBlockLoc};
		} else if (Util.isButtonOrLever(northBlock.getType()) && northBlock.getRelative(Util.getAttached(northBlock).getAttachedFace()).equals(b) && tc.getTrigger(northBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(northBlockLoc), northBlockLoc};
		} else if (Util.isButtonOrLever(eastBlock.getType()) && eastBlock.getRelative(Util.getAttached(eastBlock).getAttachedFace()).equals(b) && tc.getTrigger(eastBlockLoc) != null) {
			result = new Object[]{tc.getTrigger(eastBlockLoc), eastBlockLoc};
		} else if ((Util.isPressurePlate(topBlock.getType()) || (Util.isLever(topBlock.getType())) && topBlock.getRelative(Util.getAttached(topBlock).getAttachedFace()).equals(b)) && tc.getTrigger(topBlockLoc) != null)
			result = new Object[]{tc.getTrigger(topBlockLoc), topBlockLoc};

		return result;
	}
	
	
}
