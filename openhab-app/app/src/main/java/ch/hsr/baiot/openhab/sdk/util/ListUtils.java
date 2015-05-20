package ch.hsr.baiot.openhab.sdk.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.hsr.baiot.openhab.sdk.model.MemberEquals;


/**
 * Created by dominik on 19.05.15.
 */
public class ListUtils {

    public static <T> List<T> added(List<T> original, List<T> modified) {
        List<T> add = new ArrayList(modified);
        add.removeAll(original);
        return add;
    }

    public static <T> List<T> removed(List<T> original, List<T> modified) {
        List<T> remove = new ArrayList<>(original);
        remove.removeAll(modified);
        return remove;
    }

    public static <T> List<T> intersect(List<T> original, List<T> modified) {
        Set<T> intersect = new LinkedHashSet<>();
        intersect.addAll(original);
        intersect.addAll(modified);
        intersect.removeAll(added(original, modified));
        intersect.removeAll(removed(original, modified));
        return new ArrayList(intersect);
    }


    public static <T> List<T> moved(List<T> original, List<T> modified) {
        List<T> originalWithoutRemoved = new ArrayList<>(original);
        originalWithoutRemoved.removeAll(removed(original, modified));

        List<T> modifiedWithoutAdded = new ArrayList<>(modified);
        modifiedWithoutAdded.removeAll(added(original, modified));

        List<T> moved = new LinkedList<>();

        for(int i = 0; i < originalWithoutRemoved.size(); i++) {
            if(!originalWithoutRemoved.get(i).equals(modifiedWithoutAdded.get(i))) {
                moved.add(modifiedWithoutAdded.get(i));
            }
        }

        return moved;
    }

    public static <T extends MemberEquals<T>> List<T> changed(List<T> original, List<T> modified) {
        List<T> commons = new LinkedList<>(modified);
        commons.removeAll(added(original, modified));
        List<T> changed = new LinkedList<>();
        for(T element : commons) {
            T fromOriginal = original.get(original.indexOf(element));
            T fromModified = modified.get(modified.indexOf(element));
            if(!fromOriginal.hasEqualMembers(fromModified)) changed.add(element);
        }
        return changed;
    }

    public static <T> List<T> insertAddedAndRejectRemoved(List<T> original, List<T> modified) {
        List<T> result = new LinkedList<T>(original);
        result.removeAll(removed(original, modified));
        List<T> added = added(original, modified);
        for(T element : added) {
            result.add(modified.indexOf(element), element);
        }
        return result;
    }


    public static <T> List<T> update(List<T> original, List<T> changed) {
        List<T> updated = new ArrayList<>(original);
        for(T element : changed) {
            updated.set(updated.indexOf(element), element);
        }
        return updated;
    }





}
