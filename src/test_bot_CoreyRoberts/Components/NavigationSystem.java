package test_bot_CoreyRoberts.Components;

import battlecode.common.*;

import java.util.Random;

public class NavigationSystem {
    private RobotController robotController;
    private MapLocation currentLocation;
    private Random random;

    public NavigationSystem (RobotController _robotController) {
        robotController = _robotController;
        currentLocation = robotController.getLocation();
        random = new Random();
    }

    private Direction randomDirection() {
        return new Direction(random.nextFloat() * 2 * (float)Math.PI);
    }

    //TODO actually detect and avoid bullets
    //wiggle about randomly, mostly to avoid bullets.  Update later.
    public void dodgeBullets() throws GameActionException {
        Direction direction = randomDirection();
        tryMove(direction);
    }

    //TODO default to something else instead of random
    //This will probably be built into the various robot subtypes
    public boolean tryMove(MapLocation location) throws GameActionException {
        currentLocation = robotController.getLocation();
        if(location == null) {
            return tryMove(randomDirection());
        }
        return tryMove(currentLocation.directionTo(location));
    }

    public boolean tryMove(Direction direction) throws GameActionException {
        if(direction == null) {
            direction = randomDirection();
        }
        return tryMove(direction,20,3);
    }

    //TODO replace with pathfinder algorithm
    public boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        if(robotController.hasMoved()) {
            return false;
        }

        // First, try intended direction
        if (robotController.canMove(dir)) {
            robotController.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(robotController.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                robotController.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(robotController.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                robotController.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        return false;
    }
}
