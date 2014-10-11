package com.backagen.peid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;

public class LoginFragment extends Fragment {
    private static final int LOGIN_REQUEST = 0;

    private TextView titleTextView;
    private TextView emailTextView;
    private TextView nameTextView;
    private EditText nameEditText;
    private EditText emailEditText;

    private Button beginSplitButton;
    private Button updateProfileButton;
    private Button loginOrLogoutButton;

    private ParseUser currentUser;

    public interface OnLogonEventListener {
        void onLoggedInWithCompletedProfile();
        void onLoggedOut();
        void onUserClickedBeginSplitBill();
    }

    List<OnLogonEventListener> logonListeners = new ArrayList<OnLogonEventListener>();

    public LoginFragment addOnLogonListener(OnLogonEventListener listener){
        logonListeners.add(listener);
        return this;
    }

    public LoginFragment removeOnLogonListener(OnLogonEventListener listener){
        logonListeners.remove(listener);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        titleTextView = (TextView) rootView.findViewById(R.id.profile_title);
        emailTextView = (TextView) rootView.findViewById(R.id.profile_email);
        nameTextView = (TextView) rootView.findViewById(R.id.profile_name);
        nameEditText = (EditText) rootView.findViewById(R.id.login_name_input);
        emailEditText = (EditText) rootView.findViewById(R.id.login_email_input);

        beginSplitButton = (Button) rootView.findViewById(R.id.login_begin_split_btn);
        updateProfileButton = (Button) rootView.findViewById(R.id.login_update_profile_btn);
        loginOrLogoutButton = (Button) rootView.findViewById(R.id.login_or_logout_button);

        beginSplitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(OnLogonEventListener listener : logonListeners){
                    listener.onUserClickedBeginSplitBill();
                }
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateProfile();
            }
        });

        loginOrLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    // User clicked to log out.
                    ParseUser.logOut();
                    currentUser = null;
                    onLoggedOut();
                } else {
                    // User clicked to log in.
                    ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                            getActivity());
                    startActivityForResult(loginBuilder.build(), LOGIN_REQUEST);
                }
            }
        });
        updateViewBasedOnCurrentUser();
        return rootView;
    }

    /**
     * Three states:
     * logged in with a full profile
     * logged in with a partial profile
     * logged out
     *
     */
    private void updateViewBasedOnCurrentUser() {
        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            if(hasAllRequiredDetails()){
                showProfileLoggedIn();
            }else{
                showUpdateProfile();
            }
        } else {
            showProfileLoggedOut();
        }
    }

    private boolean hasAllRequiredDetails() {
        return currentUser != null
                && currentUser.getEmail() != null
                && currentUser.getString("name") != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST){
            switch(resultCode){
                case Activity.RESULT_OK:
                    currentUser = ParseUser.getCurrentUser();
                    if(hasAllRequiredDetails()){
                        onLoggedIn();
                    }else{
                        showUpdateProfile();
                    }
                    break;
                default:
                    onLoggedOut();
            }
        }
    }

    private void onLoggedIn() {
        currentUser = ParseUser.getCurrentUser();
        showProfileLoggedIn();
        notifyLoginListeners();
    }

    private void onLoggedOut() {
        currentUser = null;
        notifyLogoutListeners();
        showProfileLoggedOut();
    }

    private void notifyLoginListeners() {
        for(final OnLogonEventListener callback : logonListeners){
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    callback.onLoggedInWithCompletedProfile();
                }

                @Override
                protected Void doInBackground(Void[] objects) {
                    return null;
                }
            }.execute();
        }
    }

    private void notifyLogoutListeners() {
        for(OnLogonEventListener callback : logonListeners){
            callback.onLoggedOut();
        }
    }

    /**
     * Shows the profile of the given user.
     */
    private void showProfileLoggedIn() {
        showViews(titleTextView, nameTextView, emailTextView, beginSplitButton, loginOrLogoutButton, updateProfileButton);
        hideViews(nameEditText, emailEditText);
        titleTextView.setText(R.string.profile_title_logged_in);
        nameTextView.setText(currentUser.getString("name"));
        emailTextView.setText(currentUser.getEmail());

        loginOrLogoutButton.setText(R.string.profile_logout_button_label);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateProfile();
            }
        });
    }

    /**
     * Show a message asking the user to log in, toggle login/logout button text.
     */
    private void showProfileLoggedOut() {
        showViews(titleTextView, loginOrLogoutButton);
        hideViews(nameEditText, emailEditText, nameTextView, emailTextView, beginSplitButton, updateProfileButton);

        titleTextView.setText(R.string.profile_title_logged_out);
        loginOrLogoutButton.setText(R.string.profile_login_button_label);
    }

    private void showUpdateProfile() {
        showViews(titleTextView, nameEditText, emailEditText, updateProfileButton, loginOrLogoutButton);
        hideViews(nameTextView, emailTextView, beginSplitButton);
        titleTextView.setText("Complete your profile to begin");
        nameEditText.setText(nameTextView.getText());
        emailEditText.setText(emailTextView.getText());
        loginOrLogoutButton.setText(R.string.profile_logout_button_label);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasValidProfile()) {
                    updateProfileButton.setEnabled(false);
                    updateProfileButton.setText("Loading...");
                    currentUser.put("name", nameEditText.getText().toString());
                    currentUser.setEmail(emailEditText.getText().toString());
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                StringBuilder error = new StringBuilder();
                                switch(e.getCode()){
                                    case ParseException.DUPLICATE_VALUE:
                                        error.append("Save failed, duplicate value.");
                                        break;
                                    case ParseException.ACCOUNT_ALREADY_LINKED:
                                        error.append("Save failed, account already linked");
                                        break;
                                    case ParseException.EMAIL_TAKEN:
                                        error.append("Save failed, email is already taken");
                                        break;
                                    case ParseException.CONNECTION_FAILED:
                                        error.append("Connection failed, please check your network");
                                        break;
                                    case ParseException.TIMEOUT:
                                        error.append("Connection timed out, please check your network");
                                        break;
                                    default:
                                        error.append("Save failed, are you connected?");
                                }
                                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_LONG).show();
                                updateViewBasedOnCurrentUser();
                            }
                            updateProfileButton.setEnabled(true);
                            updateProfileButton.setText("Update Profile");
                        }
                    });
                }
            }
        });
    }

    private boolean hasValidProfile() {
        int errors = 0;
        if(nameEditText.getText().length() == 0){
            errors++;
            nameEditText.setError("Please enter a name");
        }
        if(!isValidEmail(emailEditText.getText())){
            errors++;
            emailEditText.setError("Please enter a valid email");
        }
        return errors == 0;
    }

    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void hideViews(View... views) {
        for(View view : views){
            view.setVisibility(View.GONE);
        }
    }

    private void showViews(View... views) {
        for(View view : views){
            view.setVisibility(View.VISIBLE);
        }
    }


}
