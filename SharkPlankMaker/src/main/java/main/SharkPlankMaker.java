package main;

import helpers.*;
import helpers.annotations.AllowedValue;
import helpers.annotations.ScriptConfiguration;
import helpers.annotations.ScriptManifest;
import helpers.utils.OptionType;
import helpers.utils.RegionBox;
import helpers.utils.Tile;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static helpers.Interfaces.*;

@ScriptManifest(
        name = "Shark Plank Maker",
        description = "Turns logs into planks at the Woodcutting Guild sawmill.",
        guideLink = "https://tidale.us/",
        version = "0.1",
        categories = {ScriptCategory.Construction}
)
@ScriptConfiguration.List(
        {
                @ScriptConfiguration(
                        name = "Log Type",
                        description = "Which type of logs would you like to process?",
                        defaultValue = "Logs",
                        allowedValues = {
                                @AllowedValue(optionIcon = "1511", optionName = "Logs"),
                                @AllowedValue(optionIcon = "1521", optionName = "Oak logs"),
                                @AllowedValue(optionIcon = "6333", optionName = "Teak logs"),
                                @AllowedValue(optionIcon = "6332", optionName = "Mahogany logs")
                        },
                        optionType = OptionType.STRING
                ),
                @ScriptConfiguration(
                        name = "Bank Tab",
                        description = "What bank tab are your resources located in?",
                        defaultValue = "0",
                        optionType = OptionType.BANKTABS
                ),
                @ScriptConfiguration(
                        name = "Use world hopper?", 
                        description = "Would you like to hop worlds based on your hop profile settings?",
                        defaultValue = "1",
                        optionType = OptionType.WORLDHOPPER
                )
        }
)

public class SharkPlankMaker extends AbstractScript {
    String hopProfile;
    Boolean hopEnabled;
    String logType;  
    String bankloc;
    int banktab;
    String coins = "995";
    String unprocessedItemID;
    String processedItemID;
    Map<String, String[]> itemIDs;
    RegionBox woodcuttingGuildRegion = new RegionBox("WoodcuttingGuild", 1695, 2427, 1938, 2607);


    List<Color> operatorColors = Arrays.asList(
        Color.decode("#7f7b76"),
        Color.decode("#deac82"), 
        Color.decode("#43301f")
    );
    
    Rectangle searchArea = new Rectangle(50, 50, 400, 300);
    
    Tile[] pathToSawmill = {
        new Tile(587, 852),
        new Tile(585, 846),
        new Tile(589, 845),
        new Tile(598, 823),
        new Tile(610, 822),
        new Tile(615, 818), 
        new Tile(630, 818),
        new Tile(630, 819)
    };

    @Override
    public void onStart() {
        // Get configurations
        setupConfigs();
        
        // Initialize item IDs
        initializeItemIDs();
        
        // One-time setup
        Logger.log("Setting up Shark Plank Maker...");
        setupBanking(); 
        initialSetup();
        
        Logger.log("Setup complete. Starting to make planks.");
    }
    
    @Override
    public void poll() {
        // Main script loop
        checkInventory();
        walkToSawmill();
        processLogs(); 
        walkToBank();
        bank();
        hopActions();
    }
    
    private void setupConfigs() {
        // Get configurations from UI 
        Map<String, String> configs = getConfigurations();
        logType = configs.get("Log Type");
        banktab = Integer.parseInt(configs.get("Bank Tab"));
        hopProfile = configs.get("Use world hopper?");  
        hopEnabled = Boolean.valueOf(configs.get("Use world hopper?.enabled"));
    }
    
    private void initializeItemIDs() {
        // Map log types to item IDs
        itemIDs = Map.of(
            "Logs", new String[]{"1511", "960"},
            "Oak logs", new String[]{"1521", "8778"},
            "Teak logs", new String[]{"6333", "8780"}, 
            "Mahogany logs", new String[]{"6332", "8782"}
        );
        
        String[] ids = itemIDs.get(logType);
        unprocessedItemID = ids[0];
        processedItemID = ids[1];
    }
    
    private void setupBanking() {
        // Find nearest bank and set as banking location
        bankloc = Bank.setupDynamicBank();
        
        if (bankloc == null) {
            Logger.log("No bank found nearby. Stopping script.");
            Script.stop();
        }
    }
    
    private void initialSetup() {
        // Open bank 
        Bank.open(bankloc);
        
        // Enter pin if needed
        if (Bank.isBankPinNeeded()) {
            Bank.enterBankPin();
        }
        
        // Set correct bank tab
        if (!Bank.isSelectedBankTab(banktab)) {
            Bank.openTab(banktab); 
        }
        
        // Set custom withdraw quantity to 27
        Bank.tapQuantityCustomButton();
        Client.sendKeystroke("27");
        
        // Withdraw logs and coins  
        Bank.withdrawItem("[995-1004,617]", 0.8);
        Bank.withdrawItem(unprocessedItemID, 0.8);
        
        // Close bank
        Bank.close();
    }
    
    private void checkInventory() {
        // Ensure we have logs and coins to continue
        if (!Inventory.contains(unprocessedItemID, 0.8) || !Inventory.contains(coins, 0.8)) {
            Logger.log("Missing logs or coins. Getting more.");
            bank();
        }
    }
    
    private void walkToSawmill() {
        // Walk to sawmill if not already there
        if (!Client.isColorAtPoint(operatorColors.get(0), searchArea.getLocation(), 10)) {
            Logger.log("Walking to sawmill...");
            Walker.walkPath(woodcuttingGuildRegion, pathToSawmill);
        }
    }
  
    private void processLogs() {
        // Find sawmill operator 
        List<Point> operator = Client.getPointsFromColorsInRect(operatorColors, searchArea, 10);
        
        if (!operator.isEmpty()) {
            // Open sawmill interface
            Logger.log("Found sawmill operator. Converting logs to planks...");
            Client.tap(operator.get(0));
            Condition.wait(() -> Chatbox.isMakeMenuVisible(), 200, 20);
            
            // Select log type
            switch (logType) {
                case "Logs":
                    Chatbox.makeOption(1);
                    break;
                case "Oak logs":
                    Chatbox.makeOption(2);
                    break;
                case "Teak logs":
                    Chatbox.makeOption(3);
                    break;
                case "Mahogany logs":
                    Chatbox.makeOption(4);
                    break;
            }
            
            // Wait for processing to finish
            Condition.wait(() -> !Inventory.contains(unprocessedItemID, 0.9), 200, 100);
        }
    }
    
private void walkToBank() {    
    // Define the bank tile coordinates
    Tile bankTile = new Tile(587, 852);
    
    // Walk back to bank
    Logger.log("Finished processing. Returning to bank...");
    Walker.walkTo(bankTile, woodcuttingGuildRegion);
}
    
    private void bank() {
        // Open bank 
        Bank.open(bankloc);
        
        // Deposit planks
        Bank.tapDepositInventoryButton();
        
        // Withdraw more logs and coins
        Bank.withdrawItem("[995-1004,617]", 0.9);
        Bank.withdrawItem(unprocessedItemID, 0.9); 
        
        // Close bank  
        Bank.close();
    }
    
    private void hopActions() {
        // Hop worlds if enabled
        if (hopEnabled) {
            Game.hop(hopProfile, false, false);
        }
    }
}