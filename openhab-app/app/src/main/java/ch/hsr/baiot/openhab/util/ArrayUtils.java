package ch.hsr.baiot.openhab.util;

import ch.hsr.baiot.openhab.model.MemberEquals;

/**
 * Created by dominik on 19.05.15.
 */
public class ArrayUtils {

    public static <T extends MemberEquals> boolean hasEqualMembers(T[] object, T[] other) {
        if(object == null || other == null) return false;
        if(object.length != other.length) return false;

        for(int i = 0; i < object.length; i++) {
            if(!object[i].hasEqualMembers(other[i])) return false;
        }
        return true;
    }
}
