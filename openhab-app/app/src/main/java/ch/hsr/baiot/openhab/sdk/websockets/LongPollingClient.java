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

import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by dominik on 20.05.15.
 */
public class LongPollingClient implements SocketClient {


    private OkHttpClient mOkHttpClient;
    private Map<String, Call> mPageCalls;
    private static String mTrackingId;

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
                .addHeader("X-Atmosphere-tracking-id", mTrackingId == null ? "0" : mTrackingId)
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

                mTrackingId = response.header("X-Atmosphere-tracking-id", mTrackingId);
                if(!mPageCalls.containsKey(pageId)) return;
                mPageCalls.remove(pageId);

                try {
                    if(response.isSuccessful()) {

                        Gson gson = OpenHab.sdk().getGsonBuilder().create();
                        Page page = gson.fromJson(response.body().charStream(), Page.class);
                        if(page == null) {
                            Log.d("test", "empty update, " + pageId);
                            throw new SocketResponseEmptyException();
                        } else {
                            Log.d("test", "data update, " + pageId);
                            subject.onNext(page);
                            subject.onCompleted();
                        }

                    }
                } catch(Exception e) {
                    subject.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Page> open(String sitemap, String pageId) {
        final PublishSubject<Page> subject = PublishSubject.create();


        cancelPageCall(pageId);
        Request request = createPollingRequest( OpenHab.sdk().getEndpoint() + "/rest/sitemaps/"
                + sitemap + "/"
                + pageId + "?type=json",
                pageId);
        pollPage(pageId, request, subject);


        // IMPORTANT: return the observable that is returned from "doOnUnsubscribe".
        // If the subject is returned instead, "doOnUnsubscribe" won't be called!!!!
        return subject.doOnUnsubscribe(() -> {
            cancelPageCall(pageId);
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
