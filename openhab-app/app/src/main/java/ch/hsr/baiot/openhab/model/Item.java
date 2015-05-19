package ch.hsr.baiot.openhab.model;

/**
 * Created by dominik on 12.05.15.
 */
public class Item implements MemberEquals<Item>{
    public String type  = "";
    public String name = "";
    public String state = "";
    public String link = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!name.equals(item.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean hasEqualMembers(Item other) {
        if(this == other) return true;
        if(other == null) return false;

        if(this.type != null ? !this.type.equals(other.type) : other.type != null) return false;
        if(this.state != null ? !this.state.equals(other.state) : other.state != null) return false;
        if(this.link != null ? !this.link.equals(other.link) : other.link != null) return false;

        return true;
    }
}
