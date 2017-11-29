package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

public class Soldier extends Robot {
    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;
    private WeaponSystem weaponSystem;

    private static class SoldierType {
        private static int Guard = 1;
        private static int Hunter = 2;
    }

    public Soldier() {
        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController, broadcastAntenna);
        navigationSystem = new NavigationSystem(robotController);
        weaponSystem = new WeaponSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                broadcastAntenna.addSoldier(SoldierType.Hunter);

                sensorArray.confirmArchonLocations();
                sensorArray.confirmMark();

                navigationSystem.tryMove(selectMark());

                tryShakeTree();
                RobotInfo targetRobot = sensorArray.targetRobot();
                weaponSystem.tryAttackTarget(targetRobot);
                sensorArray.updateMark(targetRobot);
                weaponSystem.tryAttackTarget(sensorArray.targetEnemyTree());

                //printBytecodeUsage();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    private MapLocation selectMark() throws GameActionException {
        if (!sensorArray.selectMarkCallForHelp()) {
            if (!sensorArray.selectMarkFromBroadcast()) {
                sensorArray.selectMarkFromArchons();
            }
        }
        return sensorArray.navigationMarkLocation;
    }
}
