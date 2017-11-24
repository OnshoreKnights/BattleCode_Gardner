package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;


//TODO change type of gardener based on how many I have and how many of each I need.
// Farmers = 6 trees surrounding.  Unless I change to a more mobile format.  Place in back, tightly clustered
// Hybrids = 4 trees and two open slots for soldiers, tanks, etc.  Middle placement, leaving room for tanks to move.
// Factories = aggressively placed gardeners that just spawn other troops.  Place behind trees, or plant one tree in front of them.
//TODO build one scout IMMEDIATELY to get a head start on collecting bullets. Then build trees.  Then more robots.
class Gardener extends Robot {
    private boolean settled;
    private int maxTrees;

    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;
    private MapLocation currentLocation;

    public Gardener() {
        settled = false;
        maxTrees = 4;

        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController);
        navigationSystem = new NavigationSystem(robotController); //Add SensorArray?
    }
    public void onUpdate() {
        while (true) {
            try {
                currentLocation = robotController.getLocation();
                sensorArray.reset();

                if (!settled) {
                    navigationSystem.tryMove(randomDirection());
                    trySettle();
                }

                if(!settled) {
                    broadcastAntenna.incrementUnsettledGardeners();
                }else {
                    broadcastAntenna.incrementSettledGardeners();
                    tryPlantingTrees();
                    tryBuildScout();
                    tryBuildSoldier();
                }
                tryWateringTrees();
                tryShakeTree();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    public void trySettle() throws GameActionException {
        //Leave enough room from the edge of the map to build trees all around it
        if(!isAwayFromMapEdge(currentLocation,3)) {
            return;
        }

        //Do not build too close to own trees.  Ignore neutral trees, they'll likely be destroyed soon.
        if(robotController.senseNearbyTrees(2, myTeam).length > 0) {
            return;
        }

        //Ignore other bots.  Don't build too close to other gardeners or the Archon
        for (RobotInfo robot: robotController.senseNearbyRobots(5, myTeam)) {
            if(robot.type == RobotType.GARDENER || robot.type == RobotType.ARCHON) {
                return;
            }
        }

        settled = true;
        System.out.println("Gardener settled");
    }

    public void tryPlantingTrees() throws GameActionException {
        for(int i = 0; i < maxTrees; i++) {
            Direction direction = new Direction(i * 1.0472f);

            if (robotController.canPlantTree(direction)) {
                robotController.plantTree(direction);
                return;
            }
        }
    }

    public void tryWateringTrees() throws GameActionException {
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

    private void tryBuildScout() throws GameActionException {
        int scoutsToHire = robotController.readBroadcastInt(BroadcastAntenna.scoutsToHire);
        if(scoutsToHire <= 0) {
            return;
        }

        //Build in the direction of the intentional opening
        Direction direction = new Direction(5.236f);
        if (robotController.canBuildRobot(RobotType.SCOUT, direction)) {
            robotController.buildRobot(RobotType.SCOUT, direction);
            robotController.broadcastInt(BroadcastAntenna.scoutsToHire, scoutsToHire - 1);
        }
    }

    //TODO split between different soldier types
    // Guards and Hunters
    public void tryBuildSoldier() throws GameActionException {
        int soldiersToHire = robotController.readBroadcastInt(BroadcastAntenna.soldiersToHire);
        if(soldiersToHire == 0) {
            return;
        }

        //Build in the direction of the intentional opening
        Direction direction = new Direction(5.236f);
        if (robotController.canBuildRobot(RobotType.SOLDIER, direction)) {
            robotController.buildRobot(RobotType.SOLDIER, direction);
            robotController.broadcastInt(BroadcastAntenna.soldiersToHire, soldiersToHire - 1); //TODO broadcastAntenna
            System.out.println("Soldier created");
        }
    }
}
