package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.OpenHab;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SetupActivity extends ActionBarActivity {


    private final static String ARG_FROM_START = "fromStart";

    private boolean mFromStart;

    @InjectView(R.id.checkbox)
    CheckBox mCheckBox;

    @InjectView(R.id.url_input)
    MaterialEditText mUrlInput;

    @InjectView(R.id.sitemap_input)
    MaterialEditText mSitemapInput;

    @InjectView(R.id.connection_button)
    Button mButton;

    public static void start(Activity currentActivity, boolean fromStart) {
        Intent intent = new Intent(currentActivity, SetupActivity.class);
        Bundle args = new Bundle();
        args.putBoolean(ARG_FROM_START, fromStart);
        intent.putExtras(args);
        currentActivity.startActivity(intent);
    }

    private void assignArgumentsFromIntent() {
        Bundle args = getIntent().getExtras();
        if(args != null) {
            mFromStart = args.getBoolean(ARG_FROM_START);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        assignArgumentsFromIntent();
        getSupportActionBar().setTitle("Einrichten");
        ButterKnife.inject(this);


    }

    @OnClick(R.id.checkbox)
    public void onClickCheckbox(View view) {
        findViewById(R.id.activity_setup).requestFocus();
    }

    @OnClick(R.id.connection_button)
    public void onClickButton(View view) {
        if(validateInput()) {

            OpenHab.skd().isOpenHabAvailable(mUrlInput.getText().toString())
                    .subscribe(isAvailable -> setUrlAvailableTick(isAvailable));

            OpenHab.skd().isSitemapAvailable(mUrlInput.getText().toString(),
                    mSitemapInput.getText().toString())
                    .subscribe(isAvailable -> setSitemapAvailableTick(isAvailable));


            //mUrlInput.setFloatingLabelText("OpenHAB Url available");
            //mSitemapInput.setFloatingLabelText("Sitemap found");

            /*mUrlInput.setBaseColor(getResources().getColor(R.color.teal_500));
            mUrlInput.setTextColor(getResources().getColor(R.color.teal_500));
            mUrlInput.setPrimaryColor(getResources().getColor(R.color.teal_500));
           // mUrlInput.setHelperTextAlwaysShown(true);
           // mUrlInput.setFloatingLabelAlwaysShown(true);


            mSitemapInput.setBaseColor(getResources().getColor(R.color.teal_500));
            mSitemapInput.setTextColor(getResources().getColor(R.color.teal_500));
            mSitemapInput.setPrimaryColor(getResources().getColor(R.color.teal_500));
           // mSitemapInput.setHelperTextAlwaysShown(true);
            //mSitemapInput.setFloatingLabelAlwaysShown(true);
            */
        }
        findViewById(R.id.activity_setup).requestFocus();
    }

    private void setUrlAvailableTick(boolean isAvailable) {

    }

    private void setSitemapAvailableTick(boolean isAvailable) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void save() {
        if(validateInput()) {
            String sitemap = mSitemapInput.getText().toString();
            String url = mUrlInput.getText().toString();

            OpenHab.skd().setEndpoint(url);
            OpenHab.skd().setSitemap(sitemap);
            finish();
            if(!mFromStart) {
                OpenHab.skd().getApi().getSitemap(sitemap)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(s -> {
                            PageActivity.start(this,sitemap,s.homepage.id, "Demo" );
                        });
            }
        }
    }


    private boolean validateInput() {
        boolean isValid = true;

        String urlPattern = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        if(!mUrlInput.validate(urlPattern, "Ungültige Url")) isValid = false;

        String notEmptyPattern = "^(?=\\s*\\S).*$";
        if(!mSitemapInput.validate(notEmptyPattern, "Erforderlich")) isValid = false;

        return isValid;
    }


}
