package ch.hsr.baiot.openhab.app.widget;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

import ch.hsr.baiot.openhab.R;

/**
 * Created by dominik on 02.06.15.
 */
public class WidgetWebview extends WidgetViewHolder {


    public WebView webView;

    public WidgetWebview(ViewGroup container) {
        super(container);
        webView = (WebView) container.findViewById(R.id.webview);
    }

}
