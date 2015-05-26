package ch.hsr.baiot.openhab.app.widget;

import android.view.View;

import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.sdk.model.Widget;

/**
 * Created by dominik on 21.05.15.
 */
public class SelectableWidget extends WidgetViewHolder implements View.OnClickListener {

    public WidgetListAdapter.OnWidgetListActionListener listener;

    public SelectableWidget(View itemView, WidgetListAdapter.OnWidgetListActionListener listener) {
        super(itemView);
        this.listener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(listener != null) listener.onClick(widget);
    }
}
