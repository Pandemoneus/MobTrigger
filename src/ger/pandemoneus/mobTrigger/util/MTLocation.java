package ger.pandemoneus.mobTrigger.util;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Serializable version of Bukkit's Location class
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class MTLocation implements Serializable {

	/**
	 * serialVersionUID by Eclipse
	 */
	private static final long serialVersionUID = 7135831682192122814L;
	
	final String world;
	final int x;
	final int y;
	final int z;
	
	/**
	 * Constructs a new location with the given world and x, y and z coordinate.
	 * @param world the world
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public MTLocation(final String world, final int x, final int y, final int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Returns the x coordinate of the location.
	 * @return the x coordinate of the location
	 */
	public int getBlockX() {
		return x;
	}
	
	/**
	 * Returns the y coordinate of the location.
	 * @return the y coordinate of the location
	 */
	public int getBlockY() {
		return y;
	}
	
	/**
	 * Returns the z coordinate of the location.
	 * @return the z coordinate of the location
	 */
	public int getBlockZ() {
		return z;
	}
	
	/**
	 * Returns the block at this location.
	 * @return the block at this location
	 */
	public Block getBlock() {
		return new Location(Bukkit.getServer().getWorld(world), x, y, z).getBlock();
	}
	
	/**
	 * Returns the custom version of Bukkit's Location.
	 * @param loc the Bukkit Location
	 * @return the custom version of Bukkit's Location
	 */
	public static MTLocation getMTLocationFromLocation(Location loc) {
		MTLocation result = null;
		
		if (loc != null) {
            result = new MTLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		}
		
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return world + "," + x + "," + y + "," + z;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MTLocation)) {
			return false;
		}
		
		MTLocation loc = (MTLocation) o;
		
		return this.world.equals(loc.world) && this.x == loc.x && this.y == loc.y && this.z == loc.z;
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public int hashCode() {
        int hash = 3;

        hash = 19 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }
}
