package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import ch.hsr.baiot.openhab.sdk.model.Sitemap;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    private OpenHabApi mApi;
    private WidgetListModel mWidgetListModel;
    private Subscription mPageSubscription;
    private boolean isReloading = false;

    private static final int SETUP_ACTIVITY_RESULT = 0;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenHab.initialize(this);

        String endpoint =  OpenHab.sdk().getEndpoint();
        String sitemap = OpenHab.sdk().getSitemap();

        if(endpoint.isEmpty() || sitemap.isEmpty()) {
            SetupActivity.start(this, true, SETUP_ACTIVITY_RESULT);
        } else {
            loadHomepage(sitemap);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETUP_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {


                String endpoint = data.getStringExtra(SetupActivity.RESULT_ENDPOINT);
                String sitemap = data.getStringExtra(SetupActivity.RESULT_SITEMAP);
                OpenHab.sdk().setEndpoint(endpoint);
                OpenHab.sdk().setSitemap(sitemap);

                loadHomepage(sitemap);
            }
        }
    }

    private void loadHomepage(String sitemap) {
        final Activity self = this;
        OpenHab.sdk().getApi().getSitemap(sitemap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Sitemap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showConfigErrorDialog();
                    }

                    @Override
                    public void onNext(Sitemap sitemap) {
                        PageActivity.start(self, sitemap.name, sitemap.homepage.id, sitemap.homepage.title);
                    }
                });
    }

    private void showConfigErrorDialog() {
        final Activity self = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("OpenHAB konnte mit den aktuellen Einstellungen nicht erreicht werden. Überprüfen Sie die Verbindungsinformationen.")
                .setTitle("Verbindungsproblem")
                .setPositiveButton("Überprüfen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SetupActivity.start(self, false, SETUP_ACTIVITY_RESULT);
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
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
