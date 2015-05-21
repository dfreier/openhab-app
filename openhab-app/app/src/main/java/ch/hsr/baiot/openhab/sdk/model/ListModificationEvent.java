package ch.hsr.baiot.openhab.sdk.model;

/**
 * Created by dominik on 18.05.15.
 */
public class ListModificationEvent<T> {

    public static final int INSERTED = 0;
    public static final int CHANGED = 1;
    public static final int REMOVED = 2;
    public static final int MOVED = 3;

    public T item;
    public int modificationType;
    public int oldPos;
    public int newPos;

    public ListModificationEvent(T item, int modificationType, int oldPos, int newPos) {
        this.item = item;
        this.modificationType = modificationType;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }
}
