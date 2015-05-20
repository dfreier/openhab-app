package ch.hsr.baiot.openhab.sdk.websockets;

import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;

/**
 * Created by dominik on 20.05.15.
 */
public interface PushClient {

    public Observable<Page> subscribe(String sitemap, Page page);
}
