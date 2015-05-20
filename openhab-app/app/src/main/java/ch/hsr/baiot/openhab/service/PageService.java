package ch.hsr.baiot.openhab.service;

import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by dominik on 19.05.15.
 */
public class PageService {

    private OpenHabApi mApi;
    private Subject<Page, Page> subject = new SerializedSubject<>(ReplaySubject.create());
    private Subscription mLoadSubscription;
    private Subscription mPollSubscription;

    public PageService(OpenHabApi api) {
        mApi = api;
    }

    public Observable<Page> observePage(String sitemap, String page) {


        loadSitemap(sitemap, page);
        subject.doOnUnsubscribe(() -> {
            close();
        });


        return subject;
    }

    public void close() {
        if(mPollSubscription != null) {
            mPollSubscription.unsubscribe();
        }
        if(mLoadSubscription != null) {
            mLoadSubscription.unsubscribe();
        }
    }

    private void loadSitemap(String sitemap, String page) {
        if(mLoadSubscription != null) {
            mLoadSubscription.unsubscribe();
        }
        mLoadSubscription = mApi.getPage(sitemap, page)
                .subscribe(p -> {
                    subject.onNext(p);
                    pollSitemap(sitemap, page);
                });
    }

    private void pollSitemap(String sitemap, String page) {
        if(mPollSubscription != null) {
            mPollSubscription.unsubscribe();
        }
        mPollSubscription = mApi.subscribePage(sitemap, page)
                .subscribe(p -> {
                    loadSitemap(sitemap, page);
                });
    }

}
