package com.example.ravikant.fblogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import it.sephiroth.android.library.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private String profilePath;
    private String firstName;
    private String lastName, facebook_id;
    private AccessToken accessToken;
    private Profile profile;
    private SharedPreferences sharedPreferences;
    private ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        accessToken = AccessToken.getCurrentAccessToken();

        sharedPreferences = getSharedPreferences("MYAPP", 0);
        firstName = sharedPreferences.getString("full_name", "");
        profilePath = sharedPreferences.getString("profile_image", "");

        final ImageView profileImage = (ImageView) findViewById(R.id.imgProfileImage);
        final TextView txtName = (TextView) findViewById(R.id.txtName);
        final Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutFromFacebook();
            }
        });
        callbackManager = CallbackManager.Factory.create();
        txtName.setText(firstName);
        if (!profilePath.isEmpty()) Picasso.with(this).load(profilePath).into(profileImage);
        profile = Profile.getCurrentProfile();

        if (profile!=null) {
            firstName = profile.getFirstName();
            lastName = profile.getLastName();
            profilePath = profile.getProfilePictureUri(400, 400).toString();
            txtName.setText(firstName + " " + lastName);
            Picasso.with(this).load(profilePath).into(profileImage);
        }else{
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, Profile profile2) {

                    facebook_id=profile2.getId();
                    /*f_name=profile2.getFirstName();
                    m_name=profile2.getMiddleName();
                    l_name=profile2.getLastName();*/
                    firstName=profile2.getName();
                    profilePath=profile2.getProfilePictureUri(400, 400).toString();
                    mProfileTracker.stopTracking();

                    txtName.setText(firstName);
                    Picasso.with(MainActivity.this).load(profilePath).into(profileImage);
                }
            };
            mProfileTracker.startTracking();
        }
    }

    private void setFacebookData()
    {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        try {
                            Log.e("Login Response",response.toString());
                            Log.e("Login obj==",object.toString());

                            String email = response.getJSONObject().getString("email");
                            firstName = response.getJSONObject().getString("first_name");
                            lastName = response.getJSONObject().getString("last_name");
                            String gender = response.getJSONObject().getString("gender");
                            String bday= response.getJSONObject().getString("birthday");

                            Profile profile = Profile.getCurrentProfile();
                            String link = profile.getLinkUri().toString();
                            Log.e("Login link",link);
                            if (Profile.getCurrentProfile()!=null)
                            {
                               profilePath =  Profile.getCurrentProfile().getProfilePictureUri(500, 500).toString();
                            }

                            Log.e("Login " + "Email", email);
                            Log.e("Login "+ "FirstName", firstName);
                            Log.e("Login " + "LastName", lastName);
                            Log.e("Login " + "Gender", gender);
                            Log.e("Login " + "Bday", bday);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Login ", e.getMessage());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getUserData(){
        if (AccessToken.getCurrentAccessToken() != null) {

            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            if (me != null) {
                                firstName = me.optString("name");
                                profilePath = ImageRequest.getProfilePictureUri(me.optString("id"), 500, 500).toString();
                            }
                        }
                    });
            GraphRequest.executeBatchAsync(request);
        }
    }

    private void logoutFromFacebook(){
        try {
            if (AccessToken.getCurrentAccessToken() == null) {
                return; // already logged out
            }
            GraphRequest graphRequest=new GraphRequest(AccessToken.getCurrentAccessToken(), "/ "+facebook_id+"/permissions/", null,
                    HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    LoginManager.getInstance().logOut();
                    startActivity(new Intent(MainActivity.this, FbLoginActivity.class));
                    finish();
                }
            });

            graphRequest.executeAsync();
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
