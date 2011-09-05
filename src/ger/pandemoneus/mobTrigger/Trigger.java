package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Cuboid;
import ger.pandemoneus.mobTrigger.util.Util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This class represents a trigger which spawns mobs.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class Trigger implements Comparable<Trigger> {

	private final MobTrigger plugin;
	private final int id;
	private final String owner;
	private final Cuboid cuboid;
	private final double firstDelay;
	private final boolean selfTriggering;
	private final double selfTriggerDelay;
	private final int totalTimes;
	private final int[] amountOfMobs = new int[13];
	
	private int remainingTimes;
	private int taskID = 0;
	private boolean isExecuting = false;
	
	private final Runnable exec = new Runnable() {
        public void run() {
            fire();
        }
    };
    
    private final HashSet<Entity> spawnedCreatures = new HashSet<Entity>();
	
    /**
     * Constructs a new trigger.
     * 
     * @param plugin the plugin the trigger belongs to
     * @param id the ID of the trigger
     * @param owner the trigger's owner
     * @param cuboid the cuboid the mobs spawn in
     * @param firstDelay the delay after the trigger first fires
     * @param selfTriggering determines whether the trigger executes itself again
     * @param selfTriggerDelay the delay after which it executes itself again
     * @param totalTimes total times it can be executed
     */
	public Trigger(MobTrigger plugin, int id, String owner, Cuboid cuboid, double firstDelay, boolean selfTriggering, double selfTriggerDelay, int totalTimes) {
		this.plugin = plugin;
		this.id = id < 0 ? 0 : id;
		this.owner = owner;
		this.cuboid = cuboid;
		this.firstDelay = firstDelay < 0.0 ? 0.0 : firstDelay;
		this.selfTriggering = selfTriggering;
		this.selfTriggerDelay = selfTriggerDelay < 0.0 ? 0.0 : selfTriggerDelay;
		this.totalTimes = totalTimes < 0 ? 0 : totalTimes;
		remainingTimes = this.totalTimes;
	}
	
	/**
	 * Constructs a dummy trigger object that is not meant for real use.
	 */
	Trigger() {
		plugin = null;
		id = -1;
		owner = "";
		cuboid = null;
		firstDelay = 0;
		selfTriggering = false;
		selfTriggerDelay = 0;
		totalTimes = 0;
	}
	
	/**
	 * Gets the ID of this trigger.
	 * 
	 * @return the ID of this trigger
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Gets the owner of this trigger.
	 * 
	 * @return the owner of this trigger
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Gets the cuboid in which the mobs spawn in.
	 * 
	 * @return the cuboid in which the mobs spawn in
	 */
	public Cuboid getCuboid() {
		return cuboid;
	}
	
	/**
	 * Returns the delay in seconds after which mobs are first spawned.
	 * 
	 * @return the delay in seconds after which mobs are first spawned
	 */
	public double getFirstDelay() {
		return firstDelay;
	}
	
	/**
	 * Returns the delay in seconds after which the trigger triggers again.
	 * 
	 * @return the delay in seconds after which the trigger triggers again
	 */
	public double getSelfTriggerDelay() {
		return selfTriggerDelay;
	}
	
	/**
	 * Returns whether the trigger triggers itself again after the delay.
	 * 
	 * @return whether the trigger triggers itself again after the delay
	 */
	public boolean isSelfTriggering() {
		return selfTriggering;
	}
	
	/**
	 * Returns how many more times the trigger can be fired.
	 * 
	 * @return how many more times the trigger can be fired
	 */
	public int getRemainingTimes() {
		return remainingTimes;
	}
	
	/**
	 * Returns how many times the trigger can be fired.
	 * 
	 * @return how many times the trigger can be fired
	 */
	public int getTotalTimes() {
		return totalTimes;
	}
	
	/**
	 * Returns the amount of the given mob that is spawned by this trigger.
	 * 
	 * @param ct the mob type
	 * @return the amount of the given mob that is spawned by this trigger
	 */
	public int getAmountOfMobType(CreatureType ct) {
		int result = 0;
		
		for (int i = 0; i < amountOfMobs.length; i++) {
			if (Util.getMobNameById(i) == ct) {
				result = amountOfMobs[i];
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the amount array for all mobs that are spawned by this trigger.
	 * 
	 * @return the amount array for all mobs that are spawned by this trigger
	 */
	public int[] getAmountOfMobs() {
		return amountOfMobs;
	}
	
	/**
	 * Sets the amount of spawned mobs of the given type.
	 * 
	 * @param ct the mob type
	 * @param amount the amount to spawn
	 */
	public void setAmountOfMobType(CreatureType ct, int amount) {
		if (amount < 0) {
			return;
		}
		
		for (int i = 0; i < amountOfMobs.length; i++) {
			if (Util.getMobNameById(i) == ct) {
				amountOfMobs[i] = amount;
			}
		}
	}
	
	/**
	 * Sets the amount of spawned mobs of the given type.
	 * 
	 * @param ct the mob type
	 * @param amount the amount to spawn
	 */
	public void setAmountOfMobs(int[] amountArray) {
		int n = amountOfMobs.length;
		if (amountArray == null || amountArray.length != n) {
			return;
		}
		
		System.arraycopy(amountArray, 0, amountOfMobs, 0, n);
		
		for (int i = 0; i < n; i++) {
			if (amountOfMobs[i] < 0) {
				amountOfMobs[i] = 0;
			}
		}
	}
	
	/**
	 * Executes the trigger.
	 * 
	 * Uses a separate thread.
	 * Spawns monsters in the defined cuboid.
	 */
	public void execute() {
		if (plugin == null)
			return;
		
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		
		if (remainingTimes != 0 && !isExecuting) {
			if(!selfTriggering) {
				taskID = scheduler.scheduleAsyncDelayedTask(plugin, exec, Math.round(20 * firstDelay));
			} else {
				taskID = scheduler.scheduleAsyncRepeatingTask(plugin, exec, Math.round(20 * firstDelay), Math.round(20 * selfTriggerDelay));
				isExecuting = true;
			}
		}
	}
	
	private synchronized void fire() {
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		
		if (remainingTimes == 0) {
			scheduler.cancelTask(taskID);
			isExecuting = false;
			return;
		}
		
		for (int i = 0; i < amountOfMobs.length; i++) {
			for (int j = 0; j < amountOfMobs[i]; j++) {
				Entity spawnedEntity = cuboid.getWorld().spawnCreature(cuboid.getRandomLocationForMobs(), Util.getMobNameById(i));
				spawnedCreatures.add(spawnedEntity);
			}
		}
		
		remainingTimes--;
	}
	
	/**
	 * Resets the trigger to its original state.
	 * 
	 * Also kills all mobs spawned by the trigger.
	 */
	public void reset() {
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		
		// reset the times to their original state
		remainingTimes = totalTimes;
		
		// cancel ongoing tasks
		scheduler.cancelTask(taskID);
		isExecuting = false;
		
		// kill all mobs spawned by the trigger
		for (Entity ent : spawnedCreatures) {
			if (!ent.isDead()) {
				ent.remove();
			}
		}
		
		spawnedCreatures.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Trigger)) {
			return false;
		}
		
		Trigger t = (Trigger) o;
		
		return this.id == t.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Trigger t) {
		if (this.equals(t)) {
			return 0;
		} else {
			return (t.id > this.id) ? 1 : -1;
		}
	}
	
	/**
	 * Saves the trigger to a Map.
	 * 
	 * @return the trigger in a Map
	 */
	public Map<String, Object> save() {
		Map<String, Object> root = new LinkedHashMap<String, Object>();

		root.put("ID", id);
		root.put("Owner", owner);
		root.put("Cuboid", cuboid.toRaw());
		root.put("FirstDelay", firstDelay);
		root.put("SelfTriggering", selfTriggering);
		root.put("SelfTriggerDelay", selfTriggerDelay);
		root.put("TotalTimes", totalTimes);
		
		for (int i = 0; i < 13; i++) {
			CreatureType mob = Util.getMobNameById(i);
			root.put("amountOf." + mob.getName(), getAmountOfMobType(mob));
		}
		
		return root;
	}
	
	/**
	 * Loads the trigger from a Map.
	 * 
	 * @param root
	 *            the Map
	 * @return the trigger
	 * @throws IllegalArgumentException
	 */
	public Trigger load(Map<String, Object> root) throws IllegalArgumentException {
		if (root == null) {
			throw new IllegalArgumentException("Invalid root map!");
		}
		
		/*
		 * NOTE: I know this is pretty ugly and bad design, but I will use this until
		 *       I find a better solution.
		 */

		MobTrigger p = (MobTrigger) Bukkit.getServer().getPluginManager().getPlugin("MobTrigger");
		int i = -1;
		String o = "";
		String world = "world";
		String cuboidOwner = "";
		double fd = 0.0;
		boolean st = false;
		double std = 0.0;
		int t = 1;
		int lowX = 0;
		int lowY = 0;
		int lowZ = 0;
		int highX = 0;
		int highY = 0;
		int highZ = 0;
		
		try {
			i = (Integer) root.get("ID");
			o = (String) root.get("Owner");
			
			String str = (String) root.get("Cuboid");
			
			cuboidOwner = str.substring(0, str.indexOf(","));
			str = str.substring(str.indexOf(",") + 1);
			world = str.substring(0, str.indexOf(","));
			str = str.substring(str.indexOf(",") + 1);
			lowX = Integer.parseInt(str.substring(0, str.indexOf(",")));
			str = str.substring(str.indexOf(",") + 1);
			lowY = Integer.parseInt(str.substring(0, str.indexOf(",")));
			str = str.substring(str.indexOf(",") + 1);
			lowZ = Integer.parseInt(str.substring(0, str.indexOf(",")));
			str = str.substring(str.indexOf(",") + 1);
			highX = Integer.parseInt(str.substring(0, str.indexOf(",")));
			str = str.substring(str.indexOf(",") + 1);
			highY = Integer.parseInt(str.substring(0, str.indexOf(",")));
			str = str.substring(str.indexOf(",") + 1);
			highZ = Integer.parseInt(str);
			
			fd = (Double) root.get("FirstDelay");
			st = (Boolean) root.get("SelfTriggering");
			std = (Double) root.get("SelfTriggerDelay");
			t = (Integer) root.get("TotalTimes");
		} catch (NumberFormatException nfe) {
			p.getLogger().severe("Ill-formated raw cuboid data in trigger with ID=" + i);
			nfe.printStackTrace();
		} catch (ClassCastException cce) {
			p.getLogger().severe("Ill-formated integers in trigger with ID=" + i);
			cce.printStackTrace();
		}
		
		World w = Bukkit.getServer().getWorld(world);
		
		if (w == null) {
			p.getLogger().severe("World '" + world + "' does not exist! Check your triggers.");
		}
		
		Location low = new Location(w, lowX, lowY, lowZ);
		Location high = new Location(w, highX, highY, highZ);
		
		Cuboid c = new Cuboid(cuboidOwner, low, high);
		Trigger temp = new Trigger(p, i, o, c, fd, st, std, t);
		
		for (int j = 0; j < 13; j++) {
			CreatureType mob = Util.getMobNameById(j);
			temp.setAmountOfMobType(mob, (Integer) root.get("amountOf." + mob.getName()));
		}

		return temp;
	}
}
