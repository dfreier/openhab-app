package ch.hsr.baiot.openhab.app.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.sdk.model.Widget;

/**
 * Created by dominik on 21.05.15.
 */
public class SelectableWidget extends WidgetViewHolder implements View.OnClickListener {

    private WidgetListAdapter.OnWidgetListClickListener mListener;
    public Widget widget;

    public SelectableWidget(View itemView, WidgetListAdapter.OnWidgetListClickListener listener) {
        super(itemView);
        mListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(mListener != null) mListener.onClick(widget);
    }
}
