package botX;

import battlecode.common.*;

import static botX.RobotPlayer.rc;

public class Soldier extends Robot {

    public void onUpdate() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        while (true) {

            try {
                MapLocation myLocation = rc.getLocation();

                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                if (robots.length > 0) {
                    while (rc.canFireSingleShot() && robots[0] != null) {
                        rc.fireSingleShot(myLocation.directionTo(robots[0].location));
                    }
                }

                tryMove(randomDirection());

                Clock.yield();
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
