package ch.hsr.baiot.openhab.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, WidgetListAdapter.OnWidgetListClickListener {

    private static final String ARG_SITEMAP = "argSitemap";
    private static final String ARG_PAGE = "argPage";

    private String mSitemapName;
    private String mPageId;

    private WidgetListModel mWidgetListModel;
    private Page mPage;

    private RecyclerView mWidgetListView;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mRefreshLayout;
    private Subscription mGetPageSubscription;
    private boolean mIsLoading = false;
    private boolean mDidPauseWhileLoading = false;

    private PageFragmentListener mPageFragmentListener;
    private boolean mIsFreezed;


    public void setPageFragmentListener(PageFragmentListener listener) {
        mPageFragmentListener = listener;
    }

    public PageFragmentListener getPageFragmentListener() {
        return mPageFragmentListener;
    }

    public static PageFragment newInstance(String sitemap, String page) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITEMAP, sitemap);
        args.putString(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSitemapName = getArguments().getString(ARG_SITEMAP);
            mPageId = getArguments().getString(ARG_PAGE);
            loadPage();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_page, container, false);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mWidgetListView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mWidgetListView.setLayoutManager(mLayoutManager);


        return layout;
    }


    @Override
    public void onPause() {
        super.onPause();
        mDidPauseWhileLoading = mIsLoading;
        unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mDidPauseWhileLoading && !mIsFreezed) loadPage();
        mIsFreezed = false;
    }



    private void unsubscribe() {
        if(mGetPageSubscription != null) {
            mGetPageSubscription.unsubscribe();
        }
        setLoading(false);
    }


    private void loadPage() {
        unsubscribe();
        setLoading(true);
        mGetPageSubscription = OpenHabSdk.getOpenHabApi().getPage(mSitemapName, mPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Page>() {
                    @Override
                    public void onCompleted() {
                        setLoading(false);
                        mWidgetListModel = new WidgetListModel();
                        updateAdapter();
                        mWidgetListModel.setWidgets(Arrays.asList(mPage.widget));
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        Toast.makeText(getActivity(), "Cannot refresh page " + mPageId, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Page page) {
                        mPage = page;
                    }
                });
    }



    private void updateAdapter() {
        if(mWidgetListView != null && mWidgetListModel != null) {
            mWidgetListView.setAdapter(new WidgetListAdapter(mWidgetListModel.onModification(), this));
        }
    }


    private void setLoading(boolean isLoading) {
        this.mIsLoading = isLoading;
        if(mRefreshLayout != null) mRefreshLayout.setEnabled(!isLoading);
        if(mRefreshLayout != null) mRefreshLayout.setRefreshing(isLoading);
    }

    private void freeze() {
        unsubscribe();
        mIsFreezed = true;
    }

    @Override
    public void onRefresh() {
        if(!mIsLoading) loadPage();
    }

    @Override
    public void onClick(Widget widget) {
        if(mPageFragmentListener != null) {
            freeze();
            mPageFragmentListener.onNavigateTo(mSitemapName, widget.linkedPage.id);
        }
    }


    public static interface PageFragmentListener {
        public void onNavigateTo(String sitemap, String page);
    }
}
