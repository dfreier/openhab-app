package ch.hsr.baiot.openhab.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.model.ListModificationEvent;
import ch.hsr.baiot.openhab.model.Widget;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dominik on 18.05.15.
 */
public class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {


    private List<Widget> mWidgets = new LinkedList<>();

    Observable<ListModificationEvent<Widget>> mModifications;



    public PageAdapter(Observable<ListModificationEvent<Widget>> modifications) {

        mModifications = modifications;

        mModifications
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    int type = t.transformationType;
                    switch (t.transformationType) {
                        case ListModificationEvent.TRANSFORMATION_TYPE_INSERTED:
                            itemInserted(t);
                            break;
                        case ListModificationEvent.TRANSFORMATION_TYPE_CHANGED:
                            itemChanged(t);
                            break;
                        case ListModificationEvent.TRANSFORMATION_TYPE_REMOVED:
                            itemRemoved(t);
                            break;
                        case ListModificationEvent.TRANSFORMATION_TYPE_MOVED:
                            itemMoved(t);
                        default:
                            break;
                    }
                });
    }

    private void itemChanged(ListModificationEvent<Widget> event) {
        Widget widget = mWidgets.get(event.oldPos);
        if(widget.widgetId.equals(event.item.widgetId)); {
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        FrameLayout frame = (FrameLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_page, viewGroup, false);
        ViewHolder vh = new ViewHolder(frame);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mTextView.setText(mWidgets.get(i).item.name + " : " + mWidgets.get(i).item.state);
    }

    @Override
    public int getItemCount() {
        return mWidgets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(FrameLayout frame) {
            super(frame);
            mTextView = (TextView) frame.findViewById(R.id.text_view);
        }
    }
}
