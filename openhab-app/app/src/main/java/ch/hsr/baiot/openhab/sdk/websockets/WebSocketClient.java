package ch.hsr.baiot.openhab.sdk.websockets;

import android.os.StrictMode;
import android.util.Log;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import ch.hsr.baiot.openhab.sdk.model.Page;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by dominik on 20.05.15.
 */
public class WebSocketClient {


    public Observable<Page> subscribe(String sitemap, Page page) {

        final PublishSubject<Page> subject = PublishSubject.create();
        if(page.id.equals("0000")) return subject;

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Client client = ClientFactory.getDefault().newClient();
            RequestBuilder request = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri("http://demo.openhab.org:8080/rest/sitemaps/" + sitemap + "/" + page.id + "?type=json")
                    .header("X-Atmosphere-Framework", "1.0")
                    .header("Content-type", "application/json")
                   /* .encoder(new Encoder<String, Reader>() {        // Stream the request body
                        @Override
                        public Reader encode(String s) {
                            return new StringReader(s);
                        }
                    })
                    .decoder(new Decoder<String, Reader>() {
                        @Override
                        public Reader decode(Event type, String s) {
                            Log.d("test", "");
                            return new StringReader(s);
                        }
                    })*/
                    .transport(Request.TRANSPORT.LONG_POLLING);

            Socket socket = client.create();
            socket.on(Event.MESSAGE, new Function<Object>() {
                @Override
                public void on(final Object p) {
                    Log.d("test", p.toString());
                }
            }).on(new Function<Throwable>() {

                @Override
                public void on(Throwable t) {
                    Log.d("test", t.getMessage());
                }

            }).on(Event.CLOSE, new Function<Object>() {
                @Override
                public void on(Object o) {

                }
            }).open(request.build());

        } catch (Exception e) {
            Log.d("test", e.getMessage());
        }

        return subject.observeOn(AndroidSchedulers.mainThread());
    }

}
