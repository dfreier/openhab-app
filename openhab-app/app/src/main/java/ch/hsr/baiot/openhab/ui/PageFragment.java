package ch.hsr.baiot.openhab.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.logging.Handler;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.model.WidgetModel;

public class PageFragment extends Fragment {



    private RecyclerView mPageView;
    private RecyclerView.LayoutManager mLayoutManager;
    private WidgetModel mWidgetModel;
    private SwipeRefreshLayout mRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;

    public PageFragment() {

    }


    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener refreshListener) {
        mOnRefreshListener = refreshListener;
        if(mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        }
    }

    public SwipeRefreshLayout.OnRefreshListener getRefreshListener() {
        return mOnRefreshListener;
    }

    public void setPageModel(WidgetModel widgetModel) {
        mWidgetModel = widgetModel;
        updateAdapter();


    }

    public void setRefreshing(boolean isRefreshing) {
        if(mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(isRefreshing);
        }
    }

    private void updateAdapter() {
        if(mPageView != null && mWidgetModel != null) {
            mPageView.setAdapter(new PageAdapter(mWidgetModel.onModification()));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_page, container, false);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refresh_layout);
        if(mOnRefreshListener != null) mRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        mPageView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mPageView.setLayoutManager(mLayoutManager);
        updateAdapter();
        return layout;
    }


}
