import bc.Direction;
import bc.MapLocation;

public class MoveAction extends Action {
    private StaticPath path;
    private MapLocation destination;
    private Robo singleUnit;

    public MoveAction(GameManager manager, Robo robo, MapLocation destination) {
        initialize(manager);
        addRobo(robo);
        singleUnit = robo;
        this.destination = destination;
        path = getManager().getMapAnalyzer().getStaticPath(singleUnit.getLoc().mapLocation(), destination);
        int pathLen = 0;
        for (StaticPath s = path; path != null; s = s.getNext())
            ++pathLen;

        System.out.format("MoveAction (%d, %d) -> (%d, %d) with pathLen %d\n",
                singleUnit.getLoc().mapLocation().getX(), singleUnit.getLoc().mapLocation().getY(),
                destination.getX(), destination.getY(), pathLen);
    }

    @Override
    public ActionStatus execute() {
        if (getUnits().size() != 1 || getManager().controller().round() - getRoundCreated() > 100 || !(getUnits().first().equals(singleUnit)))
            return new ActionStatus(false, true);
        if (path == null) {
            return new ActionStatus(true, true);
        }
        if (getManager().controller().isMoveReady(singleUnit.getUnitId())) {
            MapLocation start = singleUnit.getLoc().mapLocation();
            Direction direction = start.directionTo(path.getLoc());
            if (getManager().controller().canMove(singleUnit.getUnitId(), direction)) {
                getManager().controller().moveRobot(singleUnit.getUnitId(), direction);
            }
            else {
                if (start.distanceSquaredTo(destination) > 32) {
                    StaticPath other = path.getNext().getNext().getNext();
                    path = getManager().getMapAnalyzer().getStaticPath(singleUnit.getLoc().mapLocation(), other.getLoc());
                    StaticPath element = path;
                    while (element.getNext() != null)
                        element = element.getNext();
                    element.setNext(other.getNext());
                }
                else {
                    path = getManager().getMapAnalyzer().getStaticPath(singleUnit.getLoc().mapLocation(), destination);
                }
                direction = start.directionTo(path.getLoc());
                if (getManager().controller().canMove(singleUnit.getUnitId(), direction)) {
                    getManager().controller().moveRobot(singleUnit.getUnitId(), direction);
                }
            }
            if (start.distanceSquaredTo(destination) <= 2)
                return new ActionStatus(true, true);
            if (start.add(direction).getX() == destination.getX() && start.add(direction).getY() == destination.getY())
                return new ActionStatus(true, true);
        }
        return new ActionStatus();
    }

    @Override
    public String toString() {
        return String.format("MoveAction unit %d to (%d, %d) with distance-squared %d", singleUnit.getUnitId(), destination.getX(), destination.getY(), singleUnit.getLoc().mapLocation().distanceSquaredTo(destination));
    }
}
