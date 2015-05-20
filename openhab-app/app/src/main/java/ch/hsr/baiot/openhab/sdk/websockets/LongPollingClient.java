package ch.hsr.baiot.openhab.sdk.websockets;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ch.hsr.baiot.openhab.sdk.OpenHabSdk;
import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by dominik on 20.05.15.
 */
public class LongPollingClient implements PushClient{


    private String mTrackingId;
    private OkHttpClient mOkHttpClient;
    private Map<String, Call> mPageCalls;

    public LongPollingClient() {
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        mPageCalls = new HashMap<>();
    }

    private Request createPollingRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("X-Atmosphere-Framework", "1.0")
                .addHeader("X-Atmosphere-tracking-id", mTrackingId == null ? "0" : mTrackingId)
                .addHeader("Accept", "application/json")
                .addHeader("X-Atmosphere-Transport", "long-polling")
                .build();
    }

    private void pollPage (final String pageId, final Request request, final PublishSubject<Page> subject) {
        Call call = mOkHttpClient.newCall(request);
        mPageCalls.put(pageId, call);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(!e.getMessage().equals("Canceled")) {
                    subject.onError(e);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                mTrackingId = response.header("X-Atmosphere-tracking-id", "0");
                if(response.isSuccessful()) {
                    Gson gson = OpenHabSdk.getGsonBuilder().create();
                    Page page = gson.fromJson(response.body().charStream(), Page.class);
                    mPageCalls.remove(pageId);
                    if(page != null) {
                        subject.onNext(page);
                        pollPage(pageId, request, subject);
                    } else {
                        subject.onError(new Exception("Cannot parse response"));
                    }
                }
            }
        });
    }

    @Override
    public Observable<Page> subscribe(String sitemap, Page page) {
        final PublishSubject<Page> subject = PublishSubject.create();

        cancelPageCall(page.id);
        Request request = createPollingRequest("http://demo.openhab.org:8080/rest/sitemaps/" + sitemap + "/" + page.id + "?type=json");
        pollPage(page.id, request, subject);

        return subject
                .doOnUnsubscribe(() -> cancelPageCall(page.id))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void cancelPageCall(String pageId) {
        if(mPageCalls.containsKey(pageId)) {
            mPageCalls.get(pageId).cancel();
            mPageCalls.remove(pageId);
        }
    }
}
