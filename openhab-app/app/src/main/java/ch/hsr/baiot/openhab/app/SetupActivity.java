package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ch.hsr.baiot.openhab.R;

public class SetupActivity extends Activity {

    public static void start(Activity currentActivity) {
        Intent intent = new Intent(currentActivity, SetupActivity.class);
        currentActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }


}
