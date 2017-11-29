package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

//TODO change type of gardener based on how many I have and how many of each I need.
// Farmers = 6 trees surrounding.  Unless I change to a more mobile format.  Place in back, tightly clustered
// Builder = 4 trees and two open slots for soldiers, tanks, etc.  Middle placement, leaving room for tanks to move.
class Gardener extends Robot {
    private boolean settled;
    private int maxTreePlots;
    private boolean underAttack;
    private boolean hasBuiltInitialRobot;

    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;

    //TODO this may need to be moved to its own class for use in archon
    //Add new Subtypes package maybe
    public static class GardenerType {
        private static int Unsettled = 1;
        private static int Farmer = 2;
        private static int Builder = 3;
    }

    public Gardener() {
        settled = false;
        maxTreePlots = 4;
        underAttack = false;
        hasBuiltInitialRobot = false;

        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController, broadcastAntenna);
        navigationSystem = new NavigationSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                checkIfUnderAttack();

                //Build initial scout to start the match
                if(!hasBuiltInitialRobot) {
                    buildInitialRobot();
                }

                if (!settled) {
                    navigationSystem.tryMove(randomDirection());
                    sensorArray.reset();
                    trySettle();
                }

                if(settled) {
                    broadcastAntenna.addGardener(GardenerType.Builder);
                    tryPlantingTrees();
                    tryBuildRobot();
                }else {
                    broadcastAntenna.addGardener(GardenerType.Unsettled);
                }
                tryWateringTrees();
                tryShakeTree();

                //printBytecodeUsage();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    public void trySettle() throws GameActionException {
        //Leave enough room from the edge of the map to build trees all around it
        if(!isAwayFromMapEdge(sensorArray.currentLocation,3f)) {
            return;
        }

        //Do not build too close to own trees.  Ignore neutral trees, they'll likely be destroyed soon.
        if(robotController.senseNearbyTrees(2, myTeam).length > 0) {
            return;
        }

        //TODO move to sensor
        //Ignore other bots.  Don't build too close to other gardeners or the Archon
        for (RobotInfo robot: robotController.senseNearbyRobots(5, myTeam)) {
            if(robot.type == RobotType.GARDENER || robot.type == RobotType.ARCHON) {
                return;
            }
        }

        settled = true;
    }

    private void checkIfUnderAttack() throws GameActionException {
        if(sensorArray.surroundingEnemyRobots.size() > 0) {
            underAttack = true;
            broadcastAntenna.addCallForHelp(robotType, sensorArray.currentLocation.x, sensorArray.currentLocation.y);
        } else {
            underAttack = false;
        }
    }

    public void tryPlantingTrees() throws GameActionException {
        for(int i = 0; i < maxTreePlots; i++) {
            Direction direction = new Direction(i * 1.0472f);

            if (robotController.canPlantTree(direction)) {
                robotController.plantTree(direction);
                return;
            }
        }
    }

    public void tryWateringTrees() throws GameActionException {
        //TODO move to sensor
        TreeInfo[] trees = robotController.senseNearbyTrees(2, myTeam);

        TreeInfo minHealthTree = null;
        for (TreeInfo tree : trees) {
            if (tree.health < 95) {
                if (minHealthTree == null || tree.health < minHealthTree.health) {
                    minHealthTree = tree;
                }
            }
        }
        if (minHealthTree != null) {
            robotController.water(minHealthTree.ID);
        }
    }

    private void buildInitialRobot() throws GameActionException {
        //Under attack when spawned.  Create one soldier
        if(sensorArray.surroundingEnemyRobots.size() > 0 && tryBuildSoldier()) {
            hasBuiltInitialRobot = true;
            return;
        }

        //Large number of neutral trees. Create one lumberjack
        if(sensorArray.surroundingNeutralTrees.size() > 3 && tryBuildLumberjack()) {
            hasBuiltInitialRobot = true;
            return;
        }

        //Default to building one scout
        if(tryBuildScout()) {
            hasBuiltInitialRobot = true;
            return;
        }
    }

    private void tryBuildRobot() throws GameActionException {
        if(tryBuildScout() || tryBuildLumberjack() || tryBuildTank() || tryBuildSoldier()) {
            return;
        }
    }

    private boolean tryBuildScout() throws GameActionException {
        int scoutsToHire = broadcastAntenna.getScoutsToHire();
        if (scoutsToHire <= 0) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            Direction direction = new Direction(i * 1.0472f);
            if (robotController.canBuildRobot(RobotType.SCOUT, direction)) {
                robotController.buildRobot(RobotType.SCOUT, direction);
                broadcastAntenna.setHireCount(RobotType.SCOUT, scoutsToHire - 1);
                return true;
            }
        }
        return false;
    }

    private boolean tryBuildLumberjack() throws GameActionException {
        int lumberjacksToHire = broadcastAntenna.getLumberjacksToHire();
        if(lumberjacksToHire <= 0) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            Direction direction = new Direction(i * 1.0472f);
            if (robotController.canBuildRobot(RobotType.LUMBERJACK, direction)) {
                robotController.buildRobot(RobotType.LUMBERJACK, direction);
                broadcastAntenna.setHireCount(RobotType.LUMBERJACK, lumberjacksToHire - 1);
                return true;
            }
        }
        return false;
    }

    //TODO split between different soldier types
    // Guards and Hunters
    public boolean tryBuildSoldier() throws GameActionException {
        int soldiersToHire = broadcastAntenna.getSoldiersToHire();
        if(soldiersToHire == 0) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            Direction direction = new Direction(i * 1.0472f);
            if (robotController.canBuildRobot(RobotType.SOLDIER, direction)) {
                robotController.buildRobot(RobotType.SOLDIER, direction);
                broadcastAntenna.setHireCount(RobotType.SOLDIER, soldiersToHire - 1);
                return true;
            }
        }
        return false;
    }

    public boolean tryBuildTank() throws GameActionException {
        int tanksToHire = broadcastAntenna.getTanksToHire();
        if(tanksToHire == 0) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            Direction direction = new Direction((i * 1.0472f) + 0.5236f); //offset tank spawn by half a rotation.
            if (robotController.canBuildRobot(RobotType.TANK, direction)) {
                robotController.buildRobot(RobotType.TANK, direction);
                broadcastAntenna.setHireCount(RobotType.TANK, tanksToHire - 1);
                return true;
            }
        }
        return false;
    }
}
