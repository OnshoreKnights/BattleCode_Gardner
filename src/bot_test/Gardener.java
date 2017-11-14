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
        //TODO add check that it's at least 4 away from archon to leave room for travel.  maybe 5 to be safe
        MapLocation tempLocation = robotController.getLocation();
        if (!(robotController.isCircleOccupiedExceptByThisRobot(tempLocation, 2.0f))) {
            settled = true;
            location = tempLocation;
        }
    }

    public void tryPlantingTrees() throws GameActionException {
        for(int i = 0; i < maxTrees; i++) {
            Direction direction = new Direction(i * 60f);

            if (!robotController.isCircleOccupied(location.add(direction, 2), 1f)) {
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

    public void tryBuildSoldier() throws GameActionException {
        Direction direction = new Direction(300f);
        if (soldierCount < maxSoldiers && robotController.canBuildRobot(RobotType.SOLDIER, direction)) {
            robotController.buildRobot(RobotType.SOLDIER, direction);
            soldierCount++;
        }
    }
}
