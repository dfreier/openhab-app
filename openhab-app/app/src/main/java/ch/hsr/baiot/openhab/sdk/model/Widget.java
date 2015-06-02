package ch.hsr.baiot.openhab.sdk.model;

import ch.hsr.baiot.openhab.sdk.util.ArrayUtils;

/**
 * Created by dominik on 12.05.15.
 */
public class Widget implements MemberEquals<Widget> {
    public String widgetId = "";
    public String type = "";
    public String label = "";
    public String icon = "";
    public String url = "";
    public Item item = new Item();
    public Page linkedPage = new Page();
    public Widget[] widget = new Widget[0];


    @Override
    public boolean hasEqualMembers(Widget other) {
        if(this == other) return true;
        if(other == null) return false;

        if(this.type != null ? !this.type.equals(other.type) : other.type != null) return false;
        if(this.icon != null ? !this.icon.equals(other.icon) : other.icon != null) return false;
        if(this.label != null ? !this.label.equals(other.label) : other.label != null) return false;
        if(this.item != null ? !this.item.equals(other.item) : other.item != null) return false;
        if(this.linkedPage != null ? !this.linkedPage.equals(other.linkedPage) : other.linkedPage != null) return false;
        if(this.widget != null ? !ArrayUtils.hasEqualMembers(this.widget, other.widget) : other.widget != null) return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Widget widget = (Widget) o;

        if (widgetId != null ? !widgetId.equals(widget.widgetId) : widget.widgetId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return widgetId != null ? widgetId.hashCode() : 0;
    }
}
