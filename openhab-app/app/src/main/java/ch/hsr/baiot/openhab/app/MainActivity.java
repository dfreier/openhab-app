package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.fragment.PageFragment;
import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import ch.hsr.baiot.openhab.service.PageService;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import rx.Subscription;


public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, PageFragment.PageFragmentListener {

    private PageFragment mPageFragment;
    private PageService mService;
    private OpenHabApi mApi;
    private WidgetListModel mWidgetListModel;
    private Subscription mPageSubscription;
    private boolean isReloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenHabSdk.initialize();

        PageActivity.start(this, "demo", "0000");

       /* mPageFragment = PageFragment.newInstance("demo", "0000");
        mPageFragment.setPageFragmentListener(this);
        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .add(R.id.container, mPageFragment)
                    .commit();
        }*/





        //startPageService();



       /* final List<Widget> widgets = new LinkedList<>();
        service.getPage("demo", "FF_Bath")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(page -> mPageFragment.setPage(page));*/

/*        service.getAllSitemaps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(sitemaps -> Observable.from(sitemaps.sitemap))
                .first()
                .flatMap(sitemap -> service.getSitemap(sitemap.name))
                .flatMap(sitemap -> service.getPage(sitemap.name, sitemap.homepage.id))
                .flatMap(homepage -> Observable.from(homepage.widget))
                .flatMap(widget -> {
                    if (widget.type.equals("Frame")) {
                        return Observable.from(widget.widget).startWith(widget);
                    } else {
                        return Observable.from(widget.widget);
                    }
                })
                .subscribe(widget -> Log.d("test", widget.type + " | " + widget.label + " | " + widget.widgetId));*/

        /*service.subscribePage("demo", "FF_Bath")
                .flatMap(page -> Observable.from(page.widget))
                .flatMap(widget -> Observable.just(widget.item))
                .subscribe(new Subscriber<Item>() {
                    @Override
                    public void onCompleted() {
                        Log.d("test","complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("test", e.getMessage());
                    }

                    @Override
                    public void onNext(Item item) {
                        Log.d("test", item.name + ": " + item.state);
                    }
                });*/

    }

   /* private void startPageService() {
        isReloading = true;
        if(mPageSubscription != null) {
            mPageSubscription.unsubscribe();
        }
        if(mService != null) {
            mService.close();
        }

        mWidgetListModel = new WidgetListModel();
        mPageFragment.setPageModel(mWidgetListModel);
        mService = new PageService(mApi);
        mPageSubscription = mService.observePage("demo", "FF_Bath")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Page>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Page page) {
                        mPageFragment.setRefreshing(false);
                        mWidgetListModel.setWidgets(Arrays.asList(page.widget));
                    }
                });
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
       // startPageService();
    }

    @Override
    public void onNavigateTo(String sitemap, String page) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        PageFragment fragment = PageFragment.newInstance(sitemap, page);
        transaction.add(R.id.container, fragment).addToBackStack("");
        transaction.commit();
    }


}
