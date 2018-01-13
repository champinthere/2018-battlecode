import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class ActionQueue implements Iterable<Action> {
    private PriorityQueue<Action> contents;
    public ActionQueue() {
        contents = new PriorityQueue<>();
    }

    public void add(Action action) {
        contents.add(action);
    }

    public Action peek() {
        return contents.peek();
    }

    public Action remove() {
        return contents.remove();
    }

    public int size() {
        return contents.size();
    }

    @Override
    public Iterator<Action> iterator() {
        return contents.iterator();
    }

    public List<Action> asList() {
        return new ArrayList<>(contents);
    }
}
