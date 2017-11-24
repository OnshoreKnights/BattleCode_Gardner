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
        sensorArray = new SensorArray(robotController);
        navigationSystem = new NavigationSystem(robotController);
        weaponSystem = new WeaponSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                broadcastAntenna.incrementScouts();

                sensorArray.checkArchonLocation();
                sensorArray.selectMarkBulletTree();
                if(sensorArray.navigationMarkLocation == null) {
                    sensorArray.selectMarkEnemyRobot();
                }
                navigationSystem.tryMove(sensorArray.navigationMarkLocation);

                tryShakeTree();
                weaponSystem.tryAttackTarget(sensorArray.targetRobot());
                weaponSystem.tryAttackTarget(sensorArray.targetTree());

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Scout Exception");
                e.printStackTrace();
            }
        }
    }
}
