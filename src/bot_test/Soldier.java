package bot_test;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation myLocation;

    public void onUpdate() {
        while (true) {
            try {
                myLocation = robotController.getLocation();

                tryAttack();

                tryMove(randomDirection());

                tryAttack();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    private void tryAttack() throws GameActionException {
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
}
