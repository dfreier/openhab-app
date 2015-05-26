package ch.hsr.baiot.openhab.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import rx.Subscription;


public class MainActivity extends ActionBarActivity {

    private OpenHabApi mApi;
    private WidgetListModel mWidgetListModel;
    private Subscription mPageSubscription;
    private boolean isReloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenHab.initialize(this);
        String endpoint =  OpenHab.skd().getEndpoint();
        if(endpoint.isEmpty()) {
            SetupActivity.start(this, true);
        } else {
            PageActivity.start(this, "demo", "demo", "Demo House");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
