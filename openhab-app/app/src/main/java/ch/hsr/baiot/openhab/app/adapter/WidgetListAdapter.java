package ch.hsr.baiot.openhab.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.widget.WidgetFrame;
import ch.hsr.baiot.openhab.app.widget.WidgetGroup;
import ch.hsr.baiot.openhab.app.widget.WidgetSwitch;
import ch.hsr.baiot.openhab.app.widget.WidgetText;
import ch.hsr.baiot.openhab.app.widget.WidgetViewHolder;
import ch.hsr.baiot.openhab.sdk.model.ListModificationEvent;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dominik on 18.05.15.
 */
public class WidgetListAdapter extends RecyclerView.Adapter<WidgetViewHolder> {


    private List<Widget> mWidgets = new LinkedList<>();
    private OnWidgetListActionListener mListener;

    Observable<ListModificationEvent<Widget>> mModifications;

    private Context mContext;



    public WidgetListAdapter(Observable<ListModificationEvent<Widget>> modifications, OnWidgetListActionListener listener, Context context) {

        mModifications = modifications;
        mListener = listener;
        mContext = context;


        mModifications
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    int type = t.modificationType;
                    switch (t.modificationType) {
                        case ListModificationEvent.INSERTED:
                            itemInserted(t);
                            break;
                        case ListModificationEvent.CHANGED:
                            itemChanged(t);
                            break;
                        case ListModificationEvent.REMOVED:
                            itemRemoved(t);
                            break;
                        case ListModificationEvent.MOVED:
                            itemMoved(t);
                        default:
                            break;
                    }
                });
    }

    private void itemChanged(ListModificationEvent<Widget> event) {
        Widget widget = mWidgets.get(event.oldPos);
        if(widget.widgetId.equals(event.item.widgetId)); {
            Log.d("test", "change from " + widget.item.state + " to " + event.item.item.state);
            widget.type = event.item.type;
            widget.label = event.item.label;
            widget.icon = event.item.icon;
            widget.linkedPage = event.item.linkedPage;
            widget.item = event.item.item;
            notifyItemChanged(event.oldPos);
        }
    }

    private void itemInserted(ListModificationEvent<Widget> event) {
        mWidgets.add(event.newPos, event.item);
        notifyItemInserted(event.newPos);
    }

    private void itemRemoved(ListModificationEvent<Widget> event) {
        Widget widget = mWidgets.get(event.oldPos);
        if(widget.widgetId.equals(event.item.widgetId)) {
            mWidgets.remove(event.oldPos);
            notifyItemRemoved(event.oldPos);
        }
    }

    private void itemMoved(ListModificationEvent<Widget> event) {
        Widget widget = mWidgets.get(event.oldPos);
        if(widget.widgetId.equals(event.item.widgetId)) {
            widget = mWidgets.remove(event.oldPos);
            mWidgets.add(event.newPos, widget);
            notifyItemMoved(event.oldPos, event.newPos);
        }
    }


    @Override
    public WidgetViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        ViewGroup container = null;
        WidgetViewHolder vh = null;
        switch(type) {
            case 1:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_frame, viewGroup, false);
                vh = new WidgetFrame(container, mListener);
                break;
            case 2:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_group, viewGroup, false);
                vh = new WidgetGroup(container, mListener);
                break;
            case 3:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_switch, viewGroup, false);
                vh = new WidgetSwitch(container, mListener);
                break;
            default:
                container = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.widget_text, viewGroup, false);
                vh = new WidgetText(container, mListener);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(WidgetViewHolder viewHolder, int i) {
        viewHolder.widget = mWidgets.get(i);

        if(viewHolder.widget.type.equals("Frame")) {
            ((WidgetFrame) viewHolder).textView.setText(mWidgets.get(i).label);
        }
        else if(viewHolder.widget.type.equals("Group")) {
            float scale = mContext.getResources().getDisplayMetrics().density;
            int dp8 = (int) (8*scale + 0.5f);
            int dp4 = (int) (2*scale + 0.5f);
            if(i == 0 || i == mWidgets.size() - 1) {
             //   viewHolder.itemView.setPaddingRelative(dp8,dp8,dp8,dp8);
            } else {
               // viewHolder.itemView.setPaddingRelative(dp8,dp4,dp8,dp4);
            }
            ((WidgetGroup) viewHolder).titleView.setText(viewHolder.widget.label);
            ((WidgetGroup) viewHolder).subtitleView.setText(viewHolder.widget.label);

        }
        else if(viewHolder.widget.type.equals("Switch") && viewHolder.widget.icon.contains("switch")){

            if(viewHolder.widget.icon.equals("switch-on")) {
                ((WidgetSwitch)viewHolder).icon.setImageResource(R.drawable.switch_on);
            } else {
                ((WidgetSwitch)viewHolder).icon.setImageResource(R.drawable.switch_off);
            }
            ((WidgetSwitch)viewHolder).textView.setText(mWidgets.get(i).label);
            ((WidgetSwitch)viewHolder).detailTextView.setText(mWidgets.get(i).item.state);
            ((WidgetSwitch)viewHolder).switchView.setChecked("ON".equals(mWidgets.get(i).item.state));
        } else {
            ((WidgetText)viewHolder).textView.setText(mWidgets.get(i).label);
            ((WidgetText)viewHolder).detailTextView.setText(mWidgets.get(i).item.state);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Widget widget = mWidgets.get(position);
        if(widget.type.equals("Frame")) return 1;
        else if(widget.type.equals("Group")) return 2;
        else if(widget.type.equals("Switch")) return 3;
        return 0;
    }

    @Override
    public int getItemCount() {
        return mWidgets.size();
    }




    public static interface OnWidgetListActionListener {
        public void onClick(Widget widget);
        public void onStateUpdate(Widget widget, String state);
    }
}
