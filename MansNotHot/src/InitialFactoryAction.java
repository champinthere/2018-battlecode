import bc.*;

import java.util.ArrayList;
import java.util.List;

public class InitialFactoryAction extends Action {

    public InitialFactoryAction(GameManager gm, Robo robo) {
        if (robo.getType() != UnitType.Factory)
            throw new IllegalArgumentException("InitialFactoryAction requires factory");
        initialize(gm);
        addRobo(robo);
    }

    @Override
    public ActionStatus execute() {
        if (getUnits().size() == 0)
            return new ActionStatus(false, true);

        Robo factory = getUnits().first();
        GameController gc = getManager().controller();
        if (factory.isBlueprint())
            return new ActionStatus();
        if (gc.canProduceRobot(factory.getUnitId(), UnitType.Ranger)) {
            gc.produceRobot(factory.getUnitId(), UnitType.Ranger);
        }
        VecUnitID garrison = gc.unit(factory.getUnitId()).structureGarrison();
        List<Direction> dirs = availableSpaces();
        int gi = 0, di = 0;
        while (gi < garrison.size() && di < dirs.size()) {
            gc.unload(factory.getUnitId(), dirs.get(di));
            ++gi;
            ++di;
        }
        return new ActionStatus();
    }

    public List<Direction> availableSpaces() {
        Robo factory = getUnits().first();
        MapLocation floc = factory.getLoc().mapLocation();
        ArrayList<Direction> result = new ArrayList<>();
        VecMapLocation nearby = getManager().controller().allLocationsWithin(floc, 2);
        for (int i = 0; i < nearby.size(); ++i) {
            MapLocation m = nearby.get(i);
            if (getManager().getMapAnalyzer().isPassable(m))
                result.add(floc.directionTo(m));
        }
        return result;
    }
}
