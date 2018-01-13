import bc.MapLocation;

import java.util.ArrayList;

public class InitialWorkerAction extends Action {
    public InitialWorkerAction(GameManager gm, Robo robo) {
        initialize(gm);
        getUnits().add(robo);
    }

    public ActionStatus execute() {
        ArrayList<Robo> toBeRemoved = new ArrayList<>();
        for (Robo robot: getUnits())
            if (robot.getRoundLastUpdated() != getManager().controller().round())
                toBeRemoved.add(robot);
        for (Robo robot : toBeRemoved)
            getUnits().remove(robot);

        if (getUnits().size() == 0)
            return new ActionStatus(false, true);

        if (canReplicate() && shouldReplicate()) {
            replicate();
        }
        else if (isNearbyBlueprint()) {
            buildNearbyBlueprint();
        }
        else if (canBuildFactory() && shouldBuildFactory()) {
            buildFactory();
        }
        else if (canBuildRocket() && shouldBuildRocket()) {
            buildRocket();
        }
        else if (isNearbyKarboniteDeposit()) {
            harvestNearbyKarboniteDeposit();
        }
        else {
            moveRandomDirection();
        }

        if (getManager().controller().round() >= 1000)
            return new ActionStatus(true, true);
        return new ActionStatus();
    }

    public MapLocation checkForKryptoniteDeposit() {
        return null;
    }
}
