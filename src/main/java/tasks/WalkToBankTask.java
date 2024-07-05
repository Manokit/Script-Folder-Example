package tasks;


import helpers.utils.*;
import utils.Task;

import static main.PlankMaker.*;
import static helpers.Interfaces.*;


public class WalkToBankTask extends Task {
    @Override
    public boolean activate() {
        Tile playerPosition = Walker.getPlayerPosition(WOODCUTTING_GUILD_REGION);
        return !Inventory.contains(logItemId, 0.80) && !Player.isTileWithinArea(playerPosition, AT_BANK_AREA);
    }

    @Override
    public boolean execute() {
        Logger.log("Walking to bank");
        Walker.walkPath(WOODCUTTING_GUILD_REGION, PATH_TO_BANK);
        return false;
    }
}