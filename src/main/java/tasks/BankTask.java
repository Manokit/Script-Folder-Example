package tasks;

import helpers.*;
import helpers.utils.*;
import utils.Task;
import static main.PlankMaker.*;
import helpers.*;
import helpers.annotations.AllowedValue;
import helpers.annotations.ScriptConfiguration;
import helpers.annotations.ScriptManifest;
import helpers.utils.ItemList;
import helpers.utils.OptionType;
import tasks.*;
import helpers.AbstractScript;
import helpers.ScriptCategory;

import helpers.utils.*;
import utils.Task;

import static main.PlankMaker.*;
import static helpers.Interfaces.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BankTask extends Task {
    private String dynamicBank = null;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public boolean activate() {
        return Inventory.isFull() || (!Inventory.contains(logItemId, 0.80) || !Inventory.contains(ItemList.COINS_995, 0.80));
    }

    @Override
    public boolean execute() {
        if (!Player.isTileWithinArea(Walker.getPlayerPosition(), AT_BANK_AREA)) {
            Logger.log("Not at the bank, walking there");
            Walker.walkPath(PATH_TO_BANK);
            return false;
        }
        return handleBanking();
    }

    private boolean handleBanking() {
        if (attemptCount >= MAX_ATTEMPTS) {
            Logger.log("Failed to open bank after " + MAX_ATTEMPTS + " attempts. Stopping script.");
            Script.stop();
            return false;
        }

        if (!Bank.isOpen()) {
            ensureBankIsOpenAndReady();
            attemptCount++;
            return false;
        }

        Logger.log("Banking items!");
        bankItems();
        attemptCount = 0;
        return true;
    }

    private void ensureBankIsOpenAndReady() {
        stepToBank();
        Logger.debugLog("Opening bank");
        Bank.open(dynamicBank);
        Condition.wait(Bank::isOpen, 100, 20);
    }

    private void stepToBank() {
        if (dynamicBank == null) {
            Logger.log("Finding bank");
            dynamicBank = Bank.setupDynamicBank();
            Logger.debugLog("Reached bank tile.");
        } else {
            Logger.log("Stepping to bank");
            Bank.stepToBank(dynamicBank);
            Logger.debugLog("Reached bank tile");
        }
    }

    private void bankItems() {
        // Deposit all items
        Bank.tapDepositInventoryButton();
        Condition.wait(() -> !Inventory.isFull(), 100, 10);
        
        // Withdraw coins
        if (!Inventory.contains(ItemList.COINS_995, 0.80)) {
            withdrawItem("Coins", ItemList.COINS_995);
        }

        // Withdraw logs
        withdrawItem(logType, logItemId);

        Bank.close();
        Condition.wait(() -> !Bank.isOpen(), 100, 10);
    }

    private void withdrawItem(String itemName, int itemId) {
        Bank.tapSearchButton();
        //Condition.wait(Bank::isSearchOpen, 100, 10);
        Client.sendKeystroke(itemName);
        Bank.withdrawItem(itemId, 0.80);
        Condition.wait(() -> Inventory.contains(itemId, 0.80), 100, 10);
        Bank.tapSearchButton(); // Close search
    }
}