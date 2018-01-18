import bc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InitialRangerAction extends Action {

    public InitialRangerAction(GameManager gm, Robo robo) {
        if (robo.getType() != UnitType.Ranger)
            throw new IllegalArgumentException("InitialFactoryAction requires ranger");
        initialize(gm);
        addRobo(robo);
    }

    @Override
    public ActionStatus execute() {
        if (getUnits().size() == 0)
            return new ActionStatus(false, true);

        Robo ranger = getUnits().first();
        GameController gc = getManager().controller();
        if (!ranger.getLoc().isOnMap())
            return new ActionStatus();

        List<Direction> adj = availableSpaces();
        Direction choice = adj.get((int) (Math.random() * adj.size()));
        if (gc.isMoveReady(ranger.getUnitId()))
            if (gc.canMove(ranger.getUnitId(), choice))
                gc.moveRobot(ranger.getUnitId(), choice);

        if (gc.isAttackReady(ranger.getUnitId())) {
            Unit target = findTarget(ranger);
            if (target != null && gc.canAttack(ranger.getUnitId(), target.id()))
                gc.attack(ranger.getUnitId(), target.id());
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

    public Unit findTarget(Robo ranger) {
        GameController gc = getManager().controller();
        Unit r = gc.unit(ranger.getUnitId());
        MapLocation rloc = ranger.getLoc().mapLocation();
        long rng = r.attackRange();
        long minrng = r.rangerCannotAttackRange();
        long dsq = Integer.MIN_VALUE;
        VecUnit vecTargets = gc.senseNearbyUnitsByTeam(rloc, rng, getManager().getOpposingTeam());
        List<Unit> targets = new ArrayList<Unit>();
        for (int i = 0; i < vecTargets.size(); ++i)
            targets.add(vecTargets.get(i));
        targets = targets.stream()
                .filter(u -> u.location().mapLocation().distanceSquaredTo(rloc) > minrng)
                .collect(Collectors.toList());
        Collections.sort(targets, (a, b) -> (int)
                (1000 * (a.health() - b.health()) +
                100000 * (a.unitType() == UnitType.Ranger ? 1 : 0) +
                (rloc.distanceSquaredTo(a.location().mapLocation())) -
                    (rloc.distanceSquaredTo(b.location().mapLocation()))));
        if (targets.size() > 0)
            return targets.get(0);
        return null;
    }
}
