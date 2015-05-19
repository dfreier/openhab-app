package ch.hsr.baiot.openhab.model;

/**
 * Created by dominik on 18.05.15.
 */
public interface MemberEquals<T> {
    public boolean hasEqualMembers(T other);
}
