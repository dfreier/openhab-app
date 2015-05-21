package ch.hsr.baiot.openhab.app.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;

/**
 * Created by dominik on 21.05.15.
 */
public class WidgetFrame extends SelectableWidget{

    public TextView textView;

    public WidgetFrame(ViewGroup container, WidgetListAdapter.OnWidgetListClickListener listener) {
        super(container, listener);
        textView = (TextView) container.findViewById(R.id.text_view);
    }
}
