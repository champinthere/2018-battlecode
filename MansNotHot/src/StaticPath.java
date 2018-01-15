import bc.MapLocation;

public class StaticPath {
    public static class StaticPathEntry implements Comparable<StaticPathEntry> {
        double cost;
        StaticPath path;

        @Override
        public int compareTo(StaticPathEntry o) {
            return new Double(cost).compareTo(o.cost);
        }

        public StaticPathEntry(double a, StaticPath b) {
            cost = a;
            path = b;
        }
    }

    public static double getCost(MapLocation a, MapLocation b) {
        return Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));
    }

    public MapLocation getLoc() {
        return loc;
    }

    public StaticPath getNext() {
        return next;
    }

    public void setNext(StaticPath next) {
        this.next = next;
    }

    private MapLocation loc;
    private StaticPath next;

    public StaticPath(MapLocation loc) {
        this.loc = loc;
    }

    public StaticPath(MapLocation loc, StaticPath next) {
        this.loc = loc;
        this.next = next;
    }
}
