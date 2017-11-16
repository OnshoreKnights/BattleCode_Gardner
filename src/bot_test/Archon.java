package bot_test;

import battlecode.common.*;

public class Archon extends Robot {
    int gardenerCount = 0;
    int maxGardeners = 15;

    @Override
    //TODO Archon should do the majority of the processing and planning.
    //  Sort out where gardeners should be placed, and give them a destination to move towards.
    //  Do a circular check the same way that gardeners plant trees.  Concentric circles around the archon's
    //  original starting position.  With a buffer for the Archon to wiggle about to avoid bullets.
    //  Should read transmissions and use the info to coordinate soldiers
    public void onUpdate() {
        while (true) {
            try {
                checkDonateWin();

                Direction direction = randomDirection();

                tryHireGardener(direction);

                tryMove(direction);
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    //TODO Split between different gardener types.
    // Farmers = 6 trees surrounding.  Unless I change to a more mobile format.  Place in back, tightly clustered
    // Hybrids = 4 trees and two open slots for soliders, tanks, etc.  Middle placement, leaving room for tanks to move.
    // Factories = aggressively placed gardeners that just spawn other troops.  Place behind trees, or plant one tree in front of them.
    public void tryHireGardener(Direction direction) throws GameActionException {
        gardenerCount = 0;
        for (RobotInfo robot: robotController.senseNearbyRobots(-1, myTeam))
             if(robot.type == robotType.GARDENER) {
            gardenerCount ++;
        }
        if(gardenerCount >= maxGardeners) {
            return;
        }

        if (robotController.canHireGardener(direction)) {
            robotController.hireGardener(direction);
        }
    }

    public void checkDonateWin() throws GameActionException {
        int pointsToWin = 1000 - robotController.getTeamVictoryPoints();
        double bulletsToWin = Math.ceil(pointsToWin * robotController.getVictoryPointCost());

        if (robotController.getTeamBullets() >= bulletsToWin) {
            System.out.println("Enough bullets saved for a point victory.  Donating " + robotController.getTeamBullets() + " bullets.");
            robotController.donate(robotController.getTeamBullets());
        }
    }
}
