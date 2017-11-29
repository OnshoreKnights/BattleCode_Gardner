package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

public class Lumberjack extends Robot {
    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;
    private WeaponSystem weaponSystem;

    public Lumberjack() {
        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController, broadcastAntenna);
        navigationSystem = new NavigationSystem(robotController);
        weaponSystem = new WeaponSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                broadcastAntenna.addLumberjack(1);

                sensorArray.confirmArchonLocations();
                sensorArray.confirmMark();

                navigationSystem.tryMove(selectMark());

                weaponSystem.tryChop(sensorArray.targetTreeToChop());
                RobotInfo targetRobot = sensorArray.targetRobot();
                weaponSystem.tryAttackTarget(targetRobot);
                sensorArray.updateMark(targetRobot);

                //printBytecodeUsage();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Tank Exception");
                e.printStackTrace();
            }
        }
    }

    private MapLocation selectMark() throws GameActionException {
        if (!sensorArray.selectMarkRobotTree()) {
            if (!sensorArray.selectMarkFromBroadcast()) {
                sensorArray.selectMarkFromArchons();
            }
        }
        return sensorArray.navigationMarkLocation;
    }
}
