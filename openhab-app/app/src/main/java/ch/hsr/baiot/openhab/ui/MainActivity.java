package ch.hsr.baiot.openhab.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.model.ObjectAsArrayDeserializer;
import ch.hsr.baiot.openhab.model.Page;
import ch.hsr.baiot.openhab.model.Sitemap;
import ch.hsr.baiot.openhab.model.Widget;
import ch.hsr.baiot.openhab.model.WidgetModel;
import ch.hsr.baiot.openhab.service.PageService;
import ch.hsr.baiot.openhab.service.api.OpenHabApi;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private PageFragment mPageFragment;
    private PageService mService;
    private OpenHabApi mApi;
    private WidgetModel mWidgetModel;
    private Subscription mPageSubscription;
    private boolean isReloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageFragment = new PageFragment();
        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .add(R.id.container, mPageFragment)
                    .commit();
        }
        Log.d("test", "started");


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Widget[].class, new ObjectAsArrayDeserializer<Widget>(Widget.class))
                .registerTypeAdapter(Sitemap[].class, new ObjectAsArrayDeserializer<Sitemap>(Sitemap.class))
                .create();

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://demo.openhab.org:8080")
                .setConverter(new GsonConverter(gson))
                .build();


        mPageFragment.setRefreshListener(this);
        mApi = adapter.create(OpenHabApi.class);


        startPageService();



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

    private void startPageService() {
        isReloading = true;
        if(mPageSubscription != null) {
            mPageSubscription.unsubscribe();
        }
        if(mService != null) {
            mService.close();
        }

        mWidgetModel = new WidgetModel();
        mPageFragment.setPageModel(mWidgetModel);
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
                        mWidgetModel.setWidgets(Arrays.asList(page.widget));
                    }
                });
    }


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
        startPageService();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_items, container, false);
            return rootView;
        }
    }
}
