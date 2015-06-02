package ch.hsr.baiot.openhab.app.adapter;

import ch.hsr.baiot.openhab.app.widget.WidgetWebview;

/**
 * Created by dominik on 02.06.15.
 */
public interface RefreshScheduler {
    public void register(WidgetWebview viewHolder);
    public void unregister(WidgetWebview viewHolder);
    public void requestRefresh(WidgetWebview viewHolder);
}