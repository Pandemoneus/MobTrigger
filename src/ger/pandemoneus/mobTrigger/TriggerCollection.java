package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Cuboid;
import ger.pandemoneus.mobTrigger.util.Log;
import ger.pandemoneus.mobTrigger.util.MTLocation;
import ger.pandemoneus.mobTrigger.util.YMLHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

/**
 * This class represents the trigger manager.
 * It saves, loads and manages triggers.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class TriggerCollection extends YMLHelper {
	
	private final String pluginName;
	private final Log logger;
	
	private final String directory;
	private final File indexFile;
	
	private HashMap<MTLocation, Integer> indices = new HashMap<MTLocation, Integer>();
	private ArrayList<Trigger> col = new ArrayList<Trigger>();
	
	/**
	 * Constructs a new trigger collection.
	 * @param f the file where the triggers are saved
	 * @param plugin the plugin the collection belongs to
	 */
	public TriggerCollection(File f, MobTrigger plugin) {
		super(f, plugin);
		pluginName = plugin.getPluginName();
		logger = plugin.getLogger();
		directory = "plugins" + File.separator + pluginName + File.separator;
		indexFile = new File(directory + File.separator + "triggerindices.dat");
	}
	
	/**
	 * Adds a reference to a trigger, so multiple sources can access the same trigger.
	 * @param loc the location of the trigger block
	 * @param t the trigger to reference to
	 */
	public void addReferenceToTrigger(Location loc, Trigger t) {
		if (t != null && loc != null) {
			if (!col.contains(t)) {
				col.add(t);
			}
			
			MTLocation l = MTLocation.getMTLocationFromLocation(loc);
			
			indices.put(l, col.indexOf(t));
			saveIndexFile(indices);
		}
	}
	
	/**
	 * Removes a reference to a trigger.
	 * Note that when a trigger has no more references, it is NOT deleted.
	 * @param loc the location of the trigger block
	 * @param t the trigger of which the reference is removed to
	 */
	public void removeReferenceToTrigger(Location loc, Trigger t) {
		if (t != null && loc != null && col.contains(t)) {
			MTLocation l = MTLocation.getMTLocationFromLocation(loc);
			
			if (indices.containsKey(l)) {
				indices.remove(l);
				saveIndexFile(indices);
			}
		}
	}
	
	/**
	 * Updates a trigger.
	 * Call this after you changed a trigger's settings.
	 * @param t the trigger
	 */
	public void updateTrigger(Trigger t) {
		if (t != null && col.contains(t)) {
			int index = col.indexOf(t);
			col.remove(index);
			col.add(index, t);
		}
	}
	
	/**
	 * Returns a trigger by its reference.
	 * If the reference had no trigger or the reference did not exist,
	 * null will be returned.
	 * 
	 * @param loc the trigger block
	 * @return the trigger the trigger block had its reference to or null
	 */
	public Trigger getTrigger(Location loc) {
		Trigger result = null;
		
		if (loc != null) {
			MTLocation l = MTLocation.getMTLocationFromLocation(loc);
			
			if (indices.containsKey(l)) {
				result = col.get(indices.get(l));
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a trigger by its ID.
	 * If there is no trigger with that ID in the collection,
	 * null will be returned.
	 * 
	 * @param id the trigger ID
	 * @return the trigger with the given ID or null
	 */
	public Trigger getTriggerByID(int id) {
		Trigger result = null;
		
		for (Trigger t : col) {
			if (t.getID() == id) {
				result = t;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a list of all triggers that have the passed cuboid.
	 * @param c the cuboid the triggers should have
	 * @return a list of all triggers that have the passed cuboid
	 */
	public ArrayList<Trigger> getTriggersByCuboid(Cuboid c) {
		ArrayList<Trigger> result = new ArrayList<Trigger>();
		
		if (c != null) {
			for (Trigger t : col) {
				if (t.getCuboid().equals(c)) {
					result.add(t);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a list containing all registered triggers.
	 * 
	 * @return a list containing all registered triggers
	 */
	public ArrayList<Trigger> getAllTriggers() {
		ArrayList<Trigger> result = new ArrayList<Trigger>();
		
		for (Trigger t : col) {
			result.add(t);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void loadIndexFile() {
		new File(directory).mkdirs();
		
		if (indexFile.isFile()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));
		        indices = (HashMap<MTLocation, Integer>) ois.readObject();
		        ois.close();
			} catch (IOException ioe) {
				logger.severe("Failed reading '" + indexFile.getName() + "'!");
				ioe.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				logger.severe(indexFile.getName() + " contains an unknown class, was it modified?");
				cnfe.printStackTrace();
			}	
		}
	}
	
	private void saveIndexFile(HashMap<MTLocation, Integer> map) {
		try {
			 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile));
		     oos.writeObject(map);
		     oos.flush();
		     oos.close();
		} catch (IOException ioe) {
			logger.severe("Failed writing '" + indexFile.getName() + "'!");
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Loads all triggers from the configuration file.
	 */
	public void loadTriggers() {
		load();
		
		for (String str : getKeys()) {
			Trigger t = new Trigger();
			t = t.load(getMap(str));
			
			col.add(t);
		}
		
		loadIndexFile();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMap(String name, Map<String, Object> map) {
		load();
		root.put(name, map);
		save();
		
		saveIndexFile(indices);
	}
}
