package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

import java.util.*;

//TODO Archon should do the majority of the processing and planning.
//  Sort out where gardeners should be placed, and give them a destination to move towards.
//  Do a circular check the same way that gardeners plant trees.  Concentric circles around the archon's
//  original starting position.  With a buffer for the Archon to wiggle about to avoid bullets.  At least 2 units per side.
//  Should read transmissions and use the info to coordinate soldiers
//TODO build unit maximums based on battlefield conditions instead of set maximums
//ex: scanning for neutral trees for lumberjack count
//ex2: more defending soldiers if there's lots of scouts
//TODO gardeners call for help when attacked. defender soldiers respond to calls for help
public class Archon extends Robot {
    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;

    private int maxGardeners;
    private int maxUnsettledGardeners;
    private int maxSoldiers;
    private int maxScouts;
    private boolean isFirstTurn;

    public Archon() {
        maxGardeners = 15;
        maxUnsettledGardeners = 3;
        maxSoldiers = 15;
        maxScouts = 1;
        isFirstTurn = true;

        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController);
        navigationSystem = new NavigationSystem(robotController); //Add SensorArray?
    }

    @Override
    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();

                getEnemyArchonLocations();
                checkDonateWin();
                navigationSystem.dodgeBullets();

                setBuildNumbers();
                broadcastAntenna.resetBroadcasts();

                tryHireGardener();

                isFirstTurn = false;
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    private void tryHireGardener() throws GameActionException {
        int unsettledGardenerCount = broadcastAntenna.getUnsettledGardenerCount();
        int settledGardenerCount = broadcastAntenna.getSettledGardenerCount();

        //Limit total number of gardeners
        if(unsettledGardenerCount + settledGardenerCount >= maxGardeners) {
            return;
        }

        //Limit number of unsettled gardeners to minimize wasted bullets and space
        if(unsettledGardenerCount >= maxUnsettledGardeners) {
            return;
        }

        //TODO find better way to determine empty space around with minimal checks.
        //Try 8 cardinal directions for building a gardener.
        //Hex based not required due to typically being in open ground
        for(int i = 0; i < 8; i++) {
            Direction direction = new Direction(i * 0.785398f);
            if (robotController.canHireGardener(direction)) {
                robotController.hireGardener(direction);
                System.out.println("Hiring New Gardener -- Settled(" + settledGardenerCount + ") -- Unsettled(" + unsettledGardenerCount + ")");
                break;
            }
        }
    }

    //TODO Add logic to dictate numbers based on current situation
    private void setBuildNumbers() throws GameActionException {
        broadcastAntenna.setSoldierBuildCount(maxSoldiers);
        broadcastAntenna.setScoutBuildCount(maxScouts);
    }

    private void checkDonateWin() throws GameActionException {
        int pointsToWin = GameConstants.VICTORY_POINTS_TO_WIN - robotController.getTeamVictoryPoints();
        double bulletsToWin = Math.ceil(pointsToWin * robotController.getVictoryPointCost());
        float currentBulletCount = robotController.getTeamBullets();

        if (currentBulletCount >= bulletsToWin) {
            System.out.println("Donation Victory --  Donating " + currentBulletCount + " bullets.");
            robotController.donate(robotController.getTeamBullets());
        }
    }

    private void getEnemyArchonLocations() throws GameActionException {
        List<MapLocation> locations;
        if(isFirstTurn) { //TODO change to check if already set, otherwise multiple archons each do this once.
            locations = Arrays.asList(robotController.getInitialArchonLocations(enemy));
        }
        else {
            locations = new ArrayList<>();
            List<RobotInfo> robots = sensorArray.findNearbyRobotsByType(RobotType.ARCHON);
            for(RobotInfo robot: robots) {
                locations.add(robot.location);
            }
        }

        broadcastAntenna.setArchonLocations(locations);
    }
}
