package us.dangeru.launcher.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import us.dangeru.launcher.R;
import us.dangeru.launcher.activities.HiddenSettingsActivity;
import us.dangeru.launcher.utils.PropertiesSingleton;

/**
 * Created by Niles on 8/20/17.
 */

public class JanitorLoginFragment extends Fragment implements  HiddenSettingsFragment {
    @SuppressWarnings("unused")
    private static final String TAG = JanitorLoginFragment.class.getSimpleName();
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View res = inflater.inflate(R.layout.janitor_login, container, false);
        res.post(new Runnable() {
            @Override
            public void run() {
                EditText username = res.findViewById(R.id.username);
                EditText password = res.findViewById(R.id.password);
                username.setText(PropertiesSingleton.get().getProperty("username"));
                password.setText(PropertiesSingleton.get().getProperty("password"));
                res.findViewById(R.id.button).setOnClickListener(new LoginButtonClickListener());
            }
        });
        return res;
    }
    private class LoginButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(final View view) {
            getPropertiesFromView();
            GenericProgressDialogFragment.newInstance("Logging in...", getFragmentManager());
            view.findViewById(R.id.button).setClickable(false);
            // can't do network on ui thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        authenticate();
                    } catch (Exception e) {
                        GenericAlertDialogFragment.newInstance("Unexpected error " + e, getFragmentManager());
                    }
                    view.findViewById(R.id.button).setClickable(true);
                    GenericProgressDialogFragment.dismiss(getFragmentManager());
                }
            }).start();
        }
    }
    private void authenticate() throws Exception {
        String username = PropertiesSingleton.get().getProperty("username");
        URL uri = new URL(PropertiesSingleton.get().getProperty("awoo_endpoint") + "/mod");
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        String data = "username=" + URLEncoder.encode(username, "UTF-8");
        data += "&password=" + URLEncoder.encode(PropertiesSingleton.get().getProperty("password"), "UTF-8");
        //data += "&redirect=" + URLEncoder.encode("file:///android_res/success.html", "UTF-8");
        os.write(data.getBytes());
        os.flush(); os.close();
        int responseCode = connection.getResponseCode();
        PropertiesSingleton.get().setProperty("logged_in", responseCode == 200 ? "true" : "false");
        if (responseCode == 403) {
            GenericAlertDialogFragment.newInstance("Error - Check your username and password", getFragmentManager());
        } else if (responseCode == 200) {
            GenericAlertDialogFragment.newInstance("Success! You are logged in as " + username, getFragmentManager());
        } else {
            GenericAlertDialogFragment.newInstance("Unexpected response code " + responseCode, getFragmentManager());
        }

    }
    public void getPropertiesFromView() {
        if (getView() == null) return;
        EditText username = getView().findViewById(R.id.username);
        EditText password = getView().findViewById(R.id.password);
        PropertiesSingleton.get().setProperty("username", username.getText().toString());
        PropertiesSingleton.get().setProperty("password", password.getText().toString());
    }
    /*
    @Override public void finish() {
        getPropertiesFromView();
        super.finish();
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            boolean logged_in = "true".equalsIgnoreCase(PropertiesSingleton.get().getProperty("logged_in"));
            if (logged_in) {
                GenericAlertDialogFragment.newInstance("Success! You are now logged in as " + PropertiesSingleton.get().getProperty("username"), getFragmentManager());
            } else {
                GenericAlertDialogFragment.newInstance("Error - Check your username and password?", getFragmentManager());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public HiddenSettingsActivity.FragmentType getType() {
        return HiddenSettingsActivity.FragmentType.JANITOR_LOGIN;
    }
}