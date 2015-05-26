package ch.hsr.baiot.openhab.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ch.hsr.baiot.openhab.R;
import ch.hsr.baiot.openhab.sdk.OpenHab;

public class SetupActivity extends ActionBarActivity {

    public final static String RESULT_ENDPOINT = "resultEndpoint";
    public final static String RESULT_SITEMAP = "resultSitemap";
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

    @InjectView(R.id.icon_tick_url)
    ImageView mTickUrl;

    @InjectView(R.id.icon_tick_sitemap)
    ImageView mTickSitemap;

    @InjectView(R.id.progress_bar_url)
    ProgressBar mProgressUrl;

    @InjectView(R.id.progress_bar_sitemap)
    ProgressBar mProgressSitemap;

    public static void start(Activity currentActivity, boolean fromStart, int requestCode) {
        Intent intent = new Intent(currentActivity, SetupActivity.class);
        Bundle args = new Bundle();
        args.putBoolean(ARG_FROM_START, fromStart);
        intent.putExtras(args);
        currentActivity.startActivityForResult(intent, requestCode);
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

        mUrlInput.setText(OpenHab.sdk().getEndpoint());
        mSitemapInput.setText(OpenHab.sdk().getSitemap());


        hideIndicators();


    }

    private void hideIndicators() {
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_url), new Fade());
        mTickUrl.setVisibility(View.GONE);
        mProgressUrl.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_sitemap), new Fade());
        mTickSitemap.setVisibility(View.GONE);
        mProgressSitemap.setVisibility(View.GONE);
    }

    @OnClick(R.id.checkbox)
    public void onClickCheckbox(View view) {
        findViewById(R.id.activity_setup).requestFocus();
    }

    @OnClick(R.id.connection_button)
    public void onClickButton(View view) {
        if(validateInput()) {
            checkAvailability();
        } else {
            hideIndicators();
        }
        findViewById(R.id.activity_setup).requestFocus();
    }

    private void checkAvailability() {
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_url), new Fade());
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_sitemap), new Fade());
        mTickUrl.setVisibility(View.GONE);
        mProgressUrl.setVisibility(View.VISIBLE);
        mTickSitemap.setVisibility(View.GONE);
        mProgressSitemap.setVisibility(View.VISIBLE);

        OpenHab.sdk().isOpenHabAvailable(mUrlInput.getText().toString())
                .subscribe(isAvailable -> setUrlAvailableTick(isAvailable));


        OpenHab.sdk().isSitemapAvailable(mUrlInput.getText().toString(),
                mSitemapInput.getText().toString())
                .subscribe(isAvailable -> setSitemapAvailableTick(isAvailable));
    }

    private void setUrlAvailableTick(boolean isAvailable) {
        mTickUrl.setImageResource(isAvailable ? R.drawable.ic_check_circle_cyan : R.drawable.ic_error_outline_red);
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_url), new Fade());
        mTickUrl.setVisibility(View.VISIBLE);
        mProgressUrl.setVisibility(View.GONE);
    }

    private void setSitemapAvailableTick(boolean isAvailable) {
        mTickSitemap.setImageResource(isAvailable ? R.drawable.ic_check_circle_cyan : R.drawable.ic_error_outline_red);
        TransitionManager.beginDelayedTransition((ViewGroup)findViewById(R.id.container_sitemap), new Fade());
        mTickSitemap.setVisibility(View.VISIBLE);
        mProgressSitemap.setVisibility(View.GONE);
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

            Intent resultData = new Intent();
            resultData.putExtra(RESULT_ENDPOINT, url);
            resultData.putExtra(RESULT_SITEMAP, sitemap);
            setResult(Activity.RESULT_OK, resultData);
            finish();
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
