package ch.hsr.baiot.openhab.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.clans.fab.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import ch.hsr.baiot.openhab.sdk.model.Sitemap;
import ch.hsr.baiot.openhab.sdk.model.WidgetListModel;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;


public class MainActivity extends Activity {

    private OpenHabApi mApi;
    private WidgetListModel mWidgetListModel;
    private Subscription mPageSubscription;
    private boolean isReloading = false;

    @InjectView(R.id.button_setup)
    Button mSetupButton;

    @InjectView(R.id.button_start)
    FloatingActionButton mStartButton;


    private Subscription mIsAvailableSubscription;

    private static final int SETUP_ACTIVITY_RESULT = 0;
    private Subscription mLoadHomepageSubscription;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.inject(this);
        OpenHab.initialize(this);

        String endpoint =  OpenHab.sdk().getEndpoint();
        String sitemap = OpenHab.sdk().getSitemap();

        if(endpoint.isEmpty() || sitemap.isEmpty()) {
            SetupActivity.start(this, true, SETUP_ACTIVITY_RESULT);
        } else {
            checkIfAvailable(sitemap);
        }

    }



    private void checkIfAvailable(String sitemap) {
        unsubscribeAll();
        mIsAvailableSubscription = OpenHab.sdk().isSitemapAvailable(OpenHab.sdk().getEndpoint(), sitemap)
                .subscribe(isAvailable -> {
                    if (!isAvailable) showConfigErrorDialog();

                });
    }



    @Override
    protected void onPause() {
        super.onPause();

    }

    private void unsubscribeAll() {
        if(mLoadHomepageSubscription != null) {
            mLoadHomepageSubscription.unsubscribe();
        }
        if(mIsAvailableSubscription != null) {
            mIsAvailableSubscription.unsubscribe();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETUP_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {


                String endpoint = data.getStringExtra(SetupActivity.RESULT_ENDPOINT);
                String sitemap = data.getStringExtra(SetupActivity.RESULT_SITEMAP);
                OpenHab.sdk().setEndpoint(endpoint);
                OpenHab.sdk().setSitemap(sitemap);

                checkIfAvailable(sitemap);
            }
        }
    }

    private void loadHomepage(String sitemap) {
        final Activity self = this;
        unsubscribeAll();
        mLoadHomepageSubscription = OpenHab.sdk().isSitemapAvailable(OpenHab.sdk().getEndpoint(), sitemap)
                .flatMap(isAvailable -> {
                   return isAvailable ? OpenHab.sdk().getApi().getSitemap(sitemap) : getThrowingSubject(new Exception());
                })
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

    private Observable<Sitemap>  getThrowingSubject(Throwable e) {
        Subject<Sitemap, Sitemap> subject = PublishSubject.create();
        return subject.doOnSubscribe(() -> {
            subject.onError(e);
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
        builder.create().show();
    }

    @OnClick(R.id.button_setup)
    public void onClick(View view) {
        SetupActivity.start(this, false, SETUP_ACTIVITY_RESULT);
    }

    @OnClick(R.id.button_start)
    public void onStartClick(View view) {
        loadHomepage(OpenHab.sdk().getSitemap());
    }




}
