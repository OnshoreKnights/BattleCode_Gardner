package bot_test;

import battlecode.common.*;

class Gardener extends Robot {
    private boolean settled = false;
    private int maxTrees = 5;   //Leave room for making robots. Change to 4 if tanks are involved (radius 2)
    private int soldierCount = 0;
    private int maxSoldiers = 1;    //TODO change to global count that keeps track of living soldiers
    private MapLocation location;

    //TODO remove settling once I can figure out how to lay them out in a grid pattern (2 spaces between)
    // or in rows with a break in the middle.
    public void onUpdate() {
        while (true) {
            try {
                if (!settled) {
                    tryMove(randomDirection());
                    trySettle();
                }

                if(settled) {
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
        Direction direction = new Direction(5.236f);
        if (soldierCount < maxSoldiers && robotController.canBuildRobot(RobotType.SOLDIER, direction)) {
            robotController.buildRobot(RobotType.SOLDIER, direction);
            soldierCount++;
        }
    }
}
