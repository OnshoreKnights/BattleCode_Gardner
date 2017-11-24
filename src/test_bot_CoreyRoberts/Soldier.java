package test_bot_CoreyRoberts;

import battlecode.common.*;
import test_bot_CoreyRoberts.Components.*;

public class Soldier extends Robot {
    private BroadcastAntenna broadcastAntenna;
    private SensorArray sensorArray;
    private NavigationSystem  navigationSystem;
    private WeaponSystem weaponSystem;

    public Soldier() {
        broadcastAntenna = new BroadcastAntenna(robotController);
        sensorArray = new SensorArray(robotController);
        navigationSystem = new NavigationSystem(robotController);
        weaponSystem = new WeaponSystem(robotController);
    }

    public void onUpdate() {
        while (true) {
            try {
                sensorArray.reset();
                broadcastAntenna.incrementSoldiers();

                sensorArray.checkArchonLocation(); //TODO stop marking archons, and change to just update list of known enemy targets
                sensorArray.selectMarkEnemyRobot();
                navigationSystem.tryMove(sensorArray.navigationMarkLocation);

                weaponSystem.tryAttackTarget(sensorArray.targetRobot());
                weaponSystem.tryAttackTarget(sensorArray.targetTree());
                tryShakeTree(); //Should this stay on robot?  Utility system?

                Clock.yield();
            } catch (Exception e) {
                System.out.println("A Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
