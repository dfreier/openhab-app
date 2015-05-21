package ch.hsr.baiot.openhab.sdk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.hsr.baiot.openhab.sdk.util.ListUtils;
import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by dominik on 18.05.15.
 */
public class WidgetListModel {

    private Subject<ListModificationEvent<Widget>, ListModificationEvent<Widget>> subject = new SerializedSubject<>(ReplaySubject.create());

    private List<Widget> widgets = new LinkedList<>();

    public List<Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<Widget> modified) {



        List<Widget> original = new ArrayList<>(widgets);
        List<Widget> state = new ArrayList<>(widgets);

        List<Widget> changed = ListUtils.changed(original, modified);
        notifyChanged(changed, original);
        state = ListUtils.update(state, changed);

        List<Widget> removed = ListUtils.removed(original, modified);
        notifyRemoved(removed, original);
        state.removeAll(removed);

        List<Widget> added = ListUtils.added(original, modified);
        notifyAdded(added, modified);
        state = ListUtils.insertAddedAndRejectRemoved(state, modified);

        Set<Set<Integer>> moves = getMoves(modified, state);
        notifyMoved(modified, moves);

        widgets = modified;

    }


    public void notifyChanged(List<Widget> changed, List<Widget> original) {
        for(Widget widget : changed) {
            int index = original.indexOf(widget);
            subject.onNext( new ListModificationEvent<Widget>(
                    widget,
                    ListModificationEvent.CHANGED,
                    index,
                    index
            ));
        }
    }

    public void notifyRemoved(List<Widget> removed, List<Widget> original) {
        for(Widget widget : removed) {
            subject.onNext( new ListModificationEvent<Widget>(
                    widget,
                    ListModificationEvent.REMOVED,
                    original.indexOf(widget),
                    -1
            ));
        }
    }

    public void notifyAdded(List<Widget> added, List<Widget> modified) {
        for(Widget widget : added) {
            ListModificationEvent<Widget> event = new ListModificationEvent<Widget>(
                    widget,
                    ListModificationEvent.INSERTED,
                    -1,
                    modified.indexOf(widget)
            );
            subject.onNext(event );
        }
    }

    private void notifyMoved(List<Widget> modified, Set<Set<Integer>> moves) {
        for(Set<Integer> pair : moves) {
            List<Integer> pairList = new ArrayList(pair);
            int oldPos = pairList.get(0);
            int newPos = pairList.get(1);
            subject.onNext( new ListModificationEvent<Widget>(
                    modified.get(newPos),
                    ListModificationEvent.MOVED,
                    oldPos,
                    newPos
            ));
        }
    }

    private Set<Set<Integer>> getMoves(List<Widget> modified, List<Widget> state) {
        List<Widget> moved = ListUtils.moved(state, modified);
        Set<Set<Integer>> moves = new HashSet<>();
        for(Widget widget : moved) {
            int oldPos = state.indexOf(widget);
            int newPos = modified.indexOf(widget);
            moves.add(new HashSet<>(Arrays.asList(new Integer[]{oldPos, newPos})));
        }
        return moves;
    }


    public Observable<ListModificationEvent<Widget>> onModification() {
        return subject;
    }

}
