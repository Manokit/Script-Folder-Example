package tasks;

import helpers.*;
import helpers.utils.*;
import utils.Task;
import static main.PlankMaker.*;
import helpers.annotations.AllowedValue;
import helpers.annotations.ScriptConfiguration;
import helpers.annotations.ScriptManifest;
import helpers.utils.ItemList;
import helpers.utils.OptionType;
import tasks.*;
import helpers.AbstractScript;
import helpers.ScriptCategory;

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
            Condition.wait(Bank::isOpen, 50, 10);
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
        if (!Bank.isSelectedQuantityAllButton()) {
            Client.tap(Bank.findQuantityAllButton());
            Condition.wait(Bank::isSelectedQuantityAllButton, 100, 20);
        }
        
        // Deposit all items
        Bank.tapDepositInventoryButton();
        //Condition.wait(Inventory::isEmpty, 100, 10);
        
        // Withdraw coins if not in inventory
        if (!Inventory.contains(ItemList.COINS_995, 0.80)) {
            Bank.withdrawItem(ItemList.COINS_995, 0.80);
            Condition.wait(() -> Inventory.contains(ItemList.COINS_995, 0.80), 100, 10);
        }

        // Withdraw logs
        Bank.withdrawItem(logItemId, 0.80);
        Condition.wait(() -> Inventory.contains(logItemId, 0.80), 100, 10);

        Bank.close();
    }
}