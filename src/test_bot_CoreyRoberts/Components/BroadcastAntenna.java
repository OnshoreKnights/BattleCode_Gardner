package test_bot_CoreyRoberts.Components;

import battlecode.common.*;
import java.util.List;

public class BroadcastAntenna {
    private RobotController robotController;

    //TODO better method of assigning and tracking channels
    //this will do for now.
    //TODO each bot will have five channels
    //1 = ID.  Archon can count non-zero IDs within the given range for that robot type
    //2 = status.  Settled/farmer/factory.  Guard/attack. Etc.
    //3 = x coordinate
    //4 = y coordinate
    //broadCast antenna can accept an id to check the status or if under attack
    //or check for all archons/gardeners under attack and get their ID
    //add range for "protected" robots that are under attack. Gardeners and archons. Record type, x, and y coordinates
    public static int enemyArchonLocation1x = 0;
    public static int enemyArchonLocation1y = 1;
    public static int enemyArchonLocation2x = 2;
    public static int enemyArchonLocation2y = 3;
    public static int enemyArchonLocation3x = 4;
    public static int enemyArchonLocation3y = 5;
    public static int unsettledGardeners = 6;
    public static int settledGardeners = 7;
    public static int soldierCount = 8;
    public static int soldiersToHire = 9;
    public static int scoutCount = 10;
    public static int scoutsToHire = 11;

    public BroadcastAntenna(RobotController _robotController) {
        robotController = _robotController;
    }

    public void setSoldierBuildCount(int maxSoldiers) throws GameActionException {
        int soldierCount = robotController.readBroadcastInt(BroadcastAntenna.soldierCount);
        if(soldierCount < maxSoldiers) {
            robotController.broadcastInt(BroadcastAntenna.soldiersToHire, (maxSoldiers - soldierCount));
        }
    }

    public void setScoutBuildCount(int maxScouts) throws GameActionException {
        int scoutCount = robotController.readBroadcastInt(BroadcastAntenna.scoutCount);
        if(scoutCount < maxScouts) {
            robotController.broadcastInt(scoutsToHire, (maxScouts - scoutCount));
        }
    }

    public void setArchonLocations(List<MapLocation> locations) throws GameActionException {
        if(locations.size() >=1) {
            robotController.broadcastFloat(enemyArchonLocation1x, locations.get(0).x);
            robotController.broadcastFloat(enemyArchonLocation1y, locations.get(0).y);
        }
        if(locations.size() >=2) {
            robotController.broadcastFloat(enemyArchonLocation2x, locations.get(1).x);
            robotController.broadcastFloat(enemyArchonLocation2y, locations.get(1).y);
        }
        if(locations.size() >=3) {
            robotController.broadcastFloat(enemyArchonLocation3x, locations.get(2).x);
            robotController.broadcastFloat(enemyArchonLocation3y, locations.get(2).y);
        }
    }

    public void resetBroadcasts() throws GameActionException {
        robotController.broadcastInt(unsettledGardeners,0);
        robotController.broadcastInt(settledGardeners,0);
        robotController.broadcastInt(soldierCount,0);
        robotController.broadcastInt(scoutCount,0);
    }

    public int getUnsettledGardenerCount() throws GameActionException {
        return robotController.readBroadcastInt(unsettledGardeners);
    }

    public int getSettledGardenerCount() throws GameActionException {
        return robotController.readBroadcastInt(settledGardeners);
    }

    public void incrementSettledGardeners() throws GameActionException {
        int currentGardenerCount = robotController.readBroadcastInt(settledGardeners);
        robotController.broadcastInt(settledGardeners,currentGardenerCount + 1);
    }

    public void incrementUnsettledGardeners() throws GameActionException {
        int currentGardenerCount = robotController.readBroadcastInt(unsettledGardeners);
        robotController.broadcastInt(unsettledGardeners,currentGardenerCount + 1);
    }

    public void incrementSoldiers() throws GameActionException {
        int currentSoldierCount = robotController.readBroadcastInt(soldierCount);
        robotController.broadcastInt(soldierCount, currentSoldierCount + 1);
    }

    public void incrementScouts() throws GameActionException {
        int currentScoutCount = robotController.readBroadcastInt(scoutCount);
        robotController.broadcastInt(scoutCount, currentScoutCount + 1);
    }
}
