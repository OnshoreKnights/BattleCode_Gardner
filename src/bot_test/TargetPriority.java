package bot_test;
import battlecode.common.*;

public class TargetPriority {
    private static int tree = 0;
    private static int gardener = 1;
    private static int scout = 2;
    private static int lumberjack = 3;
    private static int tank = 4;
    private static int soldier = 5;
    private static int archon = 6;

    public static int GetPriority(RobotType type) {
        int priority;
        switch(type) {
            case ARCHON:
                priority = archon;
                break;
            case SOLDIER:
                priority = soldier;
                break;
            case TANK:
                priority = tank;
                break;
            case LUMBERJACK:
                priority = lumberjack;
                break;
            case SCOUT:
                priority = scout;
                break;
            case GARDENER :
                priority = gardener;
                break;
            default:
                priority = tree;
        }

        return priority;
    }
}
