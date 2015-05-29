package ch.hsr.baiot.openhab.app.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ch.hsr.baiot.openhab.sdk.model.Widget;

/**
 * Created by dominik on 21.05.15.
 */
public class WidgetViewHolder extends RecyclerView.ViewHolder {

    public Widget widget;

    public WidgetViewHolder(View itemView) {
        super(itemView);
    }
}
