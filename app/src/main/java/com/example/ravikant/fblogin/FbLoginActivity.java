package com.example.ravikant.fblogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

public class FbLoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private Set<String> recentlyGrantedPermissions;
    private Set<String> recentlyDeniedPermissions;
    private ProgressDialog progress;
    private String facebook_id,f_name, m_name, l_name, gender, profile_image, full_name, email_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_fb_login);


        callbackManager = CallbackManager.Factory.create();

        final SharedPreferences sharedPreferences = getSharedPreferences("MYAPP", 0);

        if (AccessToken.getCurrentAccessToken() != null) {
            startActivity(new Intent(FbLoginActivity.this, MainActivity.class));
            finish();
        }

        progress=new ProgressDialog(FbLoginActivity.this);
        progress.setMessage("Please wait...");
        progress.setIndeterminate(false);
        progress.setCancelable(false);

        Button loginButton = (Button) findViewById(R.id.login_button);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;
            @Override
            public void onSuccess(LoginResult loginResult) {
                progress.show();
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    facebook_id=profile.getId();
                    f_name=profile.getFirstName();
                    m_name=profile.getMiddleName();
                    l_name=profile.getLastName();
                    full_name=profile.getName();
                    profile_image=profile.getProfilePictureUri(400, 400).toString();
                }else{
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            facebook_id=profile2.getId();
                            f_name=profile2.getFirstName();
                            m_name=profile2.getMiddleName();
                            l_name=profile2.getLastName();
                            full_name=profile2.getName();
                            profile_image=profile2.getProfilePictureUri(400, 400).toString();
                            mProfileTracker.stopTracking();
                        }
                    };
                    mProfileTracker.startTracking();
                }
                sharedPreferences.edit()
                        .putString("fb_id", facebook_id)
                        .putString("profile_image", profile_image)
                        .putString("full_name", full_name)
                        .putString("email_id", email_id)
                        .apply();
                Intent i=new Intent(FbLoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("TODAY ERROR",error.getMessage());
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(FbLoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void tmp(){
        if (AccessToken.getCurrentAccessToken() != null) {

            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            if (me != null) {

                                String profileImageUrl = ImageRequest.getProfilePictureUri(me.optString("id"), 500, 500).toString();
                                Log.i("TODAY ", profileImageUrl);
                            }
                        }
                    });
            GraphRequest.executeBatchAsync(request);
        }
    }

    /*GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
            new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        // email_id=object.getString("email");
                        // gender=object.getString("gender");
                        String profile_name=object.getString("name");
                        long fb_id=object.getLong("id"); //use this for logout
                        //Start new activity or use this info in your project.

                        sharedPreferences.edit()
                                .putString("fb_id", facebook_id)
                                .putString("profile_image", profile_image)
                                .putString("full_name", full_name)
                                .putString("email_id", email_id)
                                .apply();
                        Intent i=new Intent(FbLoginActivity.this, MainActivity.class);
                                    *//*i.putExtra("type","facebook");
                                    i.putExtra("facebook_id",facebook_id);
                                    i.putExtra("f_name",f_name);
                                    i.putExtra("m_name",m_name);
                                    i.putExtra("l_name",l_name);
                                    i.putExtra("full_name",full_name);
                                    i.putExtra("profile_image",profile_image);
                                    i.putExtra("email_id",email_id);
                                    i.putExtra("gender",gender);*//*

                        progress.dismiss();
                        startActivity(i);
                        finish();
                    } catch (JSONException e) {
                        progress.dismiss();
                        Log.e("TODAY ", e.getMessage());
                        // TODO Auto-generated catch block
                        //  e.printStackTrace();
                    }
                }
            });
    // request.executeAsync();*/
}
