package ch.hsr.baiot.openhab.sdk;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import ch.hsr.baiot.openhab.sdk.model.Sitemap;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.storage.Settings;
import ch.hsr.baiot.openhab.sdk.util.ObjectAsArrayDeserializer;
import ch.hsr.baiot.openhab.sdk.websockets.LongPollingClient;
import ch.hsr.baiot.openhab.sdk.websockets.SocketClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dominik on 20.05.15.
 */
public class OpenHab {

    private static OpenHab mSdk;

    private Context mContext;
    private OpenHabApi mOpenHabApi;
    private SocketClient mSocket;
    private GsonBuilder mGsonBuilder;
    private Settings mSettings;


    public static OpenHab initialize(Context context) {
        if(mSdk == null) {
            mSdk = new OpenHab(context);
        }
        return mSdk;
    }


    public static OpenHab skd() {
        return mSdk;
    }


    private OpenHab(Context context) {
        mContext = context;
        mSettings = new Settings(PreferenceManager.getDefaultSharedPreferences(context));

        createGsonBuilder();

        mSocket = new LongPollingClient();
    }

    private void createGsonBuilder() {
        mGsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Widget[].class, new ObjectAsArrayDeserializer<Widget>(Widget.class))
                .registerTypeAdapter(Sitemap[].class, new ObjectAsArrayDeserializer<Sitemap>(Sitemap.class));
    }



    private OpenHabApi createApi(String endpoint, Gson gson) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(new GsonConverter(gson))
                .build();
        return adapter.create(OpenHabApi.class);
    }


    public void setEndpoint(String endpoint) {
        if(endpoint == null) return;
        if(!endpoint.equals(getEndpoint())) {
            mOpenHabApi = createApi(endpoint, mGsonBuilder.create());
        }
        mSettings.setEndpoint(endpoint);
    }

    public String getEndpoint() {
        return mSettings.getEndpoint();
    }

    public void setSitemap(String sitemap) {
        if(sitemap == null) return;
        mSettings.setSitemap(sitemap);
    }

    public String getSitemap() {
        return mSettings.getSitemap();
    }

    public OpenHabApi getApi() {
        if(mOpenHabApi  == null) mOpenHabApi = createApi(getEndpoint(), mGsonBuilder.create());
        return mOpenHabApi;
    }

    public SocketClient getSocketClient() {
        return mSocket;
    }

    public GsonBuilder getGsonBuilder() {
        return mGsonBuilder;
    }


    public Observable<Boolean> isOpenHabAvailable(String url) {
        OpenHabApi api = createApi(url, mGsonBuilder.create());
        return api.getAllSitemaps()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(holder -> holder != null)
                .onErrorReturn(isAvailable -> false);
    }

    public Observable<Boolean> isSitemapAvailable(String url, String sitemap) {
        OpenHabApi api = createApi(url, mGsonBuilder.create());
        return api.getAllSitemaps()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(holder -> {
                    for(int i = 0; i < holder.sitemap.length; i++) {
                        if(holder.sitemap[i].name.equals(sitemap)) {
                            return true;
                        }
                    }
                    return false;
                })
                .onErrorReturn(hasSitemap -> false);
    }

}
