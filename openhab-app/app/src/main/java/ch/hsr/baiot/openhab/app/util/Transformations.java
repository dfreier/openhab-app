package ch.hsr.baiot.openhab.app.util;

import java.util.List;

import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.model.Widget;
import rx.Observable;

/**
 * Created by dominik on 21.05.15.
 */
public class Transformations {



    public static Observable<Widget> flatten(Widget widget) {
        if (widget.type.equals("Frame")) {
            return Observable.from(widget.widget).startWith(widget);
        } else {
            return Observable.just(widget);
        }
    }
}
