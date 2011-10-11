package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Book;
import ger.pandemoneus.mobTrigger.util.Cuboid;
import ger.pandemoneus.mobTrigger.util.Page;
import ger.pandemoneus.mobTrigger.util.Util;
import ger.pandemoneus.mobTrigger.util.YMLHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;

import com.nijiko.permissions.PermissionHandler;

/**
 * Command class.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 * 
 */
public final class MTCommands implements CommandExecutor {

	private final MobTrigger plugin;
	private final String pluginName;
	
	private final String chatPrefix;
	private final String notAuthorized;
	private final String invalidArgs;
	
	private boolean permissionsFound = false;
	private PermissionHandler ph = null;
	
	private final HashMap<String, Integer> pageIndex = new HashMap<String, Integer>();
	private final HashMap<String, Book> currentBook = new HashMap<String, Book>();
	private final HashMap<String, Integer> currentBookID = new HashMap<String, Integer>();

	/**
	 * Associates this object with a plugin
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public MTCommands(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		
		chatPrefix = new StringBuilder("" + ChatColor.WHITE).append("[").append(ChatColor.GOLD).append(pluginName).append(ChatColor.WHITE).append("] ").toString();
		notAuthorized = new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not authorized to use this command.").toString();
		invalidArgs = new StringBuilder(chatPrefix).append(ChatColor.RED).append("Too few or invalid arguments. Usage:").toString();
		
		permissionsFound = plugin.getPermissionsFound();
		ph = plugin.getPermissionsHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args != null) {
			if (sender instanceof Player) {
				determineCommand((Player) sender, cmd, commandLabel, args);
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Sorry, you are not a player!").toString());
			}
		}

		return true;
	}

	private void determineCommand(Player sender, Command cmd, String commandLabel, String[] args) {
		final int n = args.length;
		
		if (n == 0) {
			// show help
			if (hasPerm(sender, ".help")) {
				showHelp(sender);
			} else {
				sender.sendMessage(notAuthorized);
			}
		} else {
			String command = args[0];

			if (command.equalsIgnoreCase("select")) {
				// select
				if (hasPerm(sender, ".trigger.select")) {
					select(sender);
				} else {
					sender.sendMessage(notAuthorized);
				}
			} else if (command.equalsIgnoreCase("mobids")) {
				// mobIDs
				if (hasPerm(sender, ".trigger.showmobids")) {
					showMobIDs(sender);
				} else {
					sender.sendMessage(notAuthorized);
				}
			} else if (command.equalsIgnoreCase("cuboid") && n >= 2) {
				String addenum = args[1];
				// cuboid
				if (addenum != null) {
					if (addenum.equalsIgnoreCase("save")) {
						if (n >= 3 && argsValid(args)) {
							// save
							if (hasPerm(sender, ".trigger.cuboid.save")) {
								saveCuboid(sender, args[2]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"cuboid save", "(cuboidName - the Cuboid name for further references)"});
						}
					} else if (addenum.equalsIgnoreCase("info")) {
						if (n >= 3 && argsValid(args)) {
							// info
							if (hasPerm(sender, ".trigger.cuboid.info")) {
								infoCuboid(sender, args[2]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"cuboid info", "(String cuboidName - the Cuboid name)"});
						}
					} else {
						showInvalidArgsMsg(sender, "cuboid (save|info)");
					}
				} else {
					showInvalidArgsMsg(sender, "cuboid (save|info)");
				}
			} else if (command.equalsIgnoreCase("trigger") && n >= 2) {
				String addenum = args[1];
				// trigger
				if (addenum != null) {
					if (addenum.equalsIgnoreCase("create")) {	
						if (n >= 9 && argsValid(args)) {
							// create
							if (hasPerm(sender, ".trigger.create")) {
								createTrigger(sender, args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"trigger create", "(int triggerID)", "(String cuboidName)", "(double firstDelay - delay in seconds after which the trigger is first fired)", "(boolean isSelfTrigging - determines whether the trigger executes itself after the first triggering)", "(double selfTriggerDelay - delay in seconds after which the trigger triggers itself again)", "(int totalTimes - the total times the trigger can fire)", "(double resetTime - time in seconds until the trigger resets itself)"});
						}	
					} else if (addenum.equalsIgnoreCase("info")) {
						//info
						if (hasPerm(sender, ".trigger.info")) {
							infoTrigger(sender);
						} else {
							sender.sendMessage(notAuthorized);
						}
					} else if (addenum.equalsIgnoreCase("set")) {	
						if (n >= 4 && argsValid(args)) {
							// set
							if (hasPerm(sender, ".trigger.create")) {
								setMobAmount(sender, args[2], args[3]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"trigger set", "(String mobName|int mobID - the name or ID of the mob to set)", "(int amount)"});
						}	
					} else if (addenum.equalsIgnoreCase("link")) {	
						if (n >= 3 && argsValid(args)) {
							// link
							if (hasPerm(sender, ".trigger.create")) {
								linkTrigger(sender, args[2]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, "trigger link (int triggerID - the ID of the trigger to link to)");
						}	
					} else if (addenum.equalsIgnoreCase("unlink")) {	
						// unlink
						if (hasPerm(sender, ".trigger.destroy")) {
							unlinkTrigger(sender);
						} else {
							sender.sendMessage(notAuthorized);
						}
					} else if (addenum.equalsIgnoreCase("reset")) {	
						if (n >= 3 && argsValid(args)) {
							// reset
							if (hasPerm(sender, ".trigger.reset")) {
								resetTrigger(sender, args[2]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"trigger reset", "(int triggerID - the ID of the trigger to reset)"});
						}	
					} else if (addenum.equalsIgnoreCase("showids")) {	
						// show TriggerIDs
						if (hasPerm(sender, ".trigger.info")) {
							showTriggerIDs(sender);
						} else {
							sender.sendMessage(notAuthorized);
						}
					} else {
						showInvalidArgsMsg(sender, "trigger (create|info|link|reset|set|showIDs|unlink)");
					}
				} else {
					showInvalidArgsMsg(sender, "trigger (create|info|link|reset|set|showIDs|unlink)");
				}
			}
		}
	}
	
	private static boolean argsValid(String[] args) {
		boolean result = true;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null || args[i].equals("\\s+")) {
				result = false;
			}
		}
		
		return result;
	}
	
	private void showInvalidArgsMsg(Player sender, String msg) {
		sender.sendMessage(invalidArgs);
		sender.sendMessage(msg);
		sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.GOLD).append("() - required, [] - optional").toString());
	}
	
	private void showInvalidArgsMsg(Player sender, String[] msg) {
		sender.sendMessage(invalidArgs);
		sender.sendMessage(msg[0]);
		
		for (int i = 1; i < msg.length; i++) {
			sender.sendMessage(ChatColor.GREEN + msg[i]);
		}
		
		sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.GOLD).append("() - required, [] - optional").toString());
	}
	
	private boolean hasPerm(Player sender, String perm) {
		return (permissionsFound && ph.has(sender, pluginName.toLowerCase() + perm)) || (sender.hasPermission(pluginName.toLowerCase() + perm) || sender.isOp());

	}

	private void select(Player sender) {
		final HashSet<String> set = plugin.getPlayerListener().playersInSelectionMode;
		
		if (!set.contains(sender.getName())) {
			sender.sendMessage(new StringBuilder(chatPrefix).append("Selection mode: ").append(ChatColor.GREEN).append("ON").toString());
			sender.sendMessage(new StringBuilder(chatPrefix).append("Selection tool: ").append(ChatColor.GREEN).append(Material.getMaterial(plugin.getMTConfig().getSelectionItemId()).toString()).toString());
			set.add(sender.getName());
		} else {
			plugin.getPlayerListener().playersInSelectionMode.remove(sender.getName());
			sender.sendMessage(new StringBuilder(chatPrefix).append("Selection mode: ").append(ChatColor.RED).append("OFF").toString());
		}
	}
	
	private void saveCuboid(Player sender, String cuboidName) {
		final YMLHelper cm = plugin.getMTConfig().getCuboidManager();
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		final HashMap<String, Cuboid> map = plugin.getPlayerListener().triggerCuboid;
		
		final String senderName = sender.getName();
		
		if (map.containsKey(senderName)) {
			final Cuboid newCuboid = map.get(senderName);
			
			if (cm.getMap(cuboidName) == null) {
				cm.addMap(cuboidName, newCuboid.save());
				sender.sendMessage(new StringBuilder(chatPrefix).append("Saving new cuboid: ").append(ChatColor.GREEN).append(cuboidName).toString());
			} else {
				final Cuboid oldCuboid = Cuboid.load(cm.getMap(cuboidName));
				
				if (oldCuboid.getOwner().equals(senderName) || hasPerm(sender, ".admin.cuboid")) {
					final ArrayList<Trigger> list = tc.getTriggersByCuboid(oldCuboid);
					for (Trigger t : list) {
						final Trigger temp = new Trigger(plugin, t.getID(), t.getOwner(), newCuboid, t.getFirstDelay(), t.isSelfTriggering(), t.getSelfTriggerDelay(), t.getTotalTimes(), t.getResetTime());
						temp.setAmountOfMobs(t.getAmountOfMobs());
						tc.updateTrigger(temp);
						tc.addMap("" + temp.getID(), temp.save());
					}
					
					cm.addMap(cuboidName, newCuboid.save());
					sender.sendMessage(new StringBuilder(chatPrefix).append("Overwriting old cuboid: ").append(ChatColor.GREEN).append(cuboidName).toString());
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of the existing cuboid ").append(cuboidName).append("!").toString());
				}
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You have no cuboid selected!").toString());
		}
	}
	
	private void infoCuboid(Player sender, String cuboidName) {
		final YMLHelper cm = plugin.getMTConfig().getCuboidManager();
		
		if (cm.getMap(cuboidName) != null) {
			final Cuboid c = Cuboid.load(cm.getMap(cuboidName));
			
			if (c.getOwner().equals(sender.getName()) || hasPerm(sender, ".admin.cuboid")) {
				sender.sendMessage(new StringBuilder(chatPrefix).append("Cuboid ").append(ChatColor.GREEN).append(cuboidName).append(ChatColor.WHITE).append(" owned by ").append(ChatColor.GREEN).append(c.getOwner()).append(ChatColor.WHITE).append(" is at ").append(ChatColor.GREEN).append(c.toString()).toString());
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of the cuboid ").append(cuboidName).append("!").toString());
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Cuboid with name ").append(cuboidName).append(" does not exist!").toString());
		}
	}
	
	private void createTrigger(Player sender, String triggerId, String cuboidName, String firstDelay, String selfTriggering, String selfTriggerDelay, String totalTimes, String resetTime) {
		final YMLHelper cm = plugin.getMTConfig().getCuboidManager();
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		final String senderName = sender.getName();
		
		if (cm.getMap(cuboidName) != null) {
			final Cuboid c = Cuboid.load(cm.getMap(cuboidName));
			
			if (c.getOwner().equals(senderName) || hasPerm(sender, ".admin.cuboid")) {
				if (pl.selectedTriggerBlock.containsKey(senderName) && Util.isValidType(pl.selectedTriggerBlock.get(senderName).getBlock().getType())) {
					int id = -1;
					double fDelay = 0.0;
					boolean sTriggering = Boolean.parseBoolean(selfTriggering);
					double stDelay = 0.0;
					int times = 1;
					double rTime = 0.0;
					
					try {
						id = Integer.parseInt(triggerId);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid trigger ID: ").append(triggerId).toString());
					}
					
					try {
						fDelay = Double.parseDouble(firstDelay);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid first delay: ").append(firstDelay).toString());
					}
					
					try {
						stDelay = Double.parseDouble(selfTriggerDelay);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid self-trigger delay: ").append(selfTriggerDelay).toString());
					}
					
					try {
						times = Integer.parseInt(totalTimes);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid amount of times: ").append(totalTimes).toString());
					}
					
					try {
						rTime = Double.parseDouble(resetTime);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid reset time: ").append(resetTime).toString());
					}
					
					
					if (id != -1) {
						final Location selectedBlock = pl.selectedTriggerBlock.get(senderName);
						final Trigger t = new Trigger(plugin, id, senderName, c, fDelay, sTriggering, stDelay, times, rTime);
						
						tc.addReferenceToTrigger(selectedBlock, t);
						tc.updateTrigger(t);
						tc.addMap(triggerId, t.save());
						sender.sendMessage(new StringBuilder(chatPrefix).append("Successfully created a trigger!").toString());
					}
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not select a trigger block yet!").toString());
				}
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of the cuboid ").append(cuboidName).append("!").toString());
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Cuboid with name ").append(cuboidName).append(" does not exist!").toString());
		}
		
	}
	
	private void linkTrigger(Player sender, String triggerId) {
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		final String senderName = sender.getName();
		
		if (pl.selectedTriggerBlock.containsKey(senderName) && Util.isValidType(pl.selectedTriggerBlock.get(senderName).getBlock().getType())) {
			int id = -1;
			
			try {
				id = Integer.parseInt(triggerId);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid trigger ID: ").append(triggerId).toString());
			}
			
			if (id != -1) {
				final Location selectedBlock = pl.selectedTriggerBlock.get(senderName);
				final Trigger t = tc.getTriggerByID(id);
				
				if (t != null) {
					if (t.getOwner().equals(senderName) || hasPerm(sender, ".admin.trigger")) {
						// remove old reference in case one exists
						if (tc.getTrigger(selectedBlock) != null) {
							tc.removeReferenceToTrigger(selectedBlock, tc.getTrigger(selectedBlock));
						}
						
						// add new reference
						tc.addReferenceToTrigger(selectedBlock, t);
						sender.sendMessage(new StringBuilder(chatPrefix).append("Successfully linked ").append(ChatColor.GREEN).append(selectedBlock.getBlock().getType().toString()).append(ChatColor.WHITE).append(" to Trigger (ID=").append(ChatColor.GREEN).append(id).append(ChatColor.WHITE).append(")").toString());
					} else {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of Trigger ID=").append(id).append("!").toString());
					}
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Trigger with ID=").append(id).append(" does not exist!").toString());
				}
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not select a trigger block yet!").toString());
		}
	}
	
	private void unlinkTrigger(Player sender) {
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		final String senderName = sender.getName();
		
		if (pl.selectedTriggerBlock.containsKey(senderName) && Util.isValidType(pl.selectedTriggerBlock.get(senderName).getBlock().getType())) {
			final Location selectedBlock = pl.selectedTriggerBlock.get(senderName);
			final Trigger t = tc.getTrigger(selectedBlock);
			
			if (t != null) {
				if (t.getOwner().equals(senderName) || hasPerm(sender, ".admin.trigger")) {
					// remove the reference
					tc.removeReferenceToTrigger(selectedBlock, t);
					sender.sendMessage(new StringBuilder(chatPrefix).append("Successfully removed link to Trigger (ID=").append(ChatColor.GREEN).append(t.getID()).append(ChatColor.WHITE).append(") from ").append(ChatColor.GREEN).append(selectedBlock.getBlock().getType().toString()).toString());	
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of Trigger ID=").append(t.getID()).append("!").toString());
				}
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("The selected block does not contain a trigger!").toString());
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not select a trigger block yet!").toString());
		}
	}
	
	private void resetTrigger(Player sender, String triggerId) {
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		
		int id = -1;
		
		try {
			id = Integer.parseInt(triggerId);
		} catch (NumberFormatException nfe) {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid trigger ID: ").append(triggerId).toString());
		}
		
		if (id != -1) {
			final Trigger t = tc.getTriggerByID(id);
			
			if (t != null) {
				if (t.getOwner().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					t.reset();
					sender.sendMessage(new StringBuilder(chatPrefix).append("Successfully reset Trigger (ID=").append(ChatColor.GREEN).append(id).append(ChatColor.WHITE).append(")").toString());
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of Trigger ID=").append(t.getID()).append("!").toString());
				}
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Trigger with ID=").append(id).append(" does not exist!").toString());
			}
		}
	}
	
	private void setMobAmount(Player sender, String type, String amount) {
		final MTPlayerListener pl = plugin.getPlayerListener();
		final HashMap<String, Location> selectedTriggerMap = pl.selectedTriggerBlock;
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		
		final String senderName = sender.getName();
		
		if (selectedTriggerMap.containsKey(senderName)) {
			final Location key = selectedTriggerMap.get(senderName);
			
			if (tc.getTrigger(key) != null) {
				final Trigger t = tc.getTrigger(key);
				
				if (t.getOwner().equals(senderName) || hasPerm(sender, ".admin.trigger")) {
					int id = -1;
					CreatureType ct = null;
					
					try {
						id = Integer.parseInt(type);
						ct = Util.getMobNameById(id);
					} catch (NumberFormatException nfe) {}
					
					if (id == -1) {
						ct = CreatureType.fromName(Util.convertFromFriendlyMobString(type));
					}
					
					if (ct != null) {
						int am = -1;
						
						try {
							am = Integer.parseInt(amount);
						} catch (NumberFormatException nfe) {
							sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Given amount is not a number!").toString());
						}
						
						if (am >= 0) {
							t.setAmountOfMobType(ct, am);
							tc.addMap("" + t.getID(), t.save());
							sender.sendMessage(new StringBuilder(chatPrefix).append("Amount of ").append(ChatColor.GREEN).append(ct.getName()).append(ChatColor.WHITE).append(" set to ").append(ChatColor.GREEN).append(am).toString());
						} else {
							sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Amount must be greater or equal zero!").toString());
						}
					
					} else {
						sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("Invalid mob name or mob ID!").toString());
					}
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of Trigger ID=").append(t.getID()).append("!").toString());
				}
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("This block contains no trigger!").toString());
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not select a trigger block yet!").toString());
		}
	}
	
	/*---------------------------*/
	/* COMMANDS CONTAINING BOOKS */
	/*---------------------------*/
	
	private void showMobIDs(Player sender) {
		// reserved page indices: 1-3
		final int reservedPageStartIndex = 1;
		final int reservedPages = 3;
		
		final int bookID = 0;
		
		final String senderName = sender.getName();
		
		if (!pageIndex.containsKey(senderName) || (currentBookID.containsKey(senderName) && currentBookID.get(senderName) != bookID)) {
			pageIndex.put(senderName, 0);
		}
		
		currentBookID.put(senderName, bookID);
		
		if (pageIndex.get(senderName) == 0) {
			// dummy trigger			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			// first page
			Page p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Mob IDs (").append(1).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			
			int i = 0;
			
			for (; i < 6; i++) {
				p.addLine(new StringBuilder("[").append(ChatColor.GREEN).append(i).append(ChatColor.WHITE).append("] ").append(ChatColor.GREEN).append(Util.getMobNameById(i).getName()).toString());
			}
			
			p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());

			book.addPage(p);
			
			// second page
			p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Mob IDs (").append(2).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			
			for (; i < 12; i++) {
				p.addLine(new StringBuilder("[").append(ChatColor.GREEN).append(i).append(ChatColor.WHITE).append("] ").append(ChatColor.GREEN).append(Util.getMobNameById(i).getName()).toString());
			}
			
			p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());
			
			book.addPage(p);
			
			// third page
			p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Mob IDs (").append(3).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			
			for (; i < 17; i++) {
				p.addLine(new StringBuilder("[").append(ChatColor.GREEN).append(i).append(ChatColor.WHITE).append("] ").append(ChatColor.GREEN).append(Util.getMobNameById(i).getName()).toString());
			}
			
			book.addPage(p);
			
			currentBook.put(senderName, book);
		}
		
		showPage(reservedPageStartIndex, reservedPages, sender);
	}
	
	private void infoTrigger(Player sender) {
		// reserved page indices: 10-12
		final int reservedPageStartIndex = 10;
		final int reservedPages = 3;
		
		final int bookID = 1;
		
		final String senderName = sender.getName();
		
		final MTPlayerListener pl = plugin.getPlayerListener();
		final HashMap<String, Location> selectedTriggerMap = pl.selectedTriggerBlock;
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();

		if (selectedTriggerMap.containsKey(senderName)) {
			final Location key = selectedTriggerMap.get(senderName);
			
			if (tc.getTrigger(key) != null) {
				final Trigger t = tc.getTrigger(key);
				
				if (t.getOwner().equals(senderName) || hasPerm(sender, ".admin.trigger")) {
					if (!pageIndex.containsKey(senderName) || (currentBookID.containsKey(senderName) && currentBookID.get(senderName) != bookID)) {
						pageIndex.put(senderName, 0);
					}
					
					currentBookID.put(senderName, bookID);
					
					if (pageIndex.get(senderName) == 0) {	
						final Cuboid c = t.getCuboid();
						
						final int totalPages = reservedPages;
						
						final Book book = new Book();
						
						// first page
						Page p = new Page();
						
						p.addLine(new StringBuilder(chatPrefix).append("Trigger information (").append(1).append("/").append(totalPages).append(")").toString());
						p.addLine("------------------------");
						p.addLine(new StringBuilder("ID: ").append(ChatColor.GREEN).append(t.getID()).toString());
						p.addLine(new StringBuilder("Owner: ").append(ChatColor.GREEN).append(t.getOwner()).toString());
						p.addLine(new StringBuilder("Cuboid: ").append(ChatColor.GREEN).append(c.toString()).append(ChatColor.WHITE).append(" owned by ").append(ChatColor.GREEN).append(c.getOwner()).append(ChatColor.WHITE).append(" in world ").append(ChatColor.GREEN).append(c.getWorld().getName()).toString());
						p.addLine(new StringBuilder("First execution delay: ").append(ChatColor.GREEN).append(t.getFirstDelay()).append(" seconds").toString());
						p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());
					
						book.addPage(p);
						
						// second page
						p = new Page();
						
						p.addLine(new StringBuilder(chatPrefix).append("Trigger information (").append(2).append("/").append(totalPages).append(")").toString());
						p.addLine("------------------------");
						p.addLine(new StringBuilder("Is self-triggering: ").append(ChatColor.GREEN).append(t.isSelfTriggering()).toString());
						p.addLine(new StringBuilder("Consecutive execution delay: ").append(ChatColor.GREEN).append(t.getSelfTriggerDelay()).append(" seconds").toString());
						p.addLine(new StringBuilder("Total times: ").append(ChatColor.GREEN).append(t.getTotalTimes()).toString());
						p.addLine(new StringBuilder("Remaining times: ").append(ChatColor.GREEN).append(t.getRemainingTimes()).toString());
						p.addLine(new StringBuilder("Reset time: ").append(ChatColor.GREEN).append(t.getResetTime()).append(" seconds").toString());
						p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());

						book.addPage(p);
						
						// third page
						p = new Page();
						
						p.addLine(new StringBuilder(chatPrefix).append("Trigger information (").append(3).append("/").append(totalPages).append(")").toString());
						p.addLine("------------------------");
						p.addLine("Spawning:");

						StringBuilder list = new StringBuilder("  ");
						final int n = Trigger.NUMBER_OF_CREATURE_TYPES;
						
						for (int i = 0; i < n ; i++) {
							CreatureType ct = Util.getMobNameById(i);
							int amount = t.getAmountOfMobType(ct);
							
							if (amount != 0) {
								list.append(ct.getName()).append("[").append(amount).append("] ");
							}
						}
						
						if (!list.equals("  ")) {
							p.addLine(new StringBuilder("").append(ChatColor.GREEN).append(list.toString()).toString());
						} else {
							p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("  NOTHING").toString());
						}
						
						book.addPage(p);
						
						currentBook.put(senderName, book);
					}
					
					showPage(reservedPageStartIndex, reservedPages, sender);
				} else {
					sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You are not the owner of Trigger ID=").append(t.getID()).append("!").toString());
				}
			} else {
				sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not create a trigger for this block yet!").toString());
			}
		} else {
			sender.sendMessage(new StringBuilder(chatPrefix).append(ChatColor.RED).append("You did not select a trigger block yet!").toString());
		}
	}
	
	private void showTriggerIDs(Player sender) {
		// reserved page indices: 20
		final int reservedPageStartIndex = 20;
		final int reservedPages = 1;
		
		final int bookID = 2;
		
		final String senderName = sender.getName();
		
		final TriggerCollection tc = plugin.getMTConfig().getTriggerCollection();
		final ArrayList<Trigger> list = tc.getAllTriggers();
		
		final ArrayList<Trigger> toDisplay = new ArrayList<Trigger>();
		
		if (!pageIndex.containsKey(senderName) || (currentBookID.containsKey(senderName) && currentBookID.get(senderName) != bookID)) {
			pageIndex.put(senderName, 0);
		}
		
		currentBookID.put(senderName, bookID);
		
		if (pageIndex.get(senderName) == 0) {
			for (Trigger t : list) {
				if (t.getOwner().equals(senderName) || hasPerm(sender, ".admin.trigger")) {
					toDisplay.add(t);
				}
			}
			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			Page p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Trigger IDs (").append(1).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			
			StringBuilder s = new StringBuilder("");
			
			for (int j = 0; j < toDisplay.size(); j++) {
				s.append(ChatColor.GREEN).append(toDisplay.get(j).getID()).append(ChatColor.WHITE).append(", ");
			}
			
			s.delete(s.length() - 1, s.length());
			p.addLine(s.toString());
			
			book.addPage(p);
			
			currentBook.put(senderName, book);
		}
		
		showPage(reservedPageStartIndex, reservedPages, sender);
	}
	
	private void showHelp(Player sender) {
		// reserved page indices: 30-33
		final int reservedPageStartIndex = 30;
		final int reservedPages = 4;
		
		final int bookID = 3;
		
		final String senderName = sender.getName();
		
		if (!pageIndex.containsKey(senderName) || (currentBookID.containsKey(senderName) && currentBookID.get(senderName) != bookID)) {
			pageIndex.put(senderName, 0);
		}
		
		currentBookID.put(senderName, bookID);
		
		if (pageIndex.get(senderName) == 0) {			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			// first page
			Page p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Command help (").append(1).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			p.addLine("All commands are case-insensitive.");
			p.addLine("Aliases: /mobtrigger /mt");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt select").append(ChatColor.WHITE).append(" - Switches to selection mode").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt mobIDs").append(ChatColor.WHITE).append(" - Shows a list of all mob IDs used by the plug-in").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());
			
			book.addPage(p);
			
			// second page
			p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Command help (").append(2).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt cuboid save").append(ChatColor.WHITE).append(" - Saves a selected cuboid under the").toString());
			p.addLine("    specified name");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt cuboid info").append(ChatColor.WHITE).append(" - Shows information about the specified cuboid").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger create").append(ChatColor.WHITE).append(" - Creates a trigger, enter the command").toString());
			p.addLine("    for more information");
			p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());
			
			book.addPage(p);
			
			// third page
			p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Command help (").append(3).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger info").append(ChatColor.WHITE).append(" - Shows information about the specified").toString());
			p.addLine("    trigger");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger link").append(ChatColor.WHITE).append(" - Links a trigger block to a trigger").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger unlink").append(ChatColor.WHITE).append(" - Removes a link from a trigger block").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GOLD).append("Enter the command again to display the next page.").toString());
			
			book.addPage(p);
			
			// fourth page
			p = new Page();
			
			p.addLine(new StringBuilder(chatPrefix).append("Command help (").append(4).append("/").append(totalPages).append(")").toString());
			p.addLine("------------------------");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger reset").append(ChatColor.WHITE).append(" - Reset a trigger, also kills all mobs").toString());
			p.addLine("    spawned by the trigger");
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger showIDs").append(ChatColor.WHITE).append(" - Shows a list of all triggers you own").toString());
			p.addLine(new StringBuilder("").append(ChatColor.GREEN).append("/mt trigger set").append(ChatColor.WHITE).append(" - Sets the amount of mobs spawned by the").toString());
			p.addLine("    trigger");
			
			book.addPage(p);
				
			currentBook.put(senderName, book);
		}
	
		showPage(reservedPageStartIndex, reservedPages, sender);
	}
	
	private void showPage(int reservedPageStartIndex, int reservedPages, Player sender) {
		final String senderName = sender.getName();
		final int currentPage = pageIndex.get(senderName) >= reservedPageStartIndex ? pageIndex.get(senderName) - reservedPageStartIndex : 0;
		
		if (currentPage < currentBook.get(senderName).size()) {
			currentBook.get(senderName).getPage(currentPage).displayPage(sender, "");
			
			if (reservedPageStartIndex + currentPage + 1 < reservedPageStartIndex + reservedPages) {
				pageIndex.put(senderName, reservedPageStartIndex + currentPage + 1);
			} else {
				pageIndex.put(senderName, 0);
			}
		} else {
			pageIndex.put(senderName, 0);
		}
	}
}
