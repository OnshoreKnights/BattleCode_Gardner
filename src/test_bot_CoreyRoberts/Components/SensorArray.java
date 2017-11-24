package test_bot_CoreyRoberts.Components
        ;
import battlecode.common.*;
import java.util.*;

public class SensorArray {
    private RobotController robotController;
    private MapLocation currentLocation;

    private Team myTeam;
    private Team enemyTeam;

    public BodyInfo navigationMark;
    public MapLocation navigationMarkLocation;

    private List<RobotInfo> surroundingFriendlyRobots;
    private List<RobotInfo> surroundingEnemyRobots;
    private List<TreeInfo> surroundingFriendlyTrees;
    private List<TreeInfo> surroundingEnemyTrees;
    private List<TreeInfo> surroundingNeutralTrees;

    public SensorArray(RobotController _robotController) {
        robotController = _robotController;
        myTeam = robotController.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void reset() {
        currentLocation = robotController.getLocation();
        navigationMark = null;
        navigationMarkLocation = null;

        surroundingFriendlyRobots = Arrays.asList(
                Optional.ofNullable(robotController.senseNearbyRobots(-1, myTeam)).orElse(new RobotInfo[0]));
        surroundingEnemyRobots = Arrays.asList(
                Optional.ofNullable(robotController.senseNearbyRobots(-1, enemyTeam)).orElse(new RobotInfo[0]));
        surroundingFriendlyTrees = Arrays.asList(
                Optional.ofNullable(robotController.senseNearbyTrees(-1, myTeam)).orElse(new TreeInfo[0]));
        surroundingEnemyTrees = Arrays.asList(
                Optional.ofNullable(robotController.senseNearbyTrees(-1, enemyTeam)).orElse(new TreeInfo[0]));
        surroundingNeutralTrees = Arrays.asList(
                Optional.ofNullable(robotController.senseNearbyTrees(-1, Team.NEUTRAL)).orElse(new TreeInfo[0]));
    }

    public List<RobotInfo> findNearbyRobotsByType(RobotType type) {
        List<RobotInfo> robotsFound = new ArrayList<>();
        for(RobotInfo robot: surroundingEnemyRobots) {
            if(robot.type == type) {
                robotsFound.add(robot);
            }
        }
        return robotsFound;
    }

    //TODO Search known tree locations if there are none nearby
    public void selectMarkBulletTree() {
        navigationMark = null;
        navigationMarkLocation = null;
        float targetDistance = 0;

        for(TreeInfo tree: surroundingNeutralTrees) {
            if(tree.containedBullets > 0) {
                float distanceToTree = currentLocation.distanceTo(tree.location);
                if (navigationMark == null || distanceToTree < targetDistance) {
                    navigationMark = tree;
                    navigationMarkLocation = tree.location;
                    targetDistance = distanceToTree;
                }
            }
        }
    }

    //TODO save target to broadcast channels to be updated by scouts and other bots.
    public void selectMarkEnemyRobot() throws GameActionException{
        navigationMark = null;
        navigationMarkLocation = null;
        float currentTargetDistance = 0;

        for (RobotInfo robotInfo : surroundingEnemyRobots) {
            float robotDistance = currentLocation.distanceTo(robotInfo.location);
            if (navigationMark == null ||
                    getPriority(robotInfo.type) > getPriority(((RobotInfo) navigationMark).type) ||
                    (robotInfo.type == ((RobotInfo) navigationMark).type && robotDistance < currentTargetDistance)) {
                navigationMark = robotInfo;
                navigationMarkLocation = robotInfo.location;
                currentTargetDistance = robotDistance;
            }
        }

        if(navigationMark == null){
            //TODO remove archon broadcasts whenever they are destroyed (find closest broadcast to their destroyed location)
            if(robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation1x) != 0) {
                navigationMarkLocation = new MapLocation(
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation1x),
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation1y));
            }
            else if(robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation2x) != 0) {
                navigationMarkLocation = new MapLocation(
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation2x),
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation2y));
            }
            else if(robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation3x) != 0) {
                navigationMarkLocation = new MapLocation(
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation3x),
                        robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation3y));
            }
        }
    }

    public RobotInfo targetRobot() {
        RobotInfo currentTarget = null;
        float currentTargetDistance = 0;

        for (RobotInfo robot : surroundingEnemyRobots) {
            float robotDistance = currentLocation.distanceTo(robot.location);
            if (robotDistance < currentTargetDistance) {
                currentTarget = robot;
                currentTargetDistance = robotDistance;
            }
        }
        return currentTarget;
    }

    public TreeInfo targetTree() {
        TreeInfo currentTarget = null;
        float currentTargetDistance = 0;

        for (TreeInfo tree : surroundingEnemyTrees) {
            float treeDistance = currentLocation.distanceTo(tree.location);
            if(treeDistance < currentTargetDistance) {
                currentTarget = tree;
                currentTargetDistance = treeDistance;
            }
        }
        return currentTarget;
    }

    public void checkArchonLocation() throws GameActionException {
        //TODO combine robot checks to save action points.
        //Do not attempt to mark off archons if one is nearby.  Just attack it instead.
        for(RobotInfo robot : surroundingEnemyRobots) {
            if(robot.type == RobotType.ARCHON) {
                return;
            }
        }

        MapLocation archon1 = new MapLocation(
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation1x),
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation1y));
        MapLocation archon2 = new MapLocation(
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation2x),
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation2y));
        MapLocation archon3 = new MapLocation(
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation3x),
                robotController.readBroadcastFloat(BroadcastAntenna.enemyArchonLocation3y));


        if(archon1.x != 0f && robotController.canSenseLocation(archon1)){
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation1x, 0);
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation1y, 0);
        }
        if(archon2.x != 0f && robotController.canSenseLocation(archon2)){
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation2x, 0);
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation2y, 0);
        }
        if(archon3.x != 0f && robotController.canSenseLocation(archon3)){
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation3x, 0);
            robotController.broadcastFloat(BroadcastAntenna.enemyArchonLocation3y, 0);
        }
    }

    //Converts robotType into priorityType
    private int getPriority(RobotType type) {
        int priority;
        switch(type) {
            case ARCHON:
                priority = 7;
                break;
            case SOLDIER:
                priority = 6;
                break;
            case LUMBERJACK:
                priority = 5;
                break;
            case TANK:
                priority = 4;
                break;
            case SCOUT:
                priority = 3;
                break;
            case GARDENER :
                priority = 2;
                break;
            default: //tree
                priority = 1;
        }

        return priority;
    }
}
