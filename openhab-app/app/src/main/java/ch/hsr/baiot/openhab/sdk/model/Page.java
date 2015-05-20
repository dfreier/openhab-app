package ch.hsr.baiot.openhab.sdk.model;

import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.util.ArrayUtils;
import rx.Observable;

/**
 * Created by dominik on 12.05.15.
 */
public class Page implements MemberEquals<Page>{

    public String id = "";
    public String title = "";
    public String link = "";
    public Boolean leaf = false;
    public Widget[] widget = new Widget[0];





    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (id != null ? !id.equals(page.id) : page.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean hasEqualMembers(Page other) {
        if(this == other) return true;
        if(other == null) return false;

        if(this.title != null ? !this.title.equals(other.title) : other.title != null) return false;
        if(this.link != null ? !this.link.equals(other.link) : other.link != null) return false;
        if(this.leaf != null ? !this.leaf.equals(other.leaf) : other.leaf != null) return false;
        if(this.widget != null ? !ArrayUtils.hasEqualMembers(this.widget, other.widget) : other.widget != null) return false;

        return true;
    }

    public Observable<Page> subscribeToPageUpdates(String sitemap) {
        return OpenHabSdk.getPushClient().subscribe(sitemap, this);
    }


}
