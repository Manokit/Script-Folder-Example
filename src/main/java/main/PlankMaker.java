package main;

import helpers.annotations.AllowedValue;
import helpers.annotations.ScriptConfiguration;
import helpers.annotations.ScriptManifest;
import tasks.*;
import helpers.AbstractScript;
import helpers.ScriptCategory;

import helpers.utils.*;
import utils.Task;

import static helpers.Interfaces.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@ScriptManifest(
        name = "Public Plank Maker",
        description = "Makes planks at the sawmill in the Woodcutting Guild",
        guideLink = "https://tidale.us/",
        version = "1.0",
        categories = {ScriptCategory.Construction}
)
@ScriptConfiguration.List(
{
    @ScriptConfiguration(
        name = "Log Type",
        description = "Select the type of logs to process",
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
        name = "Use world hopper?",
        description = "Would you like to hop worlds based on your hop profile settings?",
        defaultValue = "1",
        optionType = OptionType.WORLDHOPPER
    )
})

public class PlankMaker extends AbstractScript {

    private List<Task> tasks = Arrays.asList(
        new CheckForItems(),
        new BankTask(),
        new WalkToSawmillTask(),
        new MakePlanksTask(),
        new WalkToBankTask()
    );

    public static String logType;
    public static int logItemId;
    public static int plankMakeOption;
    public static String hopProfile;
    public static Boolean hopEnabled;
    public static Boolean useWDH;

    // Constants
    public static final Random RANDOM = new Random();

    public static final Tile[] PATH_TO_SAWMILL = new Tile[] {
        new Tile(587, 852), new Tile(585, 846), new Tile(589, 845),
        new Tile(598, 823), new Tile(610, 822), new Tile(615, 818),
        new Tile(630, 818), new Tile(630, 819)
    };
    public static final Tile[] PATH_TO_BANK = reversePath(PATH_TO_SAWMILL);
    public static final Area AT_SAWMILL_AREA = new Area(new Tile(628, 817), new Tile(632, 821));
    public static final Area AT_BANK_AREA = new Area(new Tile(585, 850), new Tile(589, 854));
    public static final List<Color> SAWMILL_OPERATOR_COLORS = Arrays.asList(
        Color.decode("#7f7b76"), Color.decode("#deac82"), Color.decode("#43301f")
    );
    public static final Rectangle SAWMILL_OPERATOR_ROI = new Rectangle(61, 41, 600, 485);
    public static final Tile SAWMILL_OPERATOR_TILE = new Tile(630, 819);
    public static final int SAWMILL_INTERFACE_ID = 403; // Verify this
    //public static final RegionBox WOODCUTTING_GUILD_REGION = new RegionBox("WoodcuttingGuild", 0, 0, 0, 0); // Replace with actual coordinates

    @Override
    public void onStart() {
        Map<String, String> configs = getConfigurations();
        logType = configs.get("Log Type");
        hopProfile = configs.get("Use world hopper?");
        hopEnabled = Boolean.valueOf(configs.get("Use world hopper?.enabled"));
        useWDH = Boolean.valueOf(configs.get("Use world hopper?.useWDH"));

        switch (logType) {
            case "Logs":
                logItemId = ItemList.LOGS_1511;
                plankMakeOption = 1;
                break;
            case "Oak logs":
                logItemId = ItemList.OAK_LOGS_1521;
                plankMakeOption = 2;
                break;
            case "Teak logs":
                logItemId = ItemList.TEAK_LOGS_6333;
                plankMakeOption = 3;
                break;
            case "Mahogany logs":
                logItemId = ItemList.MAHOGANY_LOGS_6332;
                plankMakeOption = 4;
                break;
        }

        Logger.log("PlankMaker script started. Processing " + logType);
    }

    @Override
    public void poll() {
        for (Task task : tasks) {
            if (task.activate()) {
                task.execute();
                return;
            }
        }
    }

        //if (hopEnabled && Game.shouldHop()) {
        //    Game.hop(hopProfile, false, useWDH);
        //    return;
        //}



    

/*     @Override
    public void onStop() {
        Logger.log("PlankMaker script stopped.");
    } */

    private static Tile[] reversePath(Tile[] path) {
        Tile[] reversed = new Tile[path.length];
        for (int i = 0; i < path.length; i++) {
            reversed[i] = path[path.length - 1 - i];
        }
        return reversed;
    }
}