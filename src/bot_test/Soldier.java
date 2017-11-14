package bot_test;

import battlecode.common.*;

public class Soldier extends Robot {
    public void onUpdate() {
        while (true) {
            try {
                MapLocation myLocation = robotController.getLocation();

                RobotInfo[] robots = robotController.senseNearbyRobots(-1, enemy);

                //TODO prioritize certain targets or check for multiple potential hits
                if (robots.length > 0) {
                    while (robotController.canFireSingleShot() && robots[0] != null && robots[0].health > 0) {
                        robotController.fireSingleShot(myLocation.directionTo(robots[0].location));
                    }
                }

                tryMove(randomDirection());

                //TODO try to shoot again after moving if haven't already shot

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
