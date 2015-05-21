package ch.hsr.baiot.openhab.sdk.websockets;

import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;

/**
 * Created by dominik on 20.05.15.
 */
public interface SocketClient {

    public Observable<Page> open(String sitemap, String pageId);
}
