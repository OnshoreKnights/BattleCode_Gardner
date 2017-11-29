package test_bot_CoreyRoberts;

import battlecode.common.*;

import java.util.*;

public abstract class Robot {
    static RobotController robotController = null;
    static RobotType robotType = null;
    static Random random;
    static Team myTeam;
    static Team enemy;

    public static void init(RobotController robotController) throws GameActionException {
        Robot.robotController = robotController;
        random = new Random();
        robotType = robotController.getType();
        myTeam = robotController.getTeam();
        enemy = myTeam.opponent();
    }

    protected static Direction randomDirection() {
        return new Direction(random.nextFloat() * 2 * (float)Math.PI);
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    //TODO move to navigationSystem
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = robotController.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= robotController.getType().bodyRadius);
    }

    //TODO move to navigation system
    public static boolean isAwayFromMapEdge(MapLocation location, float distance) throws GameActionException {
        for(int i = 0; i < 4; i++) {
            MapLocation testLocation = location.add(1.5708f * i, distance);
            if(!robotController.onTheMap(testLocation)) {
                return false;
            }
        }
        return true;
    }

    //Utility system?
    static void tryShakeTree() throws GameActionException {
        if(!robotController.canShake()) {
            return;
        }
        TreeInfo[] trees = robotController.senseNearbyTrees(1f);

        for(TreeInfo tree : trees) {
            int treeId = tree.getID();
            if(tree.containedBullets > 0 && robotController.canShake(treeId)) {
                robotController.shake(treeId);
                return;
            }
        }
    }

    public void printBytecodeUsage() {
        int bytecodeUsed = Clock.getBytecodeNum();
        int bytecodeLeft = Clock.getBytecodesLeft();
        System.out.println("Bytecode Check    (" + bytecodeUsed + ") used    (" + bytecodeLeft + ") remaining");
    }

    abstract void onUpdate() throws GameActionException;
}
