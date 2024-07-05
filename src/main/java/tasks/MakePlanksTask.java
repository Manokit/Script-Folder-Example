package tasks;

import helpers.utils.Tile;
import utils.Task;
import static main.PlankMaker.*;

import java.awt.Point;
import java.util.List;

import static helpers.Interfaces.*;



public class MakePlanksTask extends Task {
    @Override
    public boolean activate() {
        Tile playerPosition = Walker.getPlayerPosition();
        return Inventory.contains(logItemId, 0.80) && Player.isTileWithinArea(playerPosition, AT_SAWMILL_AREA);
    }

    @Override
    public boolean execute() {
        List<Point> foundPoints = Client.getPointsFromColorsInRect(SAWMILL_OPERATOR_COLORS, SAWMILL_OPERATOR_ROI, 1);

        if (!foundPoints.isEmpty()) {
            Point tapPoint = foundPoints.get(RANDOM.nextInt(foundPoints.size()));
            Client.tap(tapPoint);
            if (Chatbox.isMakeMenuVisible()) {
                Chatbox.makeOption(plankMakeOption);
                Logger.log("Made " + logType + " into planks at sawmill.");
                Condition.wait(() -> !Inventory.contains(logItemId, 0.80), 100, 50);
            }
        } else {
            Logger.log("Couldn't find the sawmill operator. Moving to the next tile.");
            Walker.step(SAWMILL_OPERATOR_TILE);
        }
        return false;
    }
}