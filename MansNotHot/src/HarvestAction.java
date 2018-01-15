import bc.Direction;
import bc.MapLocation;

public class HarvestAction extends Action {
    Robo singleUnit;
    private MapLocation karboniteLocation;
    public HarvestAction(GameManager manager, Robo worker, MapLocation karboniteLocation) {
        initialize(manager);
        addRobo(worker);
        singleUnit = worker;
        this.karboniteLocation = karboniteLocation;
    }

    @Override
    public ActionStatus execute() {
        if (getUnits().size() != 1 || getManager().controller().round() - getRoundCreated() > 34 || !(getUnits().first().equals(singleUnit)))
            return new ActionStatus(false, true);
        Robo worker = singleUnit;
        if (worker.getLoc().mapLocation().distanceSquaredTo(karboniteLocation) > 2)
            throw new IllegalArgumentException("Worker not positioned close enough to karbonite");
        if (getManager().controller().karboniteAt(karboniteLocation) == 0)
            return new ActionStatus(true, true);

        Direction direction = worker.getLoc().mapLocation().directionTo(karboniteLocation);
        if (getManager().controller().canHarvest(worker.getUnitId(), direction))
            getManager().controller().harvest(worker.getUnitId(), direction);

        if (getManager().controller().karboniteAt(karboniteLocation) == 0)
            return new ActionStatus(true, true);
        return new ActionStatus();
    }
}
