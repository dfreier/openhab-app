package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.adapter.WidgetListAdapter;
import ch.hsr.baiot.openhab.app.util.Transformations;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PageActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener,
        WidgetListAdapter.OnWidgetListClickListener{

    private static final int SETUP_ACTIVITY_RESULT = 0;
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
        currentActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(currentActivity).toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                SetupActivity.start(this, false, SETUP_ACTIVITY_RESULT);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETUP_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {

                String oldEndpoint = OpenHab.sdk().getEndpoint();
                String oldSitemap = OpenHab.sdk().getSitemap();

                String endpoint = data.getStringExtra(SetupActivity.RESULT_ENDPOINT);
                String sitemap = data.getStringExtra(SetupActivity.RESULT_SITEMAP);

                OpenHab.sdk().setEndpoint(endpoint);
                OpenHab.sdk().setSitemap(sitemap);


                if(!oldEndpoint.equals(endpoint) ||
                   !oldSitemap.equals(sitemap)) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }
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
        mWidgetListView.setItemAnimator(new FadeInRightAnimator());
        getSupportActionBar().setTitle(mPageTitle);
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

        unsubscribeFromActiveSubscriptions();
        Log.d("test", "-------------------------------------");
        Log.d("test", "load, " + mPageId);
        setPageIsLoading(true);
        mLoadPageSubscription = OpenHab.sdk().getApi().getPage(mSitemapName, mPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(page -> Observable.from(page.widget))
                .flatMap(widget -> Transformations.flatten(widget))
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
        subscribeToPageUpdates();
    }

    private void subscribeToPageUpdates() {

        mPageUpdateSubscription = OpenHab.sdk().getSocketClient().open(mSitemapName, mPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Page>() {
                    @Override
                    public void onCompleted() {
                        loadPage();
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadPage();
                    }

                    @Override
                    public void onNext(Page page) {

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
        Log.d("test", "update ui, " + mPageId);
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
        mWidgetListView.setItemAnimator(new FadeInAnimator());
        mWidgetListView.getItemAnimator().setAddDuration(800);
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
