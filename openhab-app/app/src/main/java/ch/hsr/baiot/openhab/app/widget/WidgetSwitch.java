package ch.hsr.baiot.openhab.app.widget;

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;

/**
 * Created by dominik on 26.05.15.
 */
public class WidgetSwitch extends WidgetText  {

    public SwitchCompat switchView;

    public WidgetSwitch(ViewGroup container, WidgetListAdapter.OnWidgetListActionListener listener) {
        super(container, listener);
        switchView = (SwitchCompat) container.findViewById(R.id.switch_compat);
        switchView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == switchView) {
            boolean checked = ((SwitchCompat)v).isChecked();
            if(listener != null) listener.onStateUpdate(widget, checked ? "ON" : "OFF");
        } else {
            if(listener != null) listener.onClick(widget);
        }
    }
}
