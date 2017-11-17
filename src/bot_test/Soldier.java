package bot_test;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation myLocation = robotController.getLocation();
    MapLocation targetLocation;

    public void onUpdate() {
        while (true) {
            try {
                selectNextTargetLocation();
                tryMoveToTarget();

                tryAttackBots();
                tryAttackTrees();

                broadcastAlive();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    //TODO If two bots are equal in priority, see which one is closer
    //TODO deal with getting stuck between two equal distant targets.
    private void selectNextTargetLocation() throws GameActionException {
        RobotInfo currentTarget = null;
        targetLocation = null;

        //Set target to highest priority bot
        RobotInfo[] robots = robotController.senseNearbyRobots(-1, enemy);
        if(robots.length > 0) {
            for (RobotInfo robotInfo : robots) {
                //TODO TargetPriority accepts a RobotType and returns an int, for ocmparison
                if (currentTarget == null ||
                        TargetPriority.GetPriority(robotInfo.type) > TargetPriority.GetPriority(currentTarget.type)) {
                    currentTarget = robotInfo;
                }
            }
        }

        if(currentTarget != null) {
            System.out.println(currentTarget.type + " selected as soldier target");
            targetLocation = currentTarget.location;
        }

        //No bots were selected.  Search out distant archons, based on which is still alive.
        if(targetLocation == null){
            //TODO remove archon broadcasts whenever they are destroyed (find closest broadcast to their destroyed location)
            if(robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation1x) != 0) {
                float x = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation1x);
                float y = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation1y);
                targetLocation = new MapLocation(x,y);
                System.out.println("Archon1 selected as soldier target");
            }
            else if(robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation2x) != 0) {
                float x = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation2x);
                float y = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation2y);
                targetLocation = new MapLocation(x,y);
                System.out.println("Archon2 selected as soldier target");
            }
            else if(robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation3x) != 0) {
                float x = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation3x);
                float y = robotController.readBroadcastFloat(BroadcastChannels.enemyArchonLocation3y);
                targetLocation = new MapLocation(x,y);
                System.out.println("Archon3 selected as soldier target");
            }
        }

        //Archons cannot be found for whatever reason.  Move randomly.
        //TODO account for patrol mode for guards.
        if(targetLocation == null) {
            targetLocation = myLocation.add(randomDirection());
        }
    }

    private void tryMoveToTarget() throws GameActionException {
        tryMove(myLocation.directionTo(targetLocation));
        myLocation = robotController.getLocation();
    }

    private void broadcastAlive() throws GameActionException {
        int currentSoldierCount = robotController.readBroadcastInt(BroadcastChannels.soldierCount);
        robotController.broadcastInt(BroadcastChannels.soldierCount,currentSoldierCount + 1);
    }

    //TODO new function to determine shot type based on target health and distance.
    private void tryAttackBots() throws GameActionException {
        if(robotController.hasAttacked()) {
            return;
        }
        RobotInfo[] robots = robotController.senseNearbyRobots(-1, enemy);

        //TODO prioritize certain targets or check for multiple potential hits
        if (robots.length > 0) {
            if (robotController.canFireSingleShot() && robots[0] != null && robots[0].health > 0) {
                robotController.fireSingleShot(myLocation.directionTo(robots[0].location));
            }
        }
    }

    //TODO new function to determine shot type based on target health and distance.
    private void tryAttackTrees() throws GameActionException {
        if(robotController.hasAttacked()) {
            return;
        }
        TreeInfo[] trees = robotController.senseNearbyTrees(-1, enemy);
        if (trees.length > 0) {
            if(robotController.canFireSingleShot() && trees[0] != null && trees[0].health > 0) {
                robotController.fireSingleShot(myLocation.directionTo(trees[0].location));
            }
        }
    }
}
