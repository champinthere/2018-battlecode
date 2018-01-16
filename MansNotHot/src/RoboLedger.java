import bc.Unit;
import bc.UnitType;

import java.util.TreeSet;

public class RoboLedger {
    private int numWorkers;
    private int numKnights;
    private int numRangers;
    private int numMages;
    private int numHealers;
    private int numFactories;
    private int numFactoryBlueprints;
    private int numRockets;
    private int numRocketBlueprints;
    private TreeSet<Robo> blueprints; // mutability should not matter because want updated list on new turns anyway

    public RoboLedger() {
        blueprints = new TreeSet<>();
    }

    public int getCount(UnitType type) {
        switch (type) {
            case Worker:    return getNumWorkers();
            case Knight:    return getNumKnights();
            case Ranger:    return getNumRangers();
            case Mage:      return getNumMages();
            case Healer:    return getNumHealers();
            case Factory:   return getNumFactories();
            case Rocket:    return getNumRockets();
        }
        return -1;
    }

//    public void updateWithUnit(Unit unit) {
//        UnitType type = unit.unitType();
//        switch (type) {
//            case Worker:
//                ++numWorkers;
//                break;
//            case Knight:
//                ++numKnights;
//                break;
//            case Ranger:
//                ++numRangers;
//                break;
//            case Mage:
//                ++numMages;
//                break;
//            case Healer:
//                ++numHealers;
//                break;
//            case Factory:
//                ++numFactories;
//                break;
//            case Rocket:
//                ++numRockets;
//                break;
//        }
//    }

    public void removeUnit(Unit unit) {
        UnitType type = unit.unitType();
        switch (type) {
            case Worker:
                --numWorkers;
                break;
            case Knight:
                --numKnights;
                break;
            case Ranger:
                --numRangers;
                break;
            case Mage:
                --numMages;
                break;
            case Healer:
                --numHealers;
                break;
            case Factory:
                --numFactories;
                break;
            case Rocket:
                --numRockets;
                break;
        }
    }

    public void updateWithRobo(Robo robo) {
        UnitType type = robo.getType();
        switch (type) {
            case Worker:
                ++numWorkers;
                break;
            case Knight:
                ++numKnights;
                break;
            case Ranger:
                ++numRangers;
                break;
            case Mage:
                ++numMages;
                break;
            case Healer:
                ++numHealers;
                break;
            case Factory:
                ++numFactories;
                break;
            case Rocket:
                ++numRockets;
                break;
        }
        if (robo.isBlueprint()) {
            blueprints.add(robo);
            if (type == UnitType.Factory) {
                --numFactories;
                ++numFactoryBlueprints;
            }
            if (type == UnitType.Rocket) {
                --numRockets;
                ++numRocketBlueprints;
            }
        }
    }

    public void removeRobo(Robo robo) {
        UnitType type = robo.getType();
        switch (type) {
            case Worker:
                --numWorkers;
                break;
            case Knight:
                --numKnights;
                break;
            case Ranger:
                --numRangers;
                break;
            case Mage:
                --numMages;
                break;
            case Healer:
                --numHealers;
                break;
            case Factory:
                --numFactories;
                break;
            case Rocket:
                --numRockets;
                break;
        }
    }

    public void clear() {
        numWorkers = 0;
        numKnights = 0;
        numRangers = 0;
        numMages = 0;
        numHealers = 0;
        numFactories = 0;
        numFactoryBlueprints = 0;
        numRockets = 0;
        numRocketBlueprints = 0;
        blueprints.clear();
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
    }

    public int getNumKnights() {
        return numKnights;
    }

    public void setNumKnights(int numKnights) {
        this.numKnights = numKnights;
    }

    public int getNumRangers() {
        return numRangers;
    }

    public void setNumRangers(int numRangers) {
        this.numRangers = numRangers;
    }

    public int getNumMages() {
        return numMages;
    }

    public void setNumMages(int numMages) {
        this.numMages = numMages;
    }

    public int getNumHealers() {
        return numHealers;
    }

    public void setNumHealers(int numHealers) {
        this.numHealers = numHealers;
    }

    public int getNumFactories() {
        return numFactories;
    }

    public void setNumFactories(int numFactories) {
        this.numFactories = numFactories;
    }

    public int getNumFactoryBlueprints() {
        return numFactoryBlueprints;
    }

    public void setNumFactoryBlueprints(int numFactoryBlueprints) {
        this.numFactoryBlueprints = numFactoryBlueprints;
    }

    public int getNumRockets() {
        return numRockets;
    }

    public void setNumRockets(int numRockets) {
        this.numRockets = numRockets;
    }

    public int getNumRocketBlueprints() {
        return numRocketBlueprints;
    }

    public void setNumRocketBlueprints(int numRocketBlueprints) {
        this.numRocketBlueprints = numRocketBlueprints;
    }

    public TreeSet<Robo> getBlueprints() { return blueprints; }

}