package bot_test;

import battlecode.common.*;

class Gardener extends Robot {
    private boolean settled = false;
    private int maxTrees = 5;   //Leave room for making robots. Change to 4 if tanks are involved (radius 2)
    private MapLocation location;

    public void onUpdate() {
        while (true) {
            try {
                if (!settled) {
                    tryMove(randomDirection());
                    trySettle();
                }

                if(!settled) {
                    broadcastUnsettled();
                }else {
                    broadcastSettled();
                    tryPlantingTrees();
                    tryBuildSoldier();
                }
                tryWateringTrees();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    //Add self to unsettled gardener count, then broadcast it to the archon
    public void broadcastUnsettled() throws GameActionException {
        int currentGardenerCount = robotController.readBroadcastInt(BroadcastChannels.unsettledGardeners);
        robotController.broadcastInt(BroadcastChannels.unsettledGardeners,currentGardenerCount + 1);
    }

    //Add self to settled gardener count, then broadcast it to the archon
    public void broadcastSettled() throws GameActionException {
        int currentGardenerCount = robotController.readBroadcastInt(BroadcastChannels.settledGardeners);
        robotController.broadcastInt(BroadcastChannels.settledGardeners,currentGardenerCount + 1);
    }

    public void trySettle() throws GameActionException {
        MapLocation tempLocation = robotController.getLocation();

        //Leave enough room from the edge of the map to build trees all around it
        if(!isAwayFromMapEdge(tempLocation,3)) {
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
        location = tempLocation;
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

    //TODO split between different soldier types
    // Guards and Hunters
    public void tryBuildSoldier() throws GameActionException {
        int soldiersToHire = robotController.readBroadcastInt(BroadcastChannels.soldiersToHire);
        if(soldiersToHire == 0) {
            return;
        }

        //Build in the direction of the intentional opening
        Direction direction = new Direction(5.236f);
        if (robotController.canBuildRobot(RobotType.SOLDIER, direction)) {
            robotController.buildRobot(RobotType.SOLDIER, direction);
            robotController.broadcastInt(BroadcastChannels.soldiersToHire, soldiersToHire - 1);
            System.out.println("Soldier created");
        }
    }
}
