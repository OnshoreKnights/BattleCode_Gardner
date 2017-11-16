package bot_test;

import battlecode.common.*;

import java.util.ArrayList;

public class Archon extends Robot {
    int maxGardeners = 15;
    int maxUnsettledGardeners = 3;
    int maxSoldiers = 5;

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

                tryHireGardener();
                setSoldiersToHireCount();

                //wiggle about randomly, mostly to avoid bullets.
                Direction direction = randomDirection();
                tryMove(direction);

                resetBroadcasts();
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
    public void tryHireGardener() throws GameActionException {
        int unsettledGardenerCount = robotController.readBroadcastInt(0);
        int settledGardenerCount = robotController.readBroadcastInt(1);

        if(unsettledGardenerCount + settledGardenerCount >= maxGardeners) {
            return;
        }

        if(unsettledGardenerCount >= maxUnsettledGardeners) {
            return;
        }

        //Try 8 cardinal directions for building a gardener.
        //Hex based not required due to typically being in open ground
        for(int i = 0; i < 8; i++) {
            Direction direction = new Direction(i * 0.785398f);
            if (robotController.canHireGardener(direction)) {
                robotController.hireGardener(direction);
                break;
            }
        }
    }

    private void setSoldiersToHireCount() throws GameActionException {
        int soldierCount = robotController.readBroadcastInt(2);
        if(soldierCount < maxSoldiers) {
            robotController.broadcastInt(3, maxSoldiers - soldierCount);
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

    //TODO change "magic numbers" to a broadcast channel enum
    public void resetBroadcasts() throws GameActionException  {
        robotController.broadcastInt(0,0); //unsettled gardeners
        robotController.broadcastInt(1,0); //settled gardeners
        robotController.broadcastInt(2,0); //soldier count
        //do not reset soldiersToHire count      //soldiers to hire
    }
}
