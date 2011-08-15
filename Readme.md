MobTrigger plugin v1.00		
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
/mt trigger info - Shows information about the currently selected trigger		
/mt trigger link (int triggerID) - Links a trigger block to a trigger with the specified ID		
/mt trigger unlink - Removes the link from the selected trigger block		
/mt trigger reset (int triggerID) - Reset the trigger with the specified ID, also kills all mobs		
/mt trigger showIDs - Shows a list of all triggers you own		
/mt trigger set (int mobID|String mobName) (int amount) - Sets the amount of mobs spawned by the trigger

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
        description: Gives access to all trigger related commands (no admin)
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
        description: Gives access to all trigger cuboid related commands (no admin)
    mobtrigger.trigger.cuboid.save:
        description: Allows you to save cuboids
        default: op
    mobtrigger.trigger.cuboid.info:
        description: Allows you to get info about cuboids
        default: op