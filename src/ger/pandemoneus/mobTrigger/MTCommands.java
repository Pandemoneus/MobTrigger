package ger.pandemoneus.mobTrigger;

import ger.pandemoneus.mobTrigger.util.Book;
import ger.pandemoneus.mobTrigger.util.Cuboid;
import ger.pandemoneus.mobTrigger.util.Page;
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
	
	private final HashMap<Player, Integer> pageIndex = new HashMap<Player, Integer>();
	private final HashMap<Player, Book> currentBook = new HashMap<Player, Book>();
	private final HashMap<Player, Integer> currentBookID = new HashMap<Player, Integer>();

	/**
	 * Associates this object with a plugin
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public MTCommands(MobTrigger plugin) {
		this.plugin = plugin;
		pluginName = plugin.getPluginName();
		
		chatPrefix = ChatColor.WHITE + "[" + ChatColor.GOLD + pluginName + ChatColor.WHITE + "] ";
		notAuthorized = chatPrefix + ChatColor.RED + "You are not authorized to use this command.";
		invalidArgs = chatPrefix + ChatColor.RED + "Too few or invalid arguments. Usage:";
		
		permissionsFound = plugin.getPermissionsFound();
		if (permissionsFound) {
			ph = plugin.getPermissionsHandler();
		}
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
				sender.sendMessage(chatPrefix + ChatColor.RED + "Sorry, you are not a player!");
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
						if (n >= 8 && argsValid(args)) {
							// create
							if (hasPerm(sender, ".trigger.create")) {
								createTrigger(sender, args[2], args[3], args[4], args[5], args[6], args[7]);
							} else {
								sender.sendMessage(notAuthorized);
							}
						} else {
							showInvalidArgsMsg(sender, new String[]{"trigger create", "(int triggerID)", "(String cuboidName)", "(double firstDelay - delay in seconds after which the trigger is first fired)", "(boolean isSelfTrigging - determines whether the trigger executes itself after the first triggering)", "(double selfTriggerDelay - delay in seconds after which the trigger triggers itself again)", "(int totalTimes - the total times the trigger can fire)"});
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
	
	private boolean argsValid(String[] args) {
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
		sender.sendMessage(chatPrefix + ChatColor.GOLD + "() - required, [] - optional");
	}
	
	private void showInvalidArgsMsg(Player sender, String[] msg) {
		sender.sendMessage(invalidArgs);
		sender.sendMessage(msg[0]);
		
		for (int i = 1; i < msg.length; i++) {
			sender.sendMessage(ChatColor.GREEN + msg[i]);
		}
		
		sender.sendMessage(chatPrefix + ChatColor.GOLD + "() - required, [] - optional");
	}
	
	private boolean hasPerm(Player sender, String perm) {
		return (permissionsFound && ph.has(sender, pluginName.toLowerCase() + perm)) || (sender.hasPermission(pluginName.toLowerCase() + perm));

	}

	private void select(Player sender) {
		final HashSet<Player> set = plugin.getPlayerListener().playersInSelectionMode;
		
		if (!set.contains(sender)) {
			sender.sendMessage(chatPrefix + "Selection mode: " + ChatColor.GREEN + "ON");
			sender.sendMessage(chatPrefix + "Selection tool: " + ChatColor.GREEN + Material.getMaterial(plugin.getConfig().getSelectionItemId()).toString());
			set.add(sender);
		} else {
			plugin.getPlayerListener().playersInSelectionMode.remove(sender);
			sender.sendMessage(chatPrefix + "Selection mode: " + ChatColor.RED + "OFF");
		}
	}
	
	private void saveCuboid(Player sender, String cuboidName) {
		final YMLHelper cm = plugin.getConfig().getCuboidManager();
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final HashMap<Player, Cuboid> map = plugin.getPlayerListener().triggerCuboid;
		
		if (map.containsKey(sender)) {
			final Cuboid newCuboid = map.get(sender);
			
			if (cm.getMap(cuboidName) == null) {
				cm.addMap(cuboidName, newCuboid.save());
				sender.sendMessage(chatPrefix + "Saving new cuboid: " + ChatColor.GREEN + cuboidName);
			} else {
				final Cuboid oldCuboid = Cuboid.load(cm.getMap(cuboidName));
				
				if (oldCuboid.getOwner().getName().equals(sender.getName()) || ((permissionsFound && plugin.getPermissionsHandler().has(sender, pluginName.toLowerCase() + ".admin.cuboid")) || (sender.hasPermission(pluginName.toLowerCase() + ".admin.cuboid")))) {
					final ArrayList<Trigger> list = tc.getTriggersByCuboid(oldCuboid);
					for (Trigger t : list) {
						final Trigger temp = new Trigger(plugin, t.getID(), t.getOwner(), newCuboid, t.getFirstDelay(), t.isSelfTriggering(), t.getSelfTriggerDelay(), t.getTotalTimes());
						temp.setAmountOfMobs(t.getAmountOfMobs());
						tc.updateTrigger(temp);
						tc.addMap("" + temp.getID(), temp.save());
					}
					
					cm.addMap(cuboidName, newCuboid.save());
					sender.sendMessage(chatPrefix + "Overwriting old cuboid: " + ChatColor.GREEN + cuboidName);
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of the existing cuboid " + cuboidName + "!");
				}
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "You have no cuboid selected!");
		}
	}
	
	private void infoCuboid(Player sender, String cuboidName) {
		final YMLHelper cm = plugin.getConfig().getCuboidManager();
		
		if (cm.getMap(cuboidName) != null) {
			final Cuboid c = Cuboid.load(cm.getMap(cuboidName));
			
			if (c.getOwner().getName().equals(sender.getName()) || ((permissionsFound && plugin.getPermissionsHandler().has(sender, pluginName.toLowerCase() + ".admin.cuboid")) || (sender.hasPermission(pluginName.toLowerCase() + ".admin.cuboid")))) {
				sender.sendMessage(chatPrefix + "Cuboid " + ChatColor.GREEN + cuboidName + ChatColor.WHITE + " owned by " + ChatColor.GREEN + c.getOwner().getName() + ChatColor.WHITE + " is at " + ChatColor.GREEN + c.toString());
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of the cuboid " + cuboidName + "!");
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "Cuboid with name " + cuboidName + " does not exist!");
		}
	}
	
	private void createTrigger(Player sender, String triggerId, String cuboidName, String firstDelay, String selfTriggering, String selfTriggerDelay, String totalTimes) {
		final YMLHelper cm = plugin.getConfig().getCuboidManager();
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		if (cm.getMap(cuboidName) != null) {
			final Cuboid c = Cuboid.load(cm.getMap(cuboidName));
			
			if (c.getOwner().getName().equals(sender.getName()) || ((permissionsFound && plugin.getPermissionsHandler().has(sender, pluginName.toLowerCase() + ".admin.cuboid")) || (sender.hasPermission(pluginName.toLowerCase() + ".admin.cuboid")))) {
				if (pl.selectedTriggerBlock.containsKey(sender) && pl.isValidType(pl.selectedTriggerBlock.get(sender).getBlock().getType())) {
					int id = -1;
					double fDelay = 0.0;
					boolean sTriggering = Boolean.parseBoolean(selfTriggering);
					double stDelay = 0.0;
					int times = 1;
					
					try {
						id = Integer.parseInt(triggerId);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid trigger ID: " + triggerId);
					}
					
					try {
						fDelay = Double.parseDouble(firstDelay);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid first delay: " + firstDelay);
					}
					
					try {
						stDelay = Double.parseDouble(selfTriggerDelay);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid self-trigger delay: " + selfTriggerDelay);
					}
					
					try {
						times = Integer.parseInt(totalTimes);
					} catch (NumberFormatException nfe) {
						sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid amount of times: " + selfTriggerDelay);
					}
					
					
					if (id != -1) {
						final Location selectedBlock = pl.selectedTriggerBlock.get(sender);
						final Trigger t = new Trigger(plugin, id, sender, c, fDelay, sTriggering, stDelay, times);
						
						tc.addReferenceToTrigger(selectedBlock, t);
						tc.updateTrigger(t);
						tc.addMap(triggerId, t.save());
						sender.sendMessage(chatPrefix + "Successfully created a trigger!");
					}
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You did not select a trigger block yet!");
				}
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of the cuboid " + cuboidName + "!");
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "Cuboid with name " + cuboidName + " does not exist!");
		}
		
	}
	
	private void linkTrigger(Player sender, String triggerId) {
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		if (pl.selectedTriggerBlock.containsKey(sender) && pl.isValidType(pl.selectedTriggerBlock.get(sender).getBlock().getType())) {
			int id = -1;
			
			try {
				id = Integer.parseInt(triggerId);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid trigger ID: " + triggerId);
			}
			
			if (id != -1) {
				final Location selectedBlock = pl.selectedTriggerBlock.get(sender);
				final Trigger t = tc.getTriggerByID(id);
				
				if (t != null) {
					if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
						// remove old reference in case one exists
						if (tc.getTrigger(selectedBlock) != null) {
							tc.removeReferenceToTrigger(selectedBlock, tc.getTrigger(selectedBlock));
						}
						
						// add new reference
						tc.addReferenceToTrigger(selectedBlock, t);
						sender.sendMessage(chatPrefix + "Successfully linked " + ChatColor.GREEN + selectedBlock.getBlock().getType().toString() + ChatColor.WHITE + " to Trigger (ID=" + ChatColor.GREEN + id + ChatColor.WHITE + ")");
					} else {
						sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of Trigger ID=" + id + "!");
					}
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "Trigger with ID=" + id + " does not exist!");
				}
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "You did not select a trigger block yet!");
		}
	}
	
	private void unlinkTrigger(Player sender) {
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final MTPlayerListener pl = plugin.getPlayerListener();
		
		if (pl.selectedTriggerBlock.containsKey(sender) && pl.isValidType(pl.selectedTriggerBlock.get(sender).getBlock().getType())) {
			final Location selectedBlock = pl.selectedTriggerBlock.get(sender);
			final Trigger t = tc.getTrigger(selectedBlock);
			
			if (t != null) {
				if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					// remove the reference
					tc.removeReferenceToTrigger(selectedBlock, t);
					sender.sendMessage(chatPrefix + "Successfully removed link to Trigger (ID=" + ChatColor.GREEN + t.getID() + ChatColor.WHITE + ") from " + ChatColor.GREEN + selectedBlock.getBlock().getType().toString());	
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of Trigger ID=" + t.getID() + "!");
				}
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "The selected block does not contain a trigger!");
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "You did not select a trigger block yet!");
		}
	}
	
	private void resetTrigger(Player sender, String triggerId) {
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		
		int id = -1;
		
		try {
			id = Integer.parseInt(triggerId);
		} catch (NumberFormatException nfe) {
			sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid trigger ID: " + triggerId);
		}
		
		if (id != -1) {
			final Trigger t = tc.getTriggerByID(id);
			
			if (t != null) {
				if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					t.reset();
					sender.sendMessage(chatPrefix + "Successfully reset Trigger (ID=" + ChatColor.GREEN + id + ChatColor.WHITE + ")");
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of Trigger ID=" + t.getID() + "!");
				}
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "Trigger with ID=" + id + " does not exist!");
			}
		}
	}
	
	private void setMobAmount(Player sender, String type, String amount) {
		final MTPlayerListener pl = plugin.getPlayerListener();
		final HashMap<Player, Location> selectedTriggerMap = pl.selectedTriggerBlock;
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		
		if (selectedTriggerMap.containsKey(sender)) {
			final Location key = selectedTriggerMap.get(sender);
			
			if (tc.getTrigger(key) != null) {
				final Trigger t = tc.getTrigger(key);
				
				if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					int id = -1;
					CreatureType ct = null;
					
					try {
						id = Integer.parseInt(type);
						ct = t.getMobNameById(id);
					} catch (NumberFormatException nfe) {}
					
					if (id == -1) {
						ct = CreatureType.fromName(t.convertFromFriendlyMobString(type));
					}
					
					if (ct != null) {
						int am = -1;
						
						try {
							am = Integer.parseInt(amount);
						} catch (NumberFormatException nfe) {
							sender.sendMessage(chatPrefix + ChatColor.RED + "Given amount is not a number!");
						}
						
						if (am >= 0) {
							t.setAmountOfMobType(ct, am);
							tc.addMap("" + t.getID(), t.save());
							sender.sendMessage(chatPrefix + "Amount of " + ChatColor.GREEN + ct.getName() + ChatColor.WHITE + " set to " + ChatColor.GREEN + am);
						} else {
							sender.sendMessage(chatPrefix + ChatColor.RED + "Amount must be greater or equal zero!");
						}
					
					} else {
						sender.sendMessage(chatPrefix + ChatColor.RED + "Invalid mob name or mob ID!");
					}
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of Trigger ID=" + t.getID() + "!");
				}
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "This block contains no trigger!");
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "You did not select a trigger block yet!");
		}
	}
	
	/*---------------------------*/
	/* COMMANDS CONTAINING BOOKS */
	/*---------------------------*/
	
	private void showMobIDs(Player sender) {
		// reserved page indices: 1-2
		final int reservedPageStartIndex = 1;
		final int reservedPages = 2;
		
		final int bookID = 0;
		
		if (!pageIndex.containsKey(sender) || (currentBookID.containsKey(sender) && currentBookID.get(sender) != bookID)) {
			pageIndex.put(sender, 0);
		}
		
		currentBookID.put(sender, bookID);
		
		if (pageIndex.get(sender) == 0) {
			// dummy trigger
			final Trigger t = new Trigger();
			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			// first page
			Page p = new Page();
			
			p.addLine(chatPrefix + "Mob IDs (" + 1 + "/" + totalPages + ")");
			p.addLine("------------------------");
			
			for (int i = 0; i < 5; i++) {
				p.addLine("[" + ChatColor.GREEN + i + ChatColor.WHITE + "] " + ChatColor.GREEN + t.getMobNameById(i).getName());
			}
			
			p.addLine(ChatColor.GOLD + "Enter the command again to display the next page.");

			book.addPage(p);
			
			// second page
			p = new Page();
			
			p.addLine(chatPrefix + "Mob IDs (" + 2 + "/" + totalPages + ")");
			p.addLine("------------------------");
			
			for (int i = 7; i < 13; i++) {
				p.addLine("[" + ChatColor.GREEN + i + ChatColor.WHITE + "] " + ChatColor.GREEN + t.getMobNameById(i).getName());
			}
			
			book.addPage(p);
			
			currentBook.put(sender, book);
		}
		
		final int currentPage = pageIndex.get(sender) >= reservedPageStartIndex ? pageIndex.get(sender) - reservedPageStartIndex : 0;
		
		if (currentPage < currentBook.get(sender).size()) {
			currentBook.get(sender).getPage(currentPage).displayPage(sender, "");
			
			if (reservedPageStartIndex + currentPage + 1 < reservedPageStartIndex + reservedPages) {
				pageIndex.put(sender, reservedPageStartIndex + currentPage + 1);
			} else {
				pageIndex.put(sender, 0);
			}
		} else {
			pageIndex.put(sender, 0);
		}
	}
	
	private void infoTrigger(Player sender) {
		// reserved page indices: 3-4
		final int reservedPageStartIndex = 3;
		final int reservedPages = 2;
		
		final int bookID = 1;
		
		final MTPlayerListener pl = plugin.getPlayerListener();
		final HashMap<Player, Location> selectedTriggerMap = pl.selectedTriggerBlock;
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();

		if (selectedTriggerMap.containsKey(sender)) {
			final Location key = selectedTriggerMap.get(sender);
			
			if (tc.getTrigger(key) != null) {
				final Trigger t = tc.getTrigger(key);
				
				if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					if (!pageIndex.containsKey(sender) || (currentBookID.containsKey(sender) && currentBookID.get(sender) != bookID)) {
						pageIndex.put(sender, 0);
					}
					
					currentBookID.put(sender, bookID);
					
					if (pageIndex.get(sender) == 0) {	
						final Cuboid c = t.getCuboid();
						
						final int totalPages = reservedPages;
						
						final Book book = new Book();
						
						// first page
						Page p = new Page();
						
						p.addLine(chatPrefix + "Trigger information (" + 1 + "/" + totalPages + ")");
						p.addLine("------------------------");
						p.addLine("ID: " + ChatColor.GREEN + t.getID());
						p.addLine("Owner: " + ChatColor.GREEN + t.getOwner().getName());
						p.addLine("Cuboid: " + ChatColor.GREEN + c.toString() + ChatColor.WHITE + " owned by " + ChatColor.GREEN + c.getOwner().getName() + ChatColor.WHITE + " in world " + ChatColor.GREEN + c.getWorld().getName());
						p.addLine("First execution delay: " + ChatColor.GREEN + t.getFirstDelay() + " seconds");
						p.addLine(ChatColor.GOLD + "Enter the command again to display the next page.");
					
						book.addPage(p);
						
						// second page
						p = new Page();
						
						p.addLine(chatPrefix + "Trigger information (" + 2 + "/" + totalPages + ")");
						p.addLine("------------------------");
						p.addLine("Is self-triggering: " + ChatColor.GREEN + t.isSelfTriggering());
						p.addLine("Consecutive execution delay: " + ChatColor.GREEN + t.getSelfTriggerDelay() + " seconds");
						p.addLine("Total times: " + ChatColor.GREEN + t.getTotalTimes());
						p.addLine("Remaining times: " + ChatColor.GREEN + t.getRemainingTimes());
						p.addLine("Spawning:");
						
						String list = "  ";
						
						for (int i = 0; i < 13; i++) {
							final CreatureType ct = t.getMobNameById(i);
							int amount = t.getAmountOfMobType(ct);
							
							if (amount != 0) {
								list += ct.getName() + "[" + amount + "] ";
							}
						}
						
						if (!list.equals("  ")) {
							p.addLine(ChatColor.GREEN + list);
						} else {
							p.addLine(ChatColor.GREEN + "  NOTHING");
						}
						
						book.addPage(p);
						
						currentBook.put(sender, book);
					}
					final int currentPage = pageIndex.get(sender) >= reservedPageStartIndex ? pageIndex.get(sender) - reservedPageStartIndex : 0;
					
					if (currentPage < currentBook.get(sender).size()) {
						currentBook.get(sender).getPage(currentPage).displayPage(sender, "");
						
						if (reservedPageStartIndex + currentPage + 1 < reservedPageStartIndex + reservedPages) {
							pageIndex.put(sender, reservedPageStartIndex + currentPage + 1);
						} else {
							pageIndex.put(sender, 0);
						}
					} else {
						pageIndex.put(sender, 0);
					}
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "You are not the owner of Trigger ID=" + t.getID() + "!");
				}
			} else {
				sender.sendMessage(chatPrefix + ChatColor.RED + "You did not create a trigger for this block yet!");
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "You did not select a trigger block yet!");
		}
	}
	
	private void showTriggerIDs(Player sender) {
		// reserved page indices: 5
		final int reservedPageStartIndex = 5;
		final int reservedPages = 1;
		
		final int bookID = 2;
		
		final TriggerCollection tc = plugin.getConfig().getTriggerCollection();
		final ArrayList<Trigger> list = tc.getAllTriggers();
		
		final ArrayList<Trigger> toDisplay = new ArrayList<Trigger>();
		
		if (!pageIndex.containsKey(sender) || (currentBookID.containsKey(sender) && currentBookID.get(sender) != bookID)) {
			pageIndex.put(sender, 0);
		}
		
		currentBookID.put(sender, bookID);
		
		if (pageIndex.get(sender) == 0) {
			for (Trigger t : list) {
				if (t.getOwner().getName().equals(sender.getName()) || hasPerm(sender, ".admin.trigger")) {
					toDisplay.add(t);
				}
			}
			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			Page p = new Page();
			
			p.addLine(chatPrefix + "Trigger IDs (" + 1 + "/" + totalPages + ")");
			p.addLine("------------------------");
			
			String s = "";
			
			for (int j = 0; j < toDisplay.size(); j++) {
				s += "" + ChatColor.GREEN + toDisplay.get(j).getID() + ChatColor.WHITE + ", ";
			}
			
			s = s.substring(0, s.length() - 2);
			p.addLine(s);
			
			book.addPage(p);
			
			currentBook.put(sender, book);
		}
		
		final int currentPage = pageIndex.get(sender) >= reservedPageStartIndex ? pageIndex.get(sender) - reservedPageStartIndex : 0;
		
		if (currentPage < currentBook.get(sender).size()) {
			currentBook.get(sender).getPage(currentPage).displayPage(sender, "");
			
			if (reservedPageStartIndex + currentPage + 1 < reservedPageStartIndex + reservedPages) {
				pageIndex.put(sender, reservedPageStartIndex + currentPage + 1);
			} else {
				pageIndex.put(sender, 0);
			}
		} else {
			pageIndex.put(sender, 0);
		}
	}
	
	private void showHelp(Player sender) {
		// reserved page indices: 6-9
		final int reservedPageStartIndex = 6;
		final int reservedPages = 4;
		
		final int bookID = 3;
		
		if (!pageIndex.containsKey(sender) || (currentBookID.containsKey(sender) && currentBookID.get(sender) != bookID)) {
			pageIndex.put(sender, 0);
		}
		
		currentBookID.put(sender, bookID);
		
		if (pageIndex.get(sender) == 0) {			
			final int totalPages = reservedPages;
			
			final Book book = new Book();
			
			// first page
			Page p = new Page();
			
			p.addLine(chatPrefix + "Command help (" + 1 + "/" + totalPages + ")");
			p.addLine("------------------------");
			p.addLine("All commands are case-insensitive.");
			p.addLine("Aliases: /mobtrigger /mt");
			p.addLine(ChatColor.GREEN + "/mt select" + ChatColor.WHITE + " - Switches to selection mode");
			p.addLine(ChatColor.GREEN + "/mt mobIDs" + ChatColor.WHITE + " - Shows a list of all mob IDs used by the plug-in");
			p.addLine(ChatColor.GOLD + "Enter the command again to display the next page.");
			
			book.addPage(p);
			
			// second page
			p = new Page();
			
			p.addLine(chatPrefix + "Command help (" + 2 + "/" + totalPages + ")");
			p.addLine("------------------------");
			p.addLine(ChatColor.GREEN + "/mt cuboid save" + ChatColor.WHITE + " - Saves a selected cuboid under the");
			p.addLine("    specified name");
			p.addLine(ChatColor.GREEN + "/mt cuboid info" + ChatColor.WHITE + " - Shows information about the specified cuboid");
			p.addLine(ChatColor.GREEN + "/mt trigger create" + ChatColor.WHITE + " - Creates a trigger, enter the command");
			p.addLine("    for more information");
			p.addLine(ChatColor.GOLD + "Enter the command again to display the next page.");
			
			book.addPage(p);
			
			// third page
			p = new Page();
			
			p.addLine(chatPrefix + "Command help (" + 3 + "/" + totalPages + ")");
			p.addLine("------------------------");
			p.addLine(ChatColor.GREEN + "/mt trigger info" + ChatColor.WHITE + " - Shows information about the specified");
			p.addLine("    trigger");
			p.addLine(ChatColor.GREEN + "/mt trigger link" + ChatColor.WHITE + " - Links a trigger block to a trigger");
			p.addLine(ChatColor.GREEN + "/mt trigger unlink" + ChatColor.WHITE + " - Removes a link from a trigger block");
			p.addLine(ChatColor.GOLD + "Enter the command again to display the next page.");
			
			book.addPage(p);
			
			// fourth page
			p = new Page();
			
			p.addLine(chatPrefix + "Command help (" + 4 + "/" + totalPages + ")");
			p.addLine("------------------------");
			p.addLine(ChatColor.GREEN + "/mt trigger reset" + ChatColor.WHITE + " - Reset a trigger, also kills all mobs");
			p.addLine("    spawned by the trigger");
			p.addLine(ChatColor.GREEN + "/mt trigger showIDs" + ChatColor.WHITE + " - Shows a list of all triggers you own");
			p.addLine(ChatColor.GREEN + "/mt trigger set" + ChatColor.WHITE + " - Sets the amount of mobs spawned by the");
			p.addLine("    trigger");
			
			book.addPage(p);
				
			currentBook.put(sender, book);
		}
		
		final int currentPage = pageIndex.get(sender) >= reservedPageStartIndex ? pageIndex.get(sender) - reservedPageStartIndex : 0;
		
		if (currentPage < currentBook.get(sender).size()) {
			currentBook.get(sender).getPage(currentPage).displayPage(sender, "");
			
			if (reservedPageStartIndex + currentPage + 1 < reservedPageStartIndex + reservedPages) {
				pageIndex.put(sender, reservedPageStartIndex + currentPage + 1);
			} else {
				pageIndex.put(sender, 0);
			}
		} else {
			pageIndex.put(sender, 0);
		}
	}
}
