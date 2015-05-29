package ch.hsr.baiot.openhab.app.widget;

import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;

/**
 * Created by dominik on 27.05.15.
 */
public class WidgetGroup extends SelectableWidget {

    public CardView cardView;
    public ImageView imageView;
    public TextView titleView;
    public TextView subtitleView;
    public Button buttonView;

    public WidgetGroup(ViewGroup container, WidgetListAdapter.OnWidgetListActionListener listener) {
        super(container, listener);
        cardView = (CardView) container.findViewById(R.id.card_view);
        imageView = (ImageView) container.findViewById(R.id.image_view);
        titleView = (TextView) container.findViewById(R.id.title_view);
        subtitleView = (TextView) container.findViewById(R.id.subtitle_view);
        //buttonView = (Button) container.findViewById(R.id.button_view);
    }
}
