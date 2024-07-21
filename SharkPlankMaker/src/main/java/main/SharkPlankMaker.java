package main;

import helpers.*;
import helpers.annotations.AllowedValue;
import helpers.annotations.ScriptConfiguration;
import helpers.annotations.ScriptManifest;
import helpers.utils.OptionType;
import helpers.utils.RegionBox;
import helpers.utils.Tile;
import helpers.utils.Area;
import helpers.utils.ItemList;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import utils.*;
import java.util.Random;

import static helpers.utils.BankNames.*;

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
    String coins = "1004";
    String unprocessedItemID;
    String processedItemID;
    Map<String, String[]> itemIDs;
    RegionBox woodcuttingGuildRegion = new RegionBox("WoodcuttingGuild", 1695, 2427, 1938, 2607);
    Tile sawmillTile = new Tile(630, 820);
    public static Area sawmillArea = new Area(new Tile(587, 852), new Tile(630, 820));

    // Random number generator
    Random random = new Random();

    List<Color> operatorColors = Arrays.asList(
        Color.decode("#d6a074"),
        Color.decode("#3a3309"), 
        Color.decode("#8db2b4"),
        Color.decode("#57573a"),
        Color.decode("#bad5d7"),
        Color.decode("#857f7a"),
        Color.decode("#6f6c68")
    );
    
    Rectangle searchArea = new Rectangle(384, 242, 24, 48);
    RegionBox sawmillMan = new RegionBox("REGIONBOXNAME", 379, 234, 417, 295);
    Tile bankTile = new Tile(587, 852);
    
    Tile[] pathToSawmill = new Tile[] {
        new Tile(587, 852),
        new Tile(588, 833),
        new Tile(599, 823),
        new Tile(608, 823),
        new Tile(622, 819),
        new Tile(630, 820)
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

        }
    
    
    private void initialSetup() {
        // Open bank 
        while(!Player.atTile(bankTile)) {
            Walker.step(bankTile);
        }
        Bank.open(WOODCUTTING_GUILD); 
        
        // Enter pin if needed
        if (Bank.isBankPinNeeded()) {
            Bank.enterBankPin();
        }

        //empty inventory just in case
        Bank.tapDepositInventoryButton();
        
        // Set correct bank tab
        if (!Bank.isSelectedBankTab(banktab)) {
            Bank.openTab(banktab); 
        }
        
        if (!Bank.isSelectedQuantityAllButton()) {
            Bank.tapQuantityAllButton();
        }
        Condition.wait(() -> Bank.isSelectedQuantityAllButton(), 200, 12);

        Bank.withdrawItem("1004", 0.8);
        Condition.wait(() -> Inventory.contains(coins, 0.8), 200, 12);
        // Set custom withdraw quantity to 27
        //Bank.tapQuantityCustomButton();
        //Client.sendKeystroke("27");
        
        // Withdraw logs and coins  
        Bank.withdrawItem(unprocessedItemID, 0.8);
        Condition.wait(() -> Inventory.contains(unprocessedItemID, 0.8), 200, 12);
        
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
        Logger.log("Walking to sawmill...");
        Walker.walkPath(pathToSawmill);
        //Condition.sleep(generateDelay(100,300));
        Walker.step(sawmillTile);
        Condition.wait(() -> Player.atTile(sawmillTile), 250, 20);   
    }
  
    private void processLogs() {
        // Find sawmill operator 
        List<Point> operator = Client.getPointsFromColorsInRect(operatorColors, searchArea, 4);
        
        if (!operator.isEmpty()) {
            // Open sawmill interface
            Logger.log("Found sawmill operator. Converting logs to planks...");
            //Client.tap(operator.get(0));
            Point operatorPoint = operator.get(0);
            Client.longPress(operatorPoint.x, operatorPoint.y);
            Condition.sleep(generateDelay(500,1000));
            Client.tap(380, 333);
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
        // Reverse the pathToSawmill array
        Tile[] pathToBank = new Tile[pathToSawmill.length];
        for (int i = 0; i < pathToSawmill.length; i++) {
            pathToBank[i] = pathToSawmill[pathToSawmill.length - 1 - i];
        }
    
        // Walk back to bank
        Logger.log("Finished processing. Returning to bank...");
        Walker.walkPath(pathToBank);
        //Condition.sleep(generateDelay(100, 300));
        Walker.step(bankTile);
        Condition.wait(() -> Player.atTile(bankTile), 250, 20);
    }
    
    private void bank() {
        // Open bank 
        Bank.open(WOODCUTTING_GUILD);
        
        // Deposit planks
        if (!Bank.isSelectedQuantityAllButton()) {
            Bank.tapQuantityAllButton();
        }
        Inventory.tapItem(processedItemID, 0.80);
        //Condition.wait(() -> !Inventory.contains(processedItemID, 0.9), 200, 100);

        // Withdraw more logs and coins
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

    private int generateDelay(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return random.nextInt(max - min + 1) + min;
    }
}