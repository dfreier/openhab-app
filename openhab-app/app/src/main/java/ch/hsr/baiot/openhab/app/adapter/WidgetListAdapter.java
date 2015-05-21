package ch.hsr.baiot.openhab.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.model.ListModificationEvent;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dominik on 18.05.15.
 */
public class WidgetListAdapter extends RecyclerView.Adapter<WidgetListAdapter.ViewHolder> {


    private List<Widget> mWidgets = new LinkedList<>();
    private OnWidgetListClickListener mListener;

    Observable<ListModificationEvent<Widget>> mModifications;



    public WidgetListAdapter(Observable<ListModificationEvent<Widget>> modifications, OnWidgetListClickListener listener) {

        mModifications = modifications;
        mListener = listener;


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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        FrameLayout frame = (FrameLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_page, viewGroup, false);
        ViewHolder vh = new ViewHolder(frame, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.widget = mWidgets.get(i);
        if(mWidgets.get(i).type.equals("Frame")) {
            viewHolder.textView.setText(mWidgets.get(i).label);
        }
        else viewHolder.textView.setText(mWidgets.get(i).label + " : " + mWidgets.get(i).item.state);
    }

    @Override
    public int getItemViewType(int position) {
        Widget widget = mWidgets.get(position);
        if(widget.type.equals("Frame")) return 1;
        else if(widget.type.equals("Group")) return 2;
        return 0;
    }

    @Override
    public int getItemCount() {
        return mWidgets.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        private OnWidgetListClickListener mListener;
        public Widget widget;

        public ViewHolder(FrameLayout frame, OnWidgetListClickListener listener) {
            super(frame);
            mListener = listener;
            textView = (TextView) frame.findViewById(R.id.text_view);
            frame.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) mListener.onClick(widget);
        }
    }

    public static interface OnWidgetListClickListener {
        public void onClick(Widget widget);
    }
}
