package tasks;

import helpers.utils.ItemList;
import utils.Task;
import static main.PlankMaker.*;
import static helpers.Interfaces.*;

public class CheckForItems extends Task {
    public static boolean checkedForItems = false;
    private boolean checkedForCoins = false;
    private boolean checkedForLogs = false;

    @Override
    public boolean activate() {
        return !checkedForItems;
    }

    @Override
    public boolean execute() {
        Logger.log("Checking your inventory for required items");
        if (!GameTabs.isInventoryTabOpen()) {
            GameTabs.openInventoryTab();
            Condition.wait(() -> GameTabs.isInventoryTabOpen(), 50, 10);
        }

        boolean hasCoins = Inventory.contains(ItemList.COINS_995, 0.80);
        checkedForCoins = true;

        boolean hasLogs = Inventory.contains(logItemId, 0.80);
        checkedForLogs = true;

        if (hasCoins && hasLogs) {
            checkedForItems = true;
            return true;
        } else {
            Logger.log("Coins or Logs not found, stopping script");
            Script.stop();
            return false;
        }
    }
}