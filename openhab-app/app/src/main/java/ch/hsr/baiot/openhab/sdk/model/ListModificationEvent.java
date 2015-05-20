package ch.hsr.baiot.openhab.sdk.model;

/**
 * Created by dominik on 18.05.15.
 */
public class ListModificationEvent<T> {

    public static final int TRANSFORMATION_TYPE_INSERTED = 0;
    public static final int TRANSFORMATION_TYPE_CHANGED = 1;
    public static final int TRANSFORMATION_TYPE_REMOVED = 2;
    public static final int TRANSFORMATION_TYPE_MOVED = 3;

    public T item;
    public int transformationType;
    public int oldPos;
    public int newPos;

    public ListModificationEvent(T item, int transformationType, int oldPos, int newPos) {
        this.item = item;
        this.transformationType = transformationType;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }
}
