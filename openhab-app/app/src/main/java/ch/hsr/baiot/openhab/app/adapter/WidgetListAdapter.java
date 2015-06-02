package ch.hsr.baiot.openhab.app.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ch.hsr.baiot.openhab.app.widget.WidgetFactory;
import ch.hsr.baiot.openhab.app.widget.WidgetViewHolder;
import ch.hsr.baiot.openhab.app.widget.WidgetWebview;
import ch.hsr.baiot.openhab.sdk.model.ListModificationEvent;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dominik on 18.05.15.
 */
public class WidgetListAdapter extends RecyclerView.Adapter<WidgetViewHolder> implements RefreshScheduler {


    private List<Widget> mWidgets = new LinkedList<>();
    private OnWidgetListActionListener mListener;

    Observable<ListModificationEvent<Widget>> mModifications;

    private Activity mContext;

    private List<WidgetWebview> mScheduledWidgets = new LinkedList<>();
    private Timer mScheduler;



    public WidgetListAdapter(Observable<ListModificationEvent<Widget>> modifications, OnWidgetListActionListener listener, Activity context) {

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
        return WidgetFactory.createViewHolder(type, viewGroup, mListener);
    }

    @Override
    public void onBindViewHolder(WidgetViewHolder viewHolder, int i) {
        viewHolder.widget = mWidgets.get(i);
        WidgetFactory.bindViewHolder(viewHolder, this);
    }

    @Override
    public int getItemViewType(int position) {
        return WidgetFactory.getType(mWidgets.get(position));
    }

    @Override
    public int getItemCount() {
        return mWidgets.size();
    }

    @Override
    public void register(WidgetWebview viewHolder) {
        if(!mScheduledWidgets.contains(viewHolder)) {
            mScheduledWidgets.add(viewHolder);
            setupTimer();
        }
    }

    @Override
    public void unregister(WidgetWebview viewHolder) {
        mScheduledWidgets.remove(viewHolder);
    }

    @Override
    public void requestRefresh(WidgetWebview viewHolder) {

    }

    public void onPause() {
        mScheduledWidgets = new LinkedList<>();
        if(mScheduler != null) mScheduler.cancel();

    }

    private void setupTimer() {
        if(mScheduler == null) {
            mScheduler = new Timer();
            mScheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(WidgetWebview widget : mScheduledWidgets) {
                                widget.webView.loadUrl("http://192.168.1.15:80/jpg/1/image.jpg");
                            }
                        }
                    });
                }
            }, 0, 100);
        }

    }


    public static interface OnWidgetListActionListener {
        public void onClick(Widget widget);
        public void onStateUpdate(Widget widget, String state);
    }


}
