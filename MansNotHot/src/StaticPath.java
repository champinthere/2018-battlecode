import bc.MapLocation;

public class StaticPath {
    public static class StaticPathEntry implements Comparable<StaticPathEntry> {
        int cost;
        StaticPath path;

        @Override
        public int compareTo(StaticPathEntry o) {
            return Integer.valueOf(cost).compareTo(o.cost);
        }

        public StaticPathEntry(int a, StaticPath b) {
            cost = a;
            path = b;
        }
    }

    public static int getCost(MapLocation a, MapLocation b) {
        // computes Chebyshev Distance
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
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

    public String toString() {
        String result = "";
        for (StaticPath s = this; s != null; s = s.getNext()) {
            int sx = s.getLoc().getX();
            int sy = s.getLoc().getY();
            result += ((s == this ? "" : " -> ") + "(" + sx + ", " + sy + ")");
        }
        return result;
    }
}
