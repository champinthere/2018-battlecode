import bc.Direction;
import bc.MapLocation;
import bc.UnitType;
import bc.VecMapLocation;

import java.util.ArrayList;

public class InitialWorkerAction extends Action {
    public InitialWorkerAction(GameManager gm, Robo robo) {
        if (robo.getType() != UnitType.Worker)
            throw new IllegalArgumentException("InitialWorkerAction requires worker");
        initialize(gm);
        addRobo(robo);
    }

    public ActionStatus execute() {
//        System.out.println("InitialWorkerAction Executing");
        if (getUnits().size() == 0)
            return new ActionStatus(false, true);

        Robo worker = getUnits().first();

        if (shouldReplicate(worker) && canReplicate(worker)) {
            replicate(worker);
        }
//        else if (isNearbyBlueprint(worker)) {
//            buildNearbyBlueprint(worker);
//        }
//        else if (canBuildFactory(worker) && shouldBuildFactory(worker)) {
//            buildFactory(worker);
//        }
//        else if (canBuildRocket(worker) && shouldBuildRocket(worker)) {
//            buildRocket(worker);
//        }
//        else if (isNearbyKarboniteDeposit(worker)) {
//            harvestNearbyKarboniteDeposit(worker);
//            return new ActionStatus(true, false, true);
//        }
        else {
            moveRandomDirection(worker);
        }

        if (getManager().controller().round() > 1000)
            return new ActionStatus(true, true);
        return new ActionStatus();
    }

    public MapLocation checkForKryptoniteDeposit() {
        return null;
    }

    public boolean canReplicate(Robo worker) {
        MapLocation workerLocation = worker.getLoc().mapLocation();
        VecMapLocation nearby = getManager().controller().allLocationsWithin(workerLocation, 2);
        MapLocation choice = null;
        Direction toChoice = Direction.Center;
        for (int i = 0; i < nearby.size(); ++i) {
            MapLocation m = nearby.get(i);
            if (getManager().getMapAnalyzer().isPassable(m)) {
                choice = m;
                toChoice = workerLocation.directionTo(choice);
                break;
            }
        }
        return getManager().controller().canReplicate(worker.getUnitId(), toChoice);
    }

    public void replicate(Robo worker) {
        MapLocation workerLocation = worker.getLoc().mapLocation();
        VecMapLocation nearby = getManager().controller().allLocationsWithin(workerLocation, 2);
        MapLocation choice = null;
        Direction toChoice = Direction.Center;
        for (int i = 0; i < nearby.size(); ++i) {
            MapLocation m = nearby.get(i);
            if (getManager().getMapAnalyzer().isPassable(m)) {
                choice = m;
                toChoice = workerLocation.directionTo(choice);
                getManager().controller().replicate(worker.getUnitId(), toChoice);
                getManager().getLedger().updateWithRobo(worker);
                return;
            }
        }
    }

    public boolean shouldReplicate(Robo worker) {
        return getManager().getLedger().getNumWorkers() < 24;
    }

    public boolean isNearbyKarboniteDeposit(Robo worker) {
        long nearbyRadius = 200l;
        MapLocation workerLocation = worker.getLoc().mapLocation();
        VecMapLocation nearbyLocations = getManager().controller().allLocationsWithin(workerLocation, nearbyRadius);
        for (int i = 0; i < nearbyLocations.size(); ++i) {
            MapLocation m = nearbyLocations.get(i);
            try {
                if (getManager().controller().karboniteAt(m) > 0)
                    return true;
            }
            catch (Exception e) {};
        }
        return false;
    }

    public void harvestNearbyKarboniteDeposit(Robo worker) {
        long nearbyRadius = 200l;
        MapLocation workerLocation = worker.getLoc().mapLocation();
        MapLocation karboniteLocation = null;
        VecMapLocation nearbyLocations = getManager().controller().allLocationsWithin(workerLocation, nearbyRadius);
        for (int i = 0; i < nearbyLocations.size(); ++i) {
            MapLocation m = nearbyLocations.get(i);
            try {
                if (getManager().controller().karboniteAt(m) > 0) {
                    karboniteLocation = m;
                    break;
                }
            }
            catch (Exception e) {}
        }
        Action workerAction = this;
        MoveAction mover = new MoveAction(getManager(), worker, karboniteLocation);
        HarvestAction harvester = new HarvestAction(getManager(), worker, karboniteLocation);
        mover.addRobo(worker);
        getManager().queueAction(mover);
        mover.setUponTermination(() -> {
            harvester.addRobo(worker);
            getManager().queueAction(harvester);
            harvester.setUponTermination(() -> {
                workerAction.addRobo(worker);
                getManager().queueAction(workerAction);
            });
        });
    }

    public void moveRandomDirection(Robo worker) {
        MapLocation workerLocation = worker.getLoc().mapLocation();
        if (getManager().controller().isMoveReady(worker.getUnitId())) {
            VecMapLocation nearby = getManager().controller().allLocationsWithin(workerLocation, 2);
            ArrayList<Direction> possibleDirections = new ArrayList<>();
            for (int i = 0; i < nearby.size(); ++i) {
                MapLocation m = nearby.get(i);
                if (getManager().getMapAnalyzer().isPassable(m)) {
                    possibleDirections.add(workerLocation.directionTo(m));
                }
            }
            int randomIndex = (int) (possibleDirections.size() * Math.random());
            Direction randomDirection = possibleDirections.get(randomIndex);
            getManager().getMapAnalyzer().registerMove(workerLocation,
                    workerLocation.add(randomDirection), worker);
            getManager().controller().moveRobot(worker.getUnitId(), randomDirection);
        }
    }
}
