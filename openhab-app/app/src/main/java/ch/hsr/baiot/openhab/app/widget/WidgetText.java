package ch.hsr.baiot.openhab.app.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;

/**
 * Created by dominik on 26.05.15.
 */
public class WidgetText extends SelectableWidget {
    public TextView textView;
    public TextView detailTextView;
    public ImageView icon;

    public WidgetText(ViewGroup container, WidgetListAdapter.OnWidgetListActionListener listener) {
        super(container, listener);
        textView = (TextView) container.findViewById(R.id.text_view);
        detailTextView = (TextView) container.findViewById(R.id.text_detail);
        icon = (ImageView) container.findViewById(R.id.icon);
    }

}
