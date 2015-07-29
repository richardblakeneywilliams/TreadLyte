package com.mainapp.treadlyte;

import android.content.Intent;
import android.support.v4.app.Fragment; //TODO: Change to SDK v15+ fragment.
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.common.SignInButton;




/**
 * Fragment that is hosted by the main login activity.
 * Sets up facebook and google login
 */
public class LoginFragment extends Fragment
        implements View.OnClickListener {


    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;



    private CallbackManager mCallbackManager;
    private TextView mTextDetails;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private static final String TAG = "LoginFragment";

    private UserProfileObject test;


//BRADZ Ggh

    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();
            displayWelcomeMessage(profile);
            profileCreator(profile);
            //TODO: Add profile saving and loading here.

            //Start Home_activity
            Intent i = new Intent(getActivity(),Home_Activity.class);
            startActivity(i);



        }

        @Override
        public void onCancel() {
            Toast.makeText(getActivity(),"Error with Facebook Login",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(FacebookException e) {
        //TODO: Investigate and add handler here based on what the FUCK this does. Look at API nigga
        }
    };








    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        //Google Login
        //getView().findViewById(R.id.sign_in_button).setOnClickListener(this);






        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                displayWelcomeMessage(newProfile);

            }
        };

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

    }




    public void profileCreator(Profile profile){


        ///
        // /UserProfileObject test = new UserProfileObject(profile.getFirstName(),profile.getLastName(),profile.getProfilePictureUri(300,300));


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initiate the login button on the XML
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        //This fucker is getting the permissions.
        loginButton.setReadPermissions("user_friends");
        //Setting the fragment
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);

        mTextDetails = (TextView) view.findViewById(R.id.text_details);



    }

    public void displayWelcomeMessage(Profile profile){
        if(profile != null){
            mTextDetails.setText("Welcome to TL Beta " + profile.getFirstName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        displayWelcomeMessage(profile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
    }

    @Override
    public void onClick(View v) {

    }
}
