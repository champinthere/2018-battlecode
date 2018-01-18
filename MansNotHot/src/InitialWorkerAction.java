import bc.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class InitialWorkerAction extends Action {
    private static int[][] harvestClaimed;
    private static int[][] numHarvesters;
    private static TreeMap<Robo, Integer> numBuilders;

    public InitialWorkerAction(GameManager gm, Robo robo) {
        if (numBuilders == null)
            numBuilders = new TreeMap<>();
        if (harvestClaimed == null) {
            harvestClaimed = new int[gm.getMapAnalyzer().getWidth()][gm.getMapAnalyzer().getHeight()];
            numHarvesters = new int[gm.getMapAnalyzer().getWidth()][gm.getMapAnalyzer().getHeight()];
        }
        for (int i = 0; i < harvestClaimed.length; ++i)
            for (int j = 0; j < harvestClaimed[0].length; ++j)
                harvestClaimed[i][j] = Integer.MIN_VALUE;
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
        Robo blueprint = nearbyBlueprint(worker);
//        System.out.println("=============================: " + getManager().getLedger().getBlueprints().size());
        if (shouldReplicate(worker) && canReplicate(worker)) {
            replicate(worker);
        }
        else if (blueprint != null) {
            buildBlueprint(worker, blueprint);
            return new ActionStatus(true, false, true);
        }
        else if (canBlueprintFactory(worker) && shouldBlueprintFactory(worker)) {
            blueprintFactory(worker);
            return new ActionStatus(true, false, true);
        }
//        else if (canBuildRocket(worker) && shouldBuildRocket(worker)) {
//            buildRocket(worker);
//        }
        else if (isNearbyKarboniteDeposit(worker)) {
            harvestNearbyKarboniteDeposit(worker);
            return new ActionStatus(true, false, true);
        }
        else {
            moveRandomDirection(worker);
        }

        if (getManager().controller().round() > 1000)
            return new ActionStatus(true, true);
        return new ActionStatus();
    }

    public Robo nearbyBlueprint(Robo worker) {
        Robo result = null;
        long dsq = Integer.MAX_VALUE;
        for (Robo blueprint: getManager().getLedger().getBlueprints()) {
            long testsq = worker.getLoc().mapLocation().distanceSquaredTo(blueprint.getLoc().mapLocation());
            if (numBuilders.containsKey(blueprint) && numBuilders.get(blueprint) >= 40)
                System.out.println("FULL $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            if (testsq <= 200 &&
                    (!numBuilders.containsKey(blueprint) || numBuilders.get(blueprint) < 40)) {
                if (testsq < dsq) {
                    result = blueprint;
                }
            }
        }
        return result;
    }

    public void buildBlueprint(Robo worker, Robo blueprint) {
        //todo
        if (!numBuilders.containsKey(blueprint))
            numBuilders.put(blueprint, 0);
        numBuilders.put(blueprint, numBuilders.get(blueprint) + 1);

        Action workerAction = this;
        MapLocation workerLocation = worker.getLoc().mapLocation();
        MapLocation blueprintLocation = blueprint.getLoc().mapLocation();
        MoveAction mover = new MoveAction(getManager(), worker, blueprintLocation);
        BuildBlueprintAction builder = new BuildBlueprintAction(getManager(), worker, blueprint);
        if (workerLocation.distanceSquaredTo(blueprintLocation) > 2) {
            mover.addRobo(worker);
            getManager().queueAction(mover);
            mover.setUponTermination(() -> {
                builder.addRobo(worker);
                getManager().queueAction(builder);
                builder.setUponTermination(() -> {
                    workerAction.addRobo(worker);
                    getManager().queueAction(workerAction);
                });
            });
        }
        else {
            builder.addRobo(worker);
            getManager().queueAction(builder);
            builder.setUponTermination(() -> {
                workerAction.addRobo(worker);
                getManager().queueAction(workerAction);
            });
        }
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
        return getManager().getLedger().getNumWorkers() < 8;
    }

    public boolean canBlueprintFactory(Robo worker) {
        MapLocation workerLocation = worker.getLoc().mapLocation();
        VecMapLocation nearby = getManager().controller().allLocationsWithin(workerLocation, 2);
        MapLocation choice = null;
        Direction toChoice = Direction.Center;
        GameController gc = getManager().controller();
        for (int i = 0; i < nearby.size(); ++i) {
            MapLocation m = nearby.get(i);
            if (getManager().getMapAnalyzer().isPassable(m)) {
                choice = m;
                toChoice = workerLocation.directionTo(choice);
                break;
            }
        }
        if (choice == null) return false;
        return gc.canBlueprint(worker.getUnitId(), UnitType.Factory, toChoice);
    }

    public Robo blueprintFactory(Robo worker) {
        MapLocation workerLocation = worker.getLoc().mapLocation();
        VecMapLocation nearby = getManager().controller().allLocationsWithin(workerLocation, 2);
        MapLocation choice = null;
        Direction toChoice = Direction.Center;
        GameController gc = getManager().controller();
        for (int i = 0; i < nearby.size(); ++i) {
            MapLocation m = nearby.get(i);
            if (getManager().getMapAnalyzer().isPassable(m)) {
                choice = m;
                toChoice = workerLocation.directionTo(choice);
                break;
            }
        }
        gc.blueprint(worker.getUnitId(), UnitType.Factory, toChoice);
        Robo blueprint = getManager().addRobo(gc.senseUnitAtLocation(choice).id());
        Action workerAction = this;
        MapLocation blueprintLocation = blueprint.getLoc().mapLocation();
        BuildBlueprintAction builder = new BuildBlueprintAction(getManager(), worker, blueprint);
        builder.addRobo(worker);
        getManager().queueAction(builder);
        builder.setUponTermination(() -> {
            System.out.println("FACTORY BUILT!!!");
            workerAction.addRobo(worker);
            getManager().queueAction(workerAction);
        });
        return blueprint;
    }

    public boolean shouldBlueprintFactory(Robo worker) {
        return getManager().getLedger().getNumFactories() + getManager().getLedger().getNumFactoryBlueprints() < 4;
    }

    public boolean isNearbyKarboniteDeposit(Robo worker) {
        long nearbyRadius = 201l;
        MapLocation workerLocation = worker.getLoc().mapLocation();
        GameController gc = getManager().controller();
        VecMapLocation nearbyLocations = gc.allLocationsWithin(workerLocation, nearbyRadius);
        for (int i = 0; i < nearbyLocations.size(); ++i) {
            MapLocation m = nearbyLocations.get(i);
            try {
                if (getManager().controller().karboniteAt(m) > 0 &&
                        ((gc.round() - harvestClaimed[m.getX()][m.getY()]) > 18 || numHarvesters[m.getX()][m.getY()] < 2))
                    return true;
            }
            catch (Exception e) {}
        }
        return false;
    }

    public void harvestNearbyKarboniteDeposit(Robo worker) {
        long nearbyRadius = 201l;
        MapLocation workerLocation = worker.getLoc().mapLocation();
        MapLocation karboniteLocation = null;
        long dsquared = Integer.MAX_VALUE;
        GameController gc = getManager().controller();
        VecMapLocation nearbyLocations = gc.allLocationsWithin(workerLocation, nearbyRadius);
        for (int i = 0; i < nearbyLocations.size(); ++i) {
            MapLocation m = nearbyLocations.get(i);
            try {
                if (getManager().controller().karboniteAt(m) > 0
                        && ((gc.round() - harvestClaimed[m.getX()][m.getY()]) > 18 || numHarvesters[m.getX()][m.getY()] < 2)) {
                    if (workerLocation.distanceSquaredTo(m) < dsquared) {
                        karboniteLocation = m;
                        dsquared = workerLocation.distanceSquaredTo(m);
                    }
                }
            }
            catch (Exception e) {}
        }
        Action workerAction = this;
        MoveAction mover = new MoveAction(getManager(), worker, karboniteLocation);
        HarvestAction harvester = new HarvestAction(getManager(), worker, karboniteLocation);
        harvestClaimed[karboniteLocation.getX()][karboniteLocation.getY()] = (int) gc.round();
        numHarvesters[karboniteLocation.getX()][karboniteLocation.getY()] = (numHarvesters[karboniteLocation.getX()][karboniteLocation.getY()] < 2 ? numHarvesters[karboniteLocation.getX()][karboniteLocation.getY()] + 1 : 1);
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
