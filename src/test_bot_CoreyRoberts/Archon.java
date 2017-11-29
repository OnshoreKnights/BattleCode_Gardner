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
    private int maxLumberjacks;
    private int maxTanks;
    private boolean isFirstTurn;
    private boolean underAttack;

    public Archon() {
        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController, broadcastAntenna);
        navigationSystem = new NavigationSystem(robotController); //Add SensorArray?

        maxGardeners = 15;
        maxUnsettledGardeners = 1;
        maxSoldiers = 15;
        maxScouts = 1;
        maxLumberjacks = 2;
        maxTanks = 2;

        isFirstTurn = true;
        underAttack = false;
    }

    @Override
    public void onUpdate() {
        while (true) {
            try {
                int roundNumber = robotController.getRoundNum();
                if(roundNumber == 1) {
                    setInitialArchonLocations();
                    broadcastAntenna.resetMark();
                }
                checkDonateWin();

                sensorArray.reset();
                navigationSystem.dodgeBullets();

                if(roundNumber % 2 == 0) {
                    broadcastAntenna.resetBroadcasts();
                }
                else {
                    setBuildNumbers();
                    tryHireGardener();
                }
                checkIfUnderAttack();

                //printBytecodeUsage();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    //TODO move to sensor array?
    private void checkIfUnderAttack() throws GameActionException {
        if(sensorArray.surroundingEnemyRobots.size() > 0) {
            underAttack = true;
            broadcastAntenna.addCallForHelp(robotType, sensorArray.currentLocation.x, sensorArray.currentLocation.y);
        } else {
            underAttack = false;
        }
    }

    private void tryHireGardener() throws GameActionException {
        List<Integer> gardeners = broadcastAntenna.getGardeners();
        int unsettledGardenerCount = 0;
        int settledGardenerCount = 0;

        for(Integer gardener : gardeners) {
            if(gardener == 1) {
                unsettledGardenerCount++;
            }
            else if (gardener == 2) {
                settledGardenerCount++;
            }
        }

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
                break;
            }
        }
    }

    //TODO Add logic to dictate numbers based on current situation
    //TODO also figure out robot subtypes
    private void setBuildNumbers() throws GameActionException {
        setScoutBuildNumber();
        setSoldierBuildNumber();
        setLumberjackBuildNumber();
        setTankBuildNumber();
    }

    private void setScoutBuildNumber() throws GameActionException {
        List<Integer> scouts = broadcastAntenna.getScouts();
        Integer scoutsToHire = Math.max(0, maxScouts - scouts.size());

        broadcastAntenna.setHireCount(RobotType.SCOUT, scoutsToHire);
    }

    //TODO differentiate between Guards and Hunters
    private void setSoldierBuildNumber() throws GameActionException {
        maxSoldiers = Math.min( (broadcastAntenna.getGardeners().size() * 4), (broadcastAntenna.getTanks().size() * 10) );

        List<Integer> soldiers = broadcastAntenna.getSoldiers();
        Integer soldiersToHire = Math.max(0, maxSoldiers - soldiers.size());

        broadcastAntenna.setHireCount(RobotType.SOLDIER, soldiersToHire);
    }

    private void setLumberjackBuildNumber() throws GameActionException {
        List<Integer> lumberjacks = broadcastAntenna.getLumberjacks();
        Integer lumberjacksToHire = Math.max(0, maxLumberjacks - lumberjacks.size());

        broadcastAntenna.setHireCount(RobotType.LUMBERJACK, lumberjacksToHire);
    }

    //TODO differentiate between Guards and Hunters
    private void setTankBuildNumber() throws GameActionException {
        List<Integer> tanks = broadcastAntenna.getTanks();
        Integer tanksToHire = Math.max(0, maxTanks - tanks.size());

        broadcastAntenna.setHireCount(RobotType.TANK, tanksToHire);
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

    private void setInitialArchonLocations() throws GameActionException {
        List<MapLocation> locations = Arrays.asList(robotController.getInitialArchonLocations(enemy));
        broadcastAntenna.setArchonLocations(locations);
    }
}
