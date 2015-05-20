package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

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

public class PageActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, WidgetListAdapter.OnWidgetListClickListener{

    private static final String ARG_SITEMAP = "argSitemap";
    private static final String ARG_PAGE = "argPage";

    private String mSitemapName;
    private String mPageId;

    private WidgetListModel mWidgetListModel;
    private Page mPage;

    private RecyclerView mWidgetListView;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mRefreshLayout;
    private Subscription mLoadPageSubscription;
    private Subscription mPageUpdateSubscription;
    private boolean mPageIsLoading = false;
    private boolean mDidPauseWhileLoading = false;
    private boolean mDidStartOtherActivity = false;
    private boolean mUserDidRefresh = false;



    public static void start(Activity currentActivity, String sitemap, String page) {
        Intent intent = new Intent(currentActivity, PageActivity.class);
        Bundle args = new Bundle();
        args.putString(ARG_SITEMAP, sitemap);
        args.putString(ARG_PAGE, page);
        intent.putExtras(args);
        currentActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareUserInterface();
        assignArgumentsFromIntent();
        startInitialPageLoad();
    }

    private void prepareUserInterface() {
        setContentView(R.layout.activity_page);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mWidgetListView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mWidgetListView.setLayoutManager(mLayoutManager);
    }

    private void assignArgumentsFromIntent() {
        Bundle args = getIntent().getExtras();
        if(args != null) {
            mSitemapName = args.getString(ARG_SITEMAP);
            mPageId = args.getString(ARG_PAGE);
        }
    }

    private void startInitialPageLoad() {
        if(mSitemapName != null && mPageId != null) {
            loadPage();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mDidPauseWhileLoading = mPageIsLoading;
        unsubscribeFromActiveSubscriptions();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPage();
    }



    private void unsubscribeFromActiveSubscriptions() {
        if(mLoadPageSubscription != null) {
            mLoadPageSubscription.unsubscribe();
        }
        if(mPageUpdateSubscription != null) {
            mPageUpdateSubscription.unsubscribe();
        }
    }

    private void loadPage() {
        unsubscribeFromActiveSubscriptions();
        setPageIsLoading(true);
        mLoadPageSubscription = OpenHabSdk.getOpenHabApi().getPage(mSitemapName, mPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Page>() {
                    @Override
                    public void onCompleted() {
                        onLoadPageComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadPageError();
                    }

                    @Override
                    public void onNext(Page page) {
                        mPage = page;
                    }
                });
    }

    private void subscribeToPageUpdates() {
        mPageUpdateSubscription = OpenHabSdk.getPushClient().subscribe(mSitemapName, mPage)
                .subscribe(new Subscriber<Page>() {
                    @Override
                    public void onCompleted() {
                        loadPage();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof SocketTimeoutException) {
                            subscribeToPageUpdates();
                        } else {
                            loadPage();
                        }
                    }

                    @Override
                    public void onNext(Page page) {
                        mPage = page;
                        updateWidgetListModel(Arrays.asList(mPage.widget));
                    }
                });
    }

    private void onLoadPageComplete() {
        if(mWidgetListModel == null || mUserDidRefresh) {
            createWidgetListModel();
            assignWidgetListModelToAdapter();
        }
        updateWidgetListModel(Arrays.asList(mPage.widget));
        setPageIsLoading(false);
        subscribeToPageUpdates();
    }


    private void onLoadPageError() {
        setPageIsLoading(false);
        Toast.makeText(getApplicationContext(), "Cannot refresh page " + mPageId, Toast.LENGTH_SHORT).show();
    }


    private void updateWidgetListModel(List<Widget> widgets) {
        mWidgetListModel.setWidgets(widgets);
    }

    private void createWidgetListModel() {
        mWidgetListModel = new WidgetListModel();
    }

    private void assignWidgetListModelToAdapter() {
        if(mWidgetListView != null && mWidgetListModel != null) {
            mWidgetListView.setAdapter(new WidgetListAdapter(mWidgetListModel.onModification(), this));
        }
    }

    private void setPageIsLoading(boolean isLoading) {
        this.mPageIsLoading = isLoading;
        if(mRefreshLayout != null) mRefreshLayout.setEnabled(!isLoading);
        if(mRefreshLayout != null && mUserDidRefresh) mRefreshLayout.setRefreshing(isLoading);
        if(!isLoading) mUserDidRefresh = false;
    }


    @Override
    public void onRefresh() {
        mUserDidRefresh = true;
        if(!mPageIsLoading) loadPage();
    }

    @Override
    public void onClick(Widget widget) {
        if(!widget.linkedPage.id.isEmpty() && !mPageIsLoading) {
            mDidStartOtherActivity = true;
            PageActivity.start(this, mSitemapName, widget.linkedPage.id);
        }
    }
}
