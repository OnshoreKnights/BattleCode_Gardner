package test_bot_CoreyRoberts;

import battlecode.common.*;

public strictfp class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController robotController) throws GameActionException {
        Robot robot = initializeRobot(robotController);
        runRobot(robot);
    }

    public static Robot initializeRobot(RobotController robotController) {
        Robot robot = null;

        try {
            Robot.init(robotController);

            switch (robotController.getType()) {
                case ARCHON:
                    robot = new Archon();
                    break;
                case GARDENER:
                    robot = new Gardener();
                    break;
                case SOLDIER:
                    robot = new Soldier();
                    break;
                case SCOUT:
                    robot = new Scout();
                    break;
                case TANK:
                    robot = new Tank();
                    break;
            }
        } catch (Exception e) {
            System.out.println("Exception in robot initialization: " + robotController.getType());
            e.printStackTrace();
        }

        return robot;
    }

    public static void runRobot(Robot robot) {
        while (true) {
            try {
                while (true) {
                    robot.onUpdate();
                }

            } catch (Exception e) {
                System.out.println("Exception in robot update: " + robot.robotType);
                e.printStackTrace();
            }
        }
    }
}