package ch.hsr.baiot.openhab.ui;

import java.util.Map;
import java.util.TreeMap;

import ch.hsr.baiot.openhab.util.RxBus;
import ch.hsr.baiot.openhab.model.Widget;
import rx.Observable;

/**
 * Created by dominik on 18.05.15.
 */
public class ItemViewModel<T> {


    private Map<String, Widget> widgetMap = new TreeMap<>();

    Observable<Widget> addedItems;
    Observable<Widget> removedItems;
    Observable<Widget> changedItems;

    private RxBus mBus;

    public ItemViewModel() {
        mBus = new RxBus();
    }

    public void startSubscriber() {
        addedItems.subscribe(widget -> {

        });
    }

    public Observable<Object> getBus() {
        return mBus.toObserverable();
    }

    public int[] getItemChangeIndices() {
        return null;
    }

    public int[] getItemInsertedIndices() {
        return null;
    }

    public int[] getItemRemovedIndices() {
        return null;
    }
}
