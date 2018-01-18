import bc.GameController;
import bc.MapLocation;

public class BuildBlueprintAction extends Action {
    private Robo singleUnit;
    private MapLocation blueprintLocation;
    private Robo blueprint;
    public BuildBlueprintAction(GameManager manager, Robo worker, Robo blueprint) {
        initialize(manager);
        addRobo(worker);
        singleUnit = worker;
        this.blueprint = blueprint;
        this.blueprintLocation = blueprint.getLoc().mapLocation();
        System.out.println("NEW BUILD_BLUEPRINT_ACTION");
    }

    @Override
    public ActionStatus execute() {
        if (getUnits().size() != 1 || !(getUnits().first().equals(singleUnit)))
            return new ActionStatus(false, true);
        Robo worker = singleUnit;
        if (worker.getLoc().mapLocation().distanceSquaredTo(blueprintLocation) > 2)
            return new ActionStatus(false, true);
        if (!blueprint.isBlueprint())
            return new ActionStatus(true, true);
        // note: wasted turn

        GameController gc = getManager().controller();
        if (gc.canBuild(worker.getUnitId(), blueprint.getUnitId()))
            gc.build(worker.getUnitId(), blueprint.getUnitId());

        return new ActionStatus();
    }

    @Override
    public void terminate() {
        super.terminate();
        System.out.println("BLUEPRINT ACTION TERMINATION");
    }
}
