package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.app.widget.WidgetWebview;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import ch.hsr.baiot.openhab.sdk.api.OpenHabApi;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;



public class ToggleActivity extends Activity {


    @InjectView(R.id.toggle_icon)
    ImageView mToggleIcon;

    @InjectView(R.id.text_view)
    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toggle);
        ButterKnife.inject(this);


        if (handleNfcIntent()) {
            OpenHab.initialize(this);
            toggleAlarm();
            return;
        }
    }

    private void toggleAlarm() {

        OpenHabApi api = OpenHab.sdk().getApi();

        api.getItem("Alarm_active")
                .flatMap(item -> Observable.just(item.state.equals("ON") ? "OFF" : "ON"))
                .flatMap(state -> api.sendCommand("Alarm_active",  new TypedByteArray("text/plain", state.getBytes())))
                .flatMap(aVoid -> api.getItem("Alarm_active"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(item-> {
                    mToggleIcon.setImageResource("ON".equals(item.state) ? R.drawable.ic_alarm_on : R.drawable.ic_alarm_off);
                    mToggleIcon.setVisibility(View.VISIBLE);
                    mTextView.setText(("ON".equals(item.state) ? "Scharf" : "Inaktiv"));
                    startCloseTimer();
                });
    }

    private void startCloseTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        }, 2000);
    }

    private boolean handleNfcIntent() {
        Intent intent = getIntent();

        if(intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage msg = (NdefMessage) rawMsgs[0];

            String content = new String(msg.getRecords()[0].getPayload());
            if("toggle-alarm".equals(content)) {
                return true;
            }

        }
        return false;
    }



}
