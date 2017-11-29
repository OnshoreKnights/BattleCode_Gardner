package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

public class Scout extends Robot {
    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem navigationSystem;
    private WeaponSystem weaponSystem;

    public Scout() {
        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController, broadcastAntenna);
        navigationSystem = new NavigationSystem(robotController);
        weaponSystem = new WeaponSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                broadcastAntenna.addScout(1);

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
                System.out.println("A Scout Exception");
                e.printStackTrace();
            }
        }
    }


    private MapLocation selectMark() throws GameActionException {
        if(!sensorArray.selectMarkBulletTree()) {
            if (!sensorArray.selectMarkCallForHelp()) {
                if (!sensorArray.selectMarkFromBroadcast()) {
                    sensorArray.selectMarkFromArchons();
                }
            }
        }
        return sensorArray.navigationMarkLocation;
    }
}
