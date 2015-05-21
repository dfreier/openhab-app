package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import ch.hsr.baiot.openhab.sdk.websockets.SocketResponseEmptyException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PageActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener,
        WidgetListAdapter.OnWidgetListClickListener{

    private static final String ARG_SITEMAP = "argSitemap";
    private static final String ARG_PAGE = "argPage";
    private static final String ARG_TITLE = "argTitle";

    private String mSitemapName;
    private String mPageId;
    private String mPageTitle;
    private WidgetListModel mWidgetListModel;
    private Page mPage;
    private List<Widget> mWidgets;

    @InjectView(R.id.recycler_view) RecyclerView mWidgetListView;
    private RecyclerView.LayoutManager mLayoutManager;
    @InjectView(R.id.refresh_layout) SwipeRefreshLayout mRefreshLayout;

    private Subscription mLoadPageSubscription;
    private Subscription mPageUpdateSubscription;
    private Observable<List<Widget>> mPageSocket;

    private boolean mPageIsLoading = false;
    private boolean mDidPauseWhileLoading = false;
    private boolean mDidStartOtherActivity = false;
    private boolean mUserDidRefresh = false;



    public static void start(Activity currentActivity, String sitemap, String page, String title) {
        Intent intent = new Intent(currentActivity, PageActivity.class);
        Bundle args = new Bundle();
        args.putString(ARG_SITEMAP, sitemap);
        args.putString(ARG_PAGE, page);
        args.putString(ARG_TITLE, title);
        intent.putExtras(args);
        currentActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assignArgumentsFromIntent();
        prepareUserInterface();
    }

    private void prepareUserInterface() {
        setContentView(R.layout.activity_page);
        ButterKnife.inject(this);

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.primary,
                R.color.primary_dark
        );
        mLayoutManager = new LinearLayoutManager(this);
        mWidgetListView.setLayoutManager(mLayoutManager);
        getActionBar().setTitle(mPageTitle);
    }

    private void assignArgumentsFromIntent() {
        Bundle args = getIntent().getExtras();
        if(args != null) {
            mSitemapName = args.getString(ARG_SITEMAP);
            mPageId = args.getString(ARG_PAGE);
            mPageTitle = args.getString(ARG_TITLE);
        }
    }

    private void startInitialPageLoad() {
        if(mSitemapName != null && mPageId != null) {
            loadPage();
            subscribeToPageUpdates();
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
        startInitialPageLoad();
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
        if(mLoadPageSubscription != null) {
            mLoadPageSubscription.unsubscribe();
        }
        Log.d("test", "load, " + mPageId);
        setPageIsLoading(true);
        mLoadPageSubscription = OpenHabSdk.getOpenHabApi().getPage(mSitemapName, mPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(page -> Observable.from(page.widget))
                .flatMap(widget -> {
                   if (widget.type.equals("Frame")) {
                          return Observable.from(widget.widget).startWith(widget);
                      } else {
                          return Observable.just(widget);
                      }
                 })
                .toList()
                .subscribe(new Subscriber<List<Widget>>() {
                    @Override
                    public void onCompleted() {
                        onLoadPageComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadPageError();
                    }

                    @Override
                    public void onNext(List<Widget> widgets) {
                        mWidgets = widgets;
                    }
                });
    }

    private void subscribeToPageUpdates() {
        if(mPageUpdateSubscription != null) {
            mPageUpdateSubscription.unsubscribe();
        }
        if(mPageSocket == null) {
            mPageSocket = OpenHabSdk.getSocketClient().open(mSitemapName, mPageId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(page -> Observable.from(page.widget))
                    .flatMap(widget -> {
                        if (widget.type.equals("Frame")) {
                            return Observable.from(widget.widget).startWith(widget);
                        } else {
                            return Observable.just(widget);
                        }
                    })
                    .toList();
        }

        mPageUpdateSubscription = mPageSocket.subscribe(new Subscriber<List<Widget>>() {
                    @Override
                    public void onCompleted() {
                        //subscribeToPageUpdates();
                        //loadPage();
                    }

                    @Override
                    public void onError(Throwable e) {

                        if(e instanceof SocketTimeoutException) {
                            subscribeToPageUpdates();
                        } else if(e instanceof SocketResponseEmptyException) {
                            loadPage();
                        }
                    }

                    @Override
                    public void onNext(List<Widget> widgets) {
                        //  mPage = page;
                        mWidgets = widgets;
                        updateWidgetListModel(widgets);
                    }
                });
    }

    private void onLoadPageComplete() {
        if(mWidgetListModel == null || mUserDidRefresh) {
            createWidgetListModel();
            assignWidgetListModelToAdapter();
        }
        updateWidgetListModel(mWidgets);
        setPageIsLoading(false);
    }


    private void onLoadPageError() {
        setPageIsLoading(false);
        Toast.makeText(getApplicationContext(), "Cannot refresh page "
                + mPageId, Toast.LENGTH_SHORT).show();
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
            PageActivity.start(this, mSitemapName, widget.linkedPage.id, widget.linkedPage.title);
        }
    }
}
