package bot_test;

import battlecode.common.*;

public class Archon extends Robot {
    int gardenerCount = 0;
    int maxGardeners = 15;

    @Override
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

    //TODO place gardeners at 39 degree angles from each other if possible.  9 open spaces
    public void tryHireGardener(Direction direction) throws GameActionException {
        if (robotController.canHireGardener(direction) && gardenerCount < maxGardeners) {
            robotController.hireGardener(direction);
            gardenerCount++;

            System.out.println("New gardener hired. " + gardenerCount + " / " + maxGardeners);
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
