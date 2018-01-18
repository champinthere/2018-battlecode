import bc.Unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public abstract class Action implements Comparable<Action> {
    private int priority = 10; // priority goes in decreasing order from 1 to any integer (suggested range 1-10)
    private HashSet<Integer> units;
    private Runnable uponTermination;
    private GameManager manager;
    private boolean terminated = false;
    private int roundCreated;

    private int projectedCompletion = 1000;

    public Runnable getUponTermination() {
        return uponTermination;
    }

    public void setUponTermination(Runnable uponTermination) {
        this.uponTermination = uponTermination;
    }

    public GameManager getManager() {
        return manager;
    }

    public int getRoundCreated() {
        return roundCreated;
    }

    public int getProjectedCompletion() {
        return projectedCompletion;
    }

    public void setProjectedCompletion(int projectedCompletion) {
        this.projectedCompletion = projectedCompletion;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public int compareTo(Action other) {
        return priority - other.priority;
    }

    public boolean isTerminated() {
        return terminated;
    }

    protected void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public void initialize(GameManager manager) {
        units = new HashSet<>();
        this.manager = manager;
        this.roundCreated = (int) manager.controller().round();
    }

    public void addUnit(Integer id) {
        units.add(id);
    }

    public abstract ActionStatus execute();

    public void updateMeta() {
        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        for (Integer id: units)
            if (!manager.getIdMap().containsKey(id))
                toBeRemoved.add(id);
        for (Integer id: toBeRemoved)
            units.remove(id);
    }

    public void terminate() {
        System.out.println(this);
        if (uponTermination != null)
            uponTermination.run();
    }
}
