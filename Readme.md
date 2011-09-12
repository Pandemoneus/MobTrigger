MobTrigger plugin v1.02		
by Pandemoneus		
https://github.com/Pandemoneus

Requirements:
----------------
- Permissions 3.x (optional)

How to install:
----------------
1. Copy 'MobTrigger.jar' into your 'plugins/' folder.		
2. Start your server to create a config file.		
3. Edit the config file in 'plugins/MobTrigger/config.yml'.

How to uninstall:
-----------------
1. Delete 'MobTrigger.jar'.		
2. Delete the folder 'plugins/MobTrigger'.

Editable options:
-----------------
ForceBukkitPermissions: [true/false] - got Permissions installed but still want to use the built-in Bukkit Permission system? then set to true (default: false)			
Trigger.Region.SelectionItemId: [itemID] - simply determines which item is used to select cuboids for the triggers (default: 69 / Lever)	

Commands:
-----------------
All commands are case-insensitive.		
Aliases: /mobtrigger /mt		
/mt select - Toogles selection mode		
/mt mobIDs - Shows a list of all mob IDs used by the plug-in		
/mt cuboid save (String cuboidName) - Saves a selected cuboid under the specified name			
/mt cuboid info (String cuboidName) - Shows information about the specified cuboid		
/mt trigger create (int triggerID) (String cuboidName) (double firstDelay) (boolean isSelfTrigging) (double selfTriggerDelay) (int totalTimes) - Creates a trigger. Parameters:		
*   triggerID - the ID of the trigger, using an already existing ID will overwrite that trigger		
*   cuboidName - the name of the cuboid the mobs will spawn in (see /mt cuboid save)		
*   firstDelay - the delay in seconds after which the trigger will fire the first time		
*   isSelfTriggering - determines whether the trigger executes itself again after it was first fired		
*   selfTriggerDelay - the delay in seconds after which it will execute itself again		
*   totalTimes - the total amount of times the trigger can be executed before it needs to be reset
*   resetTime - the time in seconds after which the trigger resets itself after the very last possible execution
/mt trigger info - Shows information about the currently selected trigger		
/mt trigger link (int triggerID) - Links a trigger block to a trigger with the specified ID		
/mt trigger unlink - Removes the link from the selected trigger block		
/mt trigger reset (int triggerID) - Reset the trigger with the specified ID, also kills all mobs		
/mt trigger showIDs - Shows a list of all triggers you own		
/mt trigger set (int mobID|String mobName) (int amount) - Sets the amount of mobs spawned by the trigger

Step by step guide on how to create your first trigger:
-------------------------------------------------------
1. Find a suitable place where you want mobs to spawn. Make sure your cuboid area does not have any blocks inside (or mobs might spawn inside them and suffocate).
2. Type /mt select   (requires 'mobtrigger.trigger.select' permission)
3. With a lever in your hand, mark the corners of the cuboid, by clicking once on the first, then on the second.
4. Save your cuboid! Tpye /mt cuboid save MyFirstCuboid   (requires 'mobtrigger.trigger.cuboid.save' permission)
5. Now search for the trigger block you want to use (either a lever, button, wooden/stone pressure plate).
6. Click that block while NOT having a lever in your hand and still being in selection mode.
7. You got everything that we need now! Now type /mt trigger create 0 MyFirstCuboid 0 false 0 1 300   (requires 'mobtrigger.trigger.create' permission)
8. Now that we have created the trigger, we need to add mobs to it. Type /mt trigger set pig 3 to make 3 pigs spawn when we trigger it.  (requires 'mobtrigger.trigger.create' permission)
9. And we are good to go! Type /mt select to exit the selection mode.
10. Press the button/lever/pressure plate and see whether it works!   (requires 'mobtrigger.trigger.use' permission)

Permission nodes:
-----------------
    mobtrigger.*:
        description: Gives access to everything that the plug-in offers
    mobtrigger.admin.*:
        description: Enables you to modify everything that does not belong to you
        default: op
    mobtrigger.admin.trigger:
        description: Allows you to modify triggers that do not belong to you
        default: op
    mobtrigger.admin.cuboid:
        description: Allows you to modify cuboids that do not belong to you
        default: op
    mobtrigger.trigger.*:
        description: Gives access to all trigger related commands (excludes admin)
    mobtrigger.trigger.use:
        description: Allows you to use triggers
        default: true
    mobtrigger.trigger.select:
        description: Allows you to select triggers and trigger areas
        default: op
    mobtrigger.trigger.create:
        description: Allows you to create, link and reset triggers
        default: op
    mobtrigger.trigger.destroy:
        description: Allows you to remove and unlink triggers
        default: op
    mottrigger.trigger.mobids:
        description: Allows you to view the mob IDs the plug-in uses
        default: op
    mobtrigger.trigger.cuboid.*:
        description: Gives access to all trigger cuboid related commands (excludes admin)
    mobtrigger.trigger.cuboid.save:
        description: Allows you to save cuboids
        default: op
    mobtrigger.trigger.cuboid.info:
        description: Allows you to get info about cuboids
        default: op