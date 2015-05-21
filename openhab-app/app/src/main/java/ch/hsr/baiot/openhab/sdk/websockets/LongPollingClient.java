package ch.hsr.baiot.openhab.sdk.websockets;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by dominik on 20.05.15.
 */
public class LongPollingClient implements SocketClient {


    private OkHttpClient mOkHttpClient;
    private Map<String, Call> mPageCalls;

    public LongPollingClient() {
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
        mPageCalls = new HashMap<>();
    }

    private Request createPollingRequest(String url, String pageId) {
        return new Request.Builder()
                .url(url)
                .addHeader("X-Atmosphere-Framework", "1.0")
             //   .addHeader("X-Atmosphere-tracking-id", "0")
                .addHeader("Accept", "application/json")
                .addHeader("X-Atmosphere-Transport", "long-polling")
                .tag(pageId)
                .build();
    }

    private void pollPage (final String pageId, final Request request, final PublishSubject<Page> subject) {

        Call call = mOkHttpClient.newCall(request);
        mPageCalls.put(pageId, call);
        Log.d("test", "poll, " + pageId);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(!mPageCalls.containsKey(pageId)) return;
                mPageCalls.remove(pageId);

                if(!"Canceled".equals(e.getMessage())) {
                    Log.d("test", "timeout, " + pageId);
                    subject.onError(e);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {

              //  OpenHabSdk.AtmosphereTrackingId = response.header("X-Atmosphere-tracking-id",
                //        OpenHabSdk.AtmosphereTrackingId);
                if(!mPageCalls.containsKey(pageId)) return;
                mPageCalls.remove(pageId);

                if(response.isSuccessful()) {

                    Gson gson = OpenHabSdk.getGsonBuilder().create();
                    Page page = gson.fromJson(response.body().charStream(), Page.class);
                    if(page == null) {
                        Log.d("test", "empty update, " + pageId);
                        subject.onError(new SocketResponseEmptyException());
                    } else {
                        Log.d("test", "data update, " + pageId);
                        subject.onNext(page);
                        subject.onCompleted();
                    }

                }
            }
        });
    }

    @Override
    public Observable<Page> open(String sitemap, String pageId) {
        final PublishSubject<Page> subject = PublishSubject.create();


        cancelPageCall(pageId);
        Request request = createPollingRequest("http://demo.openhab.org:8080/rest/sitemaps/"
                + sitemap + "/"
                + pageId + "?type=json",
                pageId);
        pollPage(pageId, request, subject);


        // IMPORTANT: return the observable that is returned from "doOnUnsubscribe".
        // If the subject is returned instead, "doOnUnsubscribe" won't be called!!!!
        return subject.doOnUnsubscribe(() -> {
            //Log.d("test", "unsubscribe, " + pageId);
            cancelPageCall(pageId);
        }).doOnSubscribe(() -> {
           // Log.d("test", "subscribe, " + pageId);
        });
    }

    private void cancelPageCall(String pageId) {
        if(mPageCalls.containsKey(pageId)) {
            mPageCalls.get(pageId).cancel();
            mPageCalls.remove(pageId);
        }
        mOkHttpClient.cancel(pageId);
    }
}
