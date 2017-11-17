package bot_test;

import battlecode.common.*;

import java.util.ArrayList;

public class Archon extends Robot {
    int maxGardeners = 15;
    int maxUnsettledGardeners = 3;
    int maxSoldiers = 5;
    boolean enemyArchonsLocated = false;


    @Override
    //TODO Archon should do the majority of the processing and planning.
    //  Sort out where gardeners should be placed, and give them a destination to move towards.
    //  Do a circular check the same way that gardeners plant trees.  Concentric circles around the archon's
    //  original starting position.  With a buffer for the Archon to wiggle about to avoid bullets.
    //  Should read transmissions and use the info to coordinate soldiers
    public void onUpdate() {
        while (true) {
            try {
                getEnemyArchonSpawnLocations();

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
        int unsettledGardenerCount = robotController.readBroadcastInt(BroadcastChannels.unsettledGardeners);
        int settledGardenerCount = robotController.readBroadcastInt(BroadcastChannels.settledGardeners);

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
                System.out.println("Hiring gardener");
                break;
            }
        }
    }

    private void setSoldiersToHireCount() throws GameActionException {
        int soldierCount = robotController.readBroadcastInt(BroadcastChannels.soldierCount);
        if(soldierCount < maxSoldiers) {
            robotController.broadcastInt(BroadcastChannels.soldiersToHire, maxSoldiers - soldierCount);
        }
    }

    public void checkDonateWin() throws GameActionException {
        int pointsToWin = 1000 - robotController.getTeamVictoryPoints();
        double bulletsToWin = Math.ceil(pointsToWin * robotController.getVictoryPointCost());
        float currentBulletCount = robotController.getTeamBullets();

        if (currentBulletCount >= bulletsToWin) {
            System.out.println("Enough bullets saved for a point victory.  Donating " + currentBulletCount + " bullets.");
            robotController.donate(robotController.getTeamBullets());
        }
    }

    //TODO change "magic numbers" to a broadcast channel enum
    public void resetBroadcasts() throws GameActionException  {
        robotController.broadcastInt(BroadcastChannels.unsettledGardeners,0);
        robotController.broadcastInt(BroadcastChannels.settledGardeners,0);
        robotController.broadcastInt(BroadcastChannels.soldierCount,0);
    }

    private void getEnemyArchonSpawnLocations() throws GameActionException {
        if(enemyArchonsLocated) {
            return;
        }

        int archonCount = 0;
        MapLocation[] locations = robotController.getInitialArchonLocations(enemy);

        for(MapLocation location: locations) {
            if (archonCount == 0) {
                System.out.printf("Broadcasting archon 1 at location [%a,%a]", location.x, location.y);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation1x,location.x);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation1y,location.y);
            }
            else if(archonCount == 1) {
                System.out.printf("Broadcasting archon 2 at location [%a,%a]", location.x, location.y);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation2x,location.x);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation2y,location.y);
            }
            else if (archonCount == 2) {
                System.out.printf("Broadcasting archon 3 at location [%a,%a]", location.x, location.y);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation3x,location.x);
                robotController.broadcastFloat(BroadcastChannels.enemyArchonLocation3y,location.y);
            }
            archonCount++;
        }
        enemyArchonsLocated = true;
    }
}
