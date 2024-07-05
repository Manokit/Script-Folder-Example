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
        handleBanking();
        return false;
    }

    private void handleBanking() {
        ensureBankIsOpenAndReady();
        if (Bank.isOpen()) {
            Logger.log("Banking items!");
            bankItems();
        }
    }

    private void ensureBankIsOpenAndReady() {
        stepToBank();
        if (!Bank.isOpen()) {
            Logger.debugLog("Opening bank");
            Bank.open(dynamicBank);
            Condition.wait(Bank::isOpen, 50, 20);
        }
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
        //Condition.wait(Inventory::isEmpty, 100, 10);
        Condition.sleep(1000);        

        // Withdraw coins
        if (!Inventory.contains(ItemList.COINS_995, 0.80)) {
            Bank.tapSearchButton();
            Condition.sleep(1000);        
            //Condition.wait(() -> Bank.isSearchOpen(), 100, 10);
            Client.sendKeystroke("Coins");
            Bank.withdrawItem(ItemList.COINS_995, 0.80);
            Condition.wait(() -> Inventory.contains(ItemList.COINS_995, 0.80), 100, 10);
            Bank.tapSearchButton(); // Close search
        }

        // Withdraw logs
        Bank.tapSearchButton();
        Condition.sleep(1000);        

       // Condition.wait(() -> Bank.isSearchOpen(), 100, 10);
        Client.sendKeystroke(logType);
        Bank.withdrawItem(logItemId, 0.80);
        Condition.wait(() -> Inventory.contains(logItemId, 0.80), 100, 10);
        Bank.tapSearchButton(); // Close search

        Bank.close();
        Condition.wait(() -> !Bank.isOpen(), 100, 10);
    }
}