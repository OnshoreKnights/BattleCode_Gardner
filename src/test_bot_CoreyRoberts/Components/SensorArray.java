package test_bot_CoreyRoberts.Components;
import battlecode.common.*;
import java.util.*;
import scala.Tuple3;

public class SensorArray {
    private RobotController robotController;
    private BroadcastAntenna broadcastAntenna;
    private MapLocation currentLocation;

    private Team myTeam;
    private Team enemyTeam;

    private BodyInfo navigationMark;
    public MapLocation navigationMarkLocation;

    public List<RobotInfo> surroundingFriendlyRobots;
    public List<RobotInfo> surroundingEnemyRobots;
    public List<TreeInfo> surroundingFriendlyTrees;
    public List<TreeInfo> surroundingEnemyTrees;
    public List<TreeInfo> surroundingNeutralTrees;

    public SensorArray(RobotController _robotController, BroadcastAntenna _broadcastAntenna) {
        robotController = _robotController;
        broadcastAntenna = _broadcastAntenna;
        myTeam = robotController.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void reset() {
        currentLocation = robotController.getLocation();

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

    //Target help channel (switch to guards only)
    //If there are no robots under attack, select priority nearby robot
    //If there are no nearby robots, pull the broadcast Mark
    //If there are no broadcast marks
    public void selectMarkEnemyRobot() throws GameActionException{
        navigationMark = null;
        navigationMarkLocation = null;

        Tuple3<RobotType, Float, Float> helpRobot = broadcastAntenna.getCallForHelp();
        if(helpRobot != null) {
            navigationMarkLocation = new MapLocation(helpRobot._2(), helpRobot._3());
        }

        if(navigationMarkLocation == null) {
            for (RobotInfo robotInfo : surroundingEnemyRobots) {
                if (navigationMark == null || getPriority(robotInfo.type) > getPriority(((RobotInfo) navigationMark).type)) {
                    navigationMark = robotInfo;
                    navigationMarkLocation = robotInfo.location;
                }
            }
        }

        if(navigationMarkLocation == null) {
            Tuple3<RobotType, Float, Float> mark = broadcastAntenna.getMark();
            if(mark != null) {
                navigationMarkLocation = new MapLocation(mark._2(), mark._3());
            }
        }

        if(navigationMarkLocation == null){
            List<MapLocation> archons = broadcastAntenna.getArchonLocations();
            if(archons.size() > 0) {
                navigationMarkLocation = archons.get(0);
            }
        }
    }

    public RobotInfo targetRobot() {
        RobotInfo currentTarget = null;
        int currentTargetPriority = 0;

        for (RobotInfo robot : surroundingEnemyRobots) {
            int robotPriority = getPriority(robot.type);
            if (currentTarget == null || robotPriority > currentTargetPriority) {
                currentTarget = robot;
                currentTargetPriority = robotPriority;
            }
        }
        return currentTarget;
    }

    public TreeInfo targetTree() {
        TreeInfo currentTarget = null;
        float currentTargetDistance = 0;

        for (TreeInfo tree : surroundingEnemyTrees) {
            float treeDistance = currentLocation.distanceTo(tree.location);
            if(currentTarget == null || treeDistance < currentTargetDistance) {
                currentTarget = tree;
                currentTargetDistance = treeDistance;
            }
        }
        return currentTarget;
    }

    //If within range of a known archon location, scan for archons
    //If an archon is not seen but should be, remove it from the list. It is probably dead.
    public void confirmArchonLocations() throws GameActionException {
        for(RobotInfo robot : surroundingEnemyRobots) {
            if(robot.type == RobotType.ARCHON) {
                return;
            }
        }

        List<MapLocation> archons = broadcastAntenna.getArchonLocations();
        List<MapLocation> currentArchons = new ArrayList<>();
        for(MapLocation archon : archons) {
            if(!robotController.canSenseLocation(archon)) {
                currentArchons.add(archon);
            }
        }
        if(archons.size() != currentArchons.size()) {
            broadcastAntenna.setArchonLocations(currentArchons);
        }
    }

    public void confirmMark() throws GameActionException {
        Tuple3<RobotType, Float, Float> mark = broadcastAntenna.getMark();

        if(mark != null
                && robotController.canSenseLocation(new MapLocation(mark._2(), mark._3()))
                && surroundingEnemyRobots.size() == 0) {
            broadcastAntenna.resetMark();
        }
    }

    public void updateMark(RobotInfo robotInfo) throws GameActionException {
        if (robotInfo == null) {
            return;
        }

        Tuple3<RobotType, Float, Float> mark = broadcastAntenna.getMark();
        if(mark == null || getPriority(robotInfo.type) > getPriority(mark._1())) {
            broadcastAntenna.addMark(
                    robotInfo.type,
                    robotInfo.location.x,
                    robotInfo.location.y);
        }
    }

    //Converts robotType into priority number
    //TODO move to utility component
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
