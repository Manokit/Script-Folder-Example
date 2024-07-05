package tasks;

import helpers.utils.Tile;
import utils.Task;
import static main.PlankMaker.*;
import static helpers.Interfaces.*;

public class WalkToSawmillTask extends Task {
    @Override
    public boolean activate() {
        Tile playerPosition = Walker.getPlayerPosition();
        return Inventory.contains(logItemId, 0.80) && !Player.isTileWithinArea(playerPosition, AT_SAWMILL_AREA);
    }

    @Override
    public boolean execute() {
        Logger.log("Walking to sawmill");
        Walker.walkPath(PATH_TO_SAWMILL);
        return false;
    }
}