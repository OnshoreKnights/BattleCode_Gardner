package test_bot_CoreyRoberts.Components;

import battlecode.common.*;
import scala.Tuple3;

import java.util.*;

public class BroadcastAntenna {
    private RobotController robotController;
    private Integer empty;

    public static class Channels {
        private static int gardenerChannels = 50;
        private static int soldierChannels = 100;
        private static int scoutChannels = 10;
        private static int lumberjackChannels = 10;
        private static int tankChannels = 10;
        private static int helpChannels = 3;
        private static int targetChannels = 3;

        //(INT) 0 (Empty) or X coordinate
        //(INT) 0 (Empty) or Y coordinate
        private static int enemyArchon1X = 0;
        private static int enemyArchon1Y = 1;
        private static int enemyArchon2X = 2;
        private static int enemyArchon2Y = 3;
        private static int enemyArchon3X = 4;
        private static int enemyArchon3Y = 5;

        //(INT) Count
        private static int scoutsToHire = 6;
        private static int soldiersToHire = 7;
        private static int lumberjacksToHire = 8;
        private static int tanksToHire = 9;

        //(INT) 0 (Empty) or Robot Subtype
        private static int gardenersFirstChannel = 10;
        private static int gardenersLastChannel = gardenersFirstChannel + gardenerChannels - 1;
        private static int soldiersFirstChannel = gardenersLastChannel + 1;
        private static int soldiersLastChannel = soldiersFirstChannel + soldierChannels - 1;
        private static int scoutsFirstChannel = soldiersLastChannel + 1;
        private static int scoutsLastChannel = scoutsFirstChannel + scoutChannels - 1;
        private static int lumberjacksFirstChannel = scoutsLastChannel + 1;
        private static int lumberjacksLastChannel = lumberjacksFirstChannel + lumberjackChannels - 1;
        private static int tanksFirstChannel = lumberjacksLastChannel + 1;
        private static int tanksLastChannel = tanksFirstChannel + tankChannels - 1;

        //(INT) 0 (Empty) or Robot Type
        //(INT) 0 (Empty) or X coordinate
        //(INT) 0 (Empty) or Y coordinate
        private static int helpFirstChannel = tanksLastChannel + 1;
        private static int helpLastChannel = helpFirstChannel + helpChannels - 1;

        //(INT) 0 (Empty) or Robot Type
        //(INT) 0 (Empty) or X coordinate
        //(INT) 0 (Empty) or Y coordinate
        private static int markFirstChannel = helpLastChannel + 1;
        private static int markLastChannel = markFirstChannel + targetChannels - 1;
    }

    public BroadcastAntenna(RobotController _robotController) {
        robotController = _robotController;
        empty = Integer.MAX_VALUE;
    }

    public void resetBroadcasts() throws GameActionException {
        //Do not reset enemy archon locations

        //Reset Build Counts
        robotController.broadcastInt(Channels.scoutsToHire, 0);
        robotController.broadcastInt(Channels.soldiersToHire, 0);
        robotController.broadcastInt(Channels.lumberjacksToHire, 0);
        robotController.broadcastInt(Channels.tanksToHire, 0);

        //Reset Gardeners
        for (int i = Channels.gardenersFirstChannel; i <= Channels.gardenersLastChannel; i++) {
            robotController.broadcastInt(i, 0);
        }

        //Reset Scouts
        for (int i = Channels.scoutsFirstChannel; i <= Channels.scoutsLastChannel; i++) {
            robotController.broadcastInt(i, 0);
        }

        //Reset Soldiers
        for (int i = Channels.soldiersFirstChannel; i <= Channels.soldiersLastChannel; i++) {
            robotController.broadcastInt(i, 0);
        }

        //Reset Lumberjacks
        for (int i = Channels.lumberjacksFirstChannel; i <= Channels.lumberjacksLastChannel; i++) {
            robotController.broadcastInt(i, 0);
        }

        //Reset Tanks
        for (int i = Channels.tanksFirstChannel; i <= Channels.tanksLastChannel; i++) {
            robotController.broadcastInt(i, 0);
        }

        //Reset Help Channels
        int firstChannelMod = Channels.helpFirstChannel % 3;
        for (int i = Channels.helpFirstChannel; i <= Channels.helpLastChannel; i++) {
            if (i % 3 == firstChannelMod) {
                robotController.broadcastInt(i, empty);
            } else {
                robotController.broadcastFloat(i, 0);
            }
        }

        //Do not reset marks each turn
    }

    public List<MapLocation> getArchonLocations() throws GameActionException {
        List<MapLocation> archonLocations = new ArrayList<>();

        float archon1x = robotController.readBroadcastFloat(Channels.enemyArchon1X);
        float archon2x = robotController.readBroadcastFloat(Channels.enemyArchon2X);
        float archon3x = robotController.readBroadcastFloat(Channels.enemyArchon3X);
        if (archon1x != 0) {
            archonLocations.add(new MapLocation(archon1x, robotController.readBroadcastFloat(Channels.enemyArchon1Y)));
        }
        if (archon2x != 0) {
            archonLocations.add(new MapLocation(archon2x, robotController.readBroadcastFloat(Channels.enemyArchon2Y)));
        }
        if (archon3x != 0) {
            archonLocations.add(new MapLocation(archon3x, robotController.readBroadcastFloat(Channels.enemyArchon3Y)));
        }
        return archonLocations;
    }

    public void setArchonLocations(List<MapLocation> locations) throws GameActionException {
        if (locations.size() >= 1) {
            robotController.broadcastFloat(Channels.enemyArchon1X, locations.get(0).x);
            robotController.broadcastFloat(Channels.enemyArchon1Y, locations.get(0).y);
        } else {
            robotController.broadcastFloat(Channels.enemyArchon1X, 0);
            robotController.broadcastFloat(Channels.enemyArchon1Y, 0);
        }
        if (locations.size() >= 2) {
            robotController.broadcastFloat(Channels.enemyArchon2X, locations.get(1).x);
            robotController.broadcastFloat(Channels.enemyArchon2Y, locations.get(1).y);
        } else {
            robotController.broadcastFloat(Channels.enemyArchon2X, 0);
            robotController.broadcastFloat(Channels.enemyArchon2Y, 0);
        }
        if (locations.size() >= 3) {
            robotController.broadcastFloat(Channels.enemyArchon3X, locations.get(2).x);
            robotController.broadcastFloat(Channels.enemyArchon3Y, locations.get(2).y);
        } else {
            robotController.broadcastFloat(Channels.enemyArchon3X, 0);
            robotController.broadcastFloat(Channels.enemyArchon3Y, 0);
        }
    }

    public HashMap<RobotType, Integer> getHireCounts() throws GameActionException {
        return new HashMap<RobotType, Integer>() {{
            put(RobotType.SCOUT, robotController.readBroadcast(Channels.scoutsToHire));
            put(RobotType.SOLDIER, robotController.readBroadcast(Channels.soldiersToHire));
            put(RobotType.LUMBERJACK, robotController.readBroadcast(Channels.lumberjacksToHire));
            put(RobotType.TANK, robotController.readBroadcast(Channels.tanksToHire));
        }};
    }

    public Integer getScoutsToHire() throws GameActionException {
        return robotController.readBroadcast(Channels.scoutsToHire);
    }

    public Integer getSoldiersToHire() throws GameActionException {
        return robotController.readBroadcast(Channels.soldiersToHire);
    }

    public Integer getLumberjacksToHire() throws GameActionException {
        return robotController.readBroadcast(Channels.lumberjacksToHire);
    }

    public Integer getTanksToHire() throws GameActionException {
        return robotController.readBroadcast(Channels.tanksToHire);
    }

    public void setHireCount(RobotType type, int count) throws GameActionException {
        int channel = 0;
        switch (type) {
            case SCOUT:
                channel = Channels.scoutsToHire;
                break;
            case SOLDIER:
                channel = Channels.soldiersToHire;
                break;
            case LUMBERJACK:
                channel = Channels.lumberjacksToHire;
                break;
            case TANK:
                channel = Channels.tanksToHire;
                break;
        }

        if (channel == 0) {
            System.out.println("ERROR: invalid robot type entered");
            return;
        }
        robotController.broadcastInt(channel, count);
    }

    private List<Integer> getRobotValues(int firstChannel, int lastChannel) throws GameActionException {
        List<Integer> robots = new ArrayList<>();
        for (int i = firstChannel; i <= lastChannel; i++) {
            int value = robotController.readBroadcastInt(i);
            if (value != 0) {
                robots.add(value);
            } else {
                return robots;
            }
        }
        return robots;
    }

    public List<Integer> getGardeners() throws GameActionException {
        return getRobotValues(Channels.gardenersFirstChannel, Channels.gardenersLastChannel);
    }

    public List<Integer> getScouts() throws GameActionException {
        return getRobotValues(Channels.scoutsFirstChannel, Channels.scoutsLastChannel);
    }

    public List<Integer> getSoldiers() throws GameActionException {
        return getRobotValues(Channels.soldiersFirstChannel, Channels.soldiersLastChannel);
    }

    public List<Integer> getLumberjacks() throws GameActionException {
        return getRobotValues(Channels.lumberjacksFirstChannel, Channels.lumberjacksLastChannel);
    }

    public List<Integer> getTanks() throws GameActionException {
        return getRobotValues(Channels.tanksFirstChannel, Channels.tanksLastChannel);
    }

    private void addRobot(int firstChannel, int lastChannel, int newValue) throws GameActionException {
        for (int i = firstChannel; i <= lastChannel; i++) {
            int value = robotController.readBroadcastInt(i);
            if (value == 0) {
                robotController.broadcastInt(i, newValue);
                return;
            }
        }
    }

    //Set gardener status on the next zero (empty) gardener channel.
    public void addGardener(int gardenerType) throws GameActionException {
        addRobot(Channels.gardenersFirstChannel, Channels.gardenersLastChannel, gardenerType);
    }

    public void addScout(int scoutType) throws GameActionException {
        addRobot(Channels.scoutsFirstChannel, Channels.scoutsLastChannel, scoutType);
    }

    public void addSoldier(int soldierType) throws GameActionException {
        addRobot(Channels.soldiersFirstChannel, Channels.soldiersLastChannel, soldierType);
    }

    public void addLumberjack(int lumberjackType) throws GameActionException {
        addRobot(Channels.lumberjacksFirstChannel, Channels.lumberjacksLastChannel, lumberjackType);
    }

    public void addTank(int tankType) throws GameActionException {
        addRobot(Channels.tanksFirstChannel, Channels.tanksLastChannel, tankType);
    }

    public Tuple3<RobotType, Float, Float> getCallForHelp() throws GameActionException {
            int robotType = robotController.readBroadcastInt(Channels.helpFirstChannel);
            if (robotType == empty) {
                return null;
            }
            Float xCoordinate = robotController.readBroadcastFloat(Channels.helpFirstChannel + 1);
            Float yCoordinate = robotController.readBroadcastFloat(Channels.helpFirstChannel + 2);
            return new Tuple3<>(RobotType.values()[robotType], xCoordinate, yCoordinate);
    }

    public void addCallForHelp(RobotType robotType, Float xCoordinate, Float yCoordinate) throws GameActionException {
        int previousRobotType = robotController.readBroadcastInt(Channels.helpFirstChannel);
        if (previousRobotType == empty || getPriority(robotType) > getPriority(RobotType.values()[previousRobotType])) {
            robotController.broadcastInt(Channels.helpFirstChannel, robotType.ordinal());
            robotController.broadcastFloat(Channels.helpFirstChannel + 1, xCoordinate);
            robotController.broadcastFloat(Channels.helpFirstChannel + 2, yCoordinate);
        }
    }

    public Tuple3<RobotType, Float, Float> getMark() throws GameActionException {
        int robotType = robotController.readBroadcastInt(Channels.markFirstChannel);
        if (robotType == empty) {
            return null;
        }
        Float xCoordinate = robotController.readBroadcastFloat(Channels.markFirstChannel + 1);
        Float yCoordinate = robotController.readBroadcastFloat(Channels.markFirstChannel + 2);

        return new Tuple3<>(RobotType.values()[robotType], xCoordinate, yCoordinate);
    }

    public void addMark(RobotType robotType, Float xCoordinate, Float yCoordinate) throws GameActionException {
        robotController.broadcastInt(Channels.markFirstChannel, robotType.ordinal());
        robotController.broadcastFloat(Channels.markFirstChannel + 1, xCoordinate);
        robotController.broadcastFloat(Channels.markFirstChannel + 2, yCoordinate);
    }

    public void resetMark() throws GameActionException {
        robotController.broadcastInt(Channels.markFirstChannel, empty);
        robotController.broadcastFloat(Channels.markFirstChannel + 1, 0f);
        robotController.broadcastFloat(Channels.markFirstChannel + 2, 0f);
    }

    //TODO move to utility component
    //TODO make sure help priority is different from attack priority
    private int getPriority(RobotType type) {
        int priority;
        switch(type) {
            case ARCHON:
                priority = 7;
                break;
            case GARDENER:
                priority = 6;
                break;
            case LUMBERJACK:
                priority = 5;
                break;
            case SCOUT:
                priority = 4;
                break;
            case SOLDIER:
                priority = 3;
                break;
            case TANK:
                priority = 2;
                break;
            default: //tree
                priority = 1;
        }

        return priority;
    }
}
