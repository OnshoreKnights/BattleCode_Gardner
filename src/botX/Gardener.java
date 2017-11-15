package botX;

import battlecode.common.*;

class Gardener extends Robot {

    public void onUpdate() {
        boolean settled = false;
        Direction gardenerDir = null;

        while (true) {
            try {

                int xPos = robotController.readBroadcast(0);
                int yPos = robotController.readBroadcast(1);
                MapLocation archonLocation = new MapLocation(xPos, yPos);

                Direction dir = randomDirection();
                if (gardenerDir == null) {
                    gardenerDir = dir;
                }
                if (!(robotController.isCircleOccupiedExceptByThisRobot(robotController.getLocation(), robotController.getType().bodyRadius * 4.0f))) {
                    settled = true;
                    if (robotController.canPlantTree(dir)) {
                        robotController.plantTree(dir);
                    }
                }
                if (settled) {
                    if (robotController.canPlantTree(dir)) {
                        robotController.plantTree(dir);
                    }
                }

                if (robotController.canBuildRobot(RobotType.SOLDIER, dir)) {
                    robotController.buildRobot(RobotType.SOLDIER, dir);
                }

                TreeInfo[] trees = robotController.senseNearbyTrees(robotController.getType().bodyRadius * 2, robotController.getTeam());
                TreeInfo minHealthTree = null;
                for (TreeInfo tree : trees) {
                    if (tree.health < 70) {
                        if (minHealthTree == null || tree.health < minHealthTree.health) {
                            minHealthTree = tree;
                        }
                    }
                }
                if (minHealthTree != null) {
                    robotController.water(minHealthTree.ID);
                }

                if (!settled) {
                    if (tryMove(gardenerDir)) {
                        System.out.println("moved");
                    } else {
                        gardenerDir = randomDirection();
                        tryMove(gardenerDir);
                    }
                }

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A robotController Exception");
                e.printStackTrace();
            }
        }
    }
}
