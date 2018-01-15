import bc.Location;
import bc.Unit;
import bc.UnitType;
import bc.VecUnit;

public class Robo implements Comparable<Robo> {
    private GameManager manager;
    private Action handler;
    private int unitId;
    private UnitType type;
    private boolean blueprint = false;

    public boolean isBlueprint() {
        return blueprint;
    }

    public void setBlueprint(boolean blueprint) {
        this.blueprint = blueprint;
    }

    public UnitType getType() {
        return type;
    }

    private long roundLastUpdated;

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    // Needs constant updating
    private Location loc;
    private VecUnit nearbyFriendlyUnits;
    private VecUnit nearbyEnemyUnits;

    public VecUnit getNearbyFriendlyUnits() {
        return nearbyFriendlyUnits;
    }

    public void setNearbyFriendlyUnits(VecUnit nearbyFriendlyUnits) {
        this.nearbyFriendlyUnits = nearbyFriendlyUnits;
    }

    public VecUnit getNearbyEnemyUnits() {
        return nearbyEnemyUnits;
    }

    public void setNearbyEnemyUnits(VecUnit nearbyEnemyUnits) {
        this.nearbyEnemyUnits = nearbyEnemyUnits;
    }

    public Robo(GameManager manager, int unitId) {
        this.manager = manager;
        roundLastUpdated = manager.controller().round();
        this.unitId = unitId;
        manager.getIdMap().put(unitId, this);
        try {
            Unit unit = manager.controller().unit(unitId);
            this.loc = unit.location();
            this.type = unit.unitType();
            if (loc.isOnMap()) {
                this.nearbyFriendlyUnits = manager.controller().senseNearbyUnitsByTeam(loc.mapLocation(), unit.visionRange(), manager.getTeam());
                this.nearbyEnemyUnits = manager.controller().senseNearbyUnitsByTeam(loc.mapLocation(), unit.visionRange(), manager.getOpposingTeam());
            }
            if (type == UnitType.Factory || type == UnitType.Rocket)
                blueprint = (unit.structureIsBuilt() == 0);
        }
        catch (Exception e) {
            System.out.format("%s thrown in Robo constructor", e.getClass().toString());
        }
    }

    public GameManager getManager() {
        return manager;
    }

    public int getUnitId() {
        return unitId;
    }

    public Action getHandler() {
        return handler;
    }

    public void setHandler(Action handler) {
        this.handler = handler;
    }

    public int compareTo(Robo other) {
        return this.unitId - other.unitId;
    }

    public long getRoundLastUpdated() {
        return roundLastUpdated;
    }

    public void setRoundLastUpdated(long roundLastUpdated) {
        this.roundLastUpdated = roundLastUpdated;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Robo))
            return false;
        Robo otherRobot = (Robo) other;
        return (unitId == otherRobot.unitId);
    }

    @Override
    public int hashCode() {
        return new Integer(unitId).hashCode();
    }
}
