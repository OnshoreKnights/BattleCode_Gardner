package test_bot_CoreyRoberts.Components;
import battlecode.common.*;

public class WeaponSystem {
    private RobotController robotController;
    private RobotType robotType;

    public WeaponSystem(RobotController _robotController) {
        robotController = _robotController;
        robotType = robotController.getType();
    }

    public void tryChop(TreeInfo treeInfo) throws GameActionException {
        if(treeInfo == null) {
            return;
        }
        int treeId = treeInfo.getID();
        if(robotController.canChop(treeId)) {
            robotController.chop(treeId);
        }
    }

    public void tryAttackTarget(BodyInfo target) throws GameActionException{
        if(target == null) {
            return;
        }
        tryAttackTarget(target.getLocation(), target.getRadius());
    }

    public void tryAttackTarget(MapLocation location, float targetRadius) throws GameActionException {
        if(location == null) {
            return;
        }
        MapLocation currentLocation = robotController.getLocation();
        Direction direction = currentLocation.directionTo(location);

        if(robotType == RobotType.SOLDIER || robotType == RobotType.TANK) {
            float distance = currentLocation.distanceTo(location);
            tryPentadAttack(distance, direction, targetRadius);
            tryTriadAttack(distance,direction, targetRadius);
        }
        if(robotType == RobotType.SOLDIER || robotType == RobotType.TANK || robotType == robotType.SCOUT) {
            trySingleAttack(direction);
        }
        if(robotType == RobotType.LUMBERJACK) {
            //TODO move to sensor?
            int enemyRobotCount = robotController.senseNearbyRobots(1, robotController.getTeam().opponent()).length;
            int friendlyRobotCount = robotController.senseNearbyRobots(1, robotController.getTeam()).length;
            if(robotController.canStrike() && enemyRobotCount > friendlyRobotCount) {
                robotController.strike();
            }
        }
    }

    private void trySingleAttack(Direction direction) throws GameActionException {
        if(robotController.canFireSingleShot()) {
            robotController.fireSingleShot(direction);
        }
    }

    private void tryTriadAttack(float distance, Direction direction, float targetRadius) throws GameActionException {
        float triadMaxDistanceSmallTarget = 2.747f;
        float triadMaxDistanceLargeTarget = 5.495f;

        if (!robotController.canFireTriadShot()) {
            return;
        }

        if(targetRadius == 2 && distance <= triadMaxDistanceLargeTarget) { //Archon and tank
            robotController.fireTriadShot(direction);
        }
        if(targetRadius == 1 && distance <= triadMaxDistanceSmallTarget) { //All other targets
            robotController.fireTriadShot(direction);
        }
    }

    private void tryPentadAttack(float distance, Direction direction, float targetRadius) throws GameActionException {
        float pentadMaxDistanceSmallTarget = 1.303f;
        float pentadMaxDistanceLargeTarget = 2.606f;

        if (!robotController.canFirePentadShot()) {
            return;
        }

        if(targetRadius == 2 && distance <= pentadMaxDistanceLargeTarget) { //Archon and tank
            robotController.firePentadShot(direction);
        }
        if(targetRadius == 1 && distance <= pentadMaxDistanceSmallTarget) { //All other targets
            robotController.firePentadShot(direction);
        }
    }
}
