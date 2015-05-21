package ch.hsr.baiot.openhab.sdk;

import com.google.gson.GsonBuilder;

import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import ch.hsr.baiot.openhab.sdk.model.Sitemap;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.util.ObjectAsArrayDeserializer;
import ch.hsr.baiot.openhab.sdk.websockets.LongPollingClient;
import ch.hsr.baiot.openhab.sdk.websockets.SocketClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by dominik on 20.05.15.
 */
public class OpenHabSdk {

    private static OpenHabApi mOpenHabApi;
    private static SocketClient mSocketClient;
    private static GsonBuilder mGsonBuilder;

    public static String AtmosphereTrackingId;

    public static void initialize() {
        mGsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Widget[].class, new ObjectAsArrayDeserializer<Widget>(Widget.class))
                .registerTypeAdapter(Sitemap[].class, new ObjectAsArrayDeserializer<Sitemap>(Sitemap.class));


        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://demo.openhab.org:8080")
                .setConverter(new GsonConverter(mGsonBuilder.create()))
                .build();

        mOpenHabApi = adapter.create(OpenHabApi.class);
        mSocketClient = new LongPollingClient();

    }

    public static OpenHabApi getOpenHabApi() {
        return mOpenHabApi;
    }
    public static SocketClient getSocketClient() {return mSocketClient;}
    public static GsonBuilder getGsonBuilder() { return mGsonBuilder; }
}
