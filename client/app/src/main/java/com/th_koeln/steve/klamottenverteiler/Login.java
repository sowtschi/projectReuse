package com.th_koeln.steve.klamottenverteiler;

/**
 * Diese Klasse dient der Verifizierung der Identität der Benutzer über die Firebase Plattform.
 * Neben dem Login des Benutzers prüft die Klasse ebenfalls, ob die Email Adresse des Benutzers
 * valide ist.
 * Im Erfolgsfall wird ein Benutzer-Token von Firebase angefordert und anschließend an den Server
 * weitergeleitet.
 *
 * Created by Steffen Owtschinnikow on 05.11.17.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;



public class Login extends AppCompatActivity implements View.OnClickListener {
    private TextView etPasswordLogin;
    private TextView etEmailLogin;
    private TextView tvLogin;
    private String uID;
    private Button btnLogin;
    private String newTokenKey;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 256-Bit Key um Berechtigung am Server zu erhalten
        newTokenKey= "nIWP3D9OZFoEb36ZpukAoCD67Gm50SW0pPEuKwuSnNv5lFcx5EcNBHNhac1h6mZR2vxrWJWIyBTWISJJIsf4brBbmAqVL2GO2bfmWECj1lkGaui4nN6C8vepsTu2oYPFR3u7uREyZ4ztf1GfVCCPfEKvoQzpaeEZKfzyFAaF07bxipxz9KYKx42KExExKCwNfh1EHJOnRnNGvUkuE53kTcuZc8bc3tb5hqgIhJ9GYExeIwRHtutHZ03uP9Hh        VE6lJs8acr8y4IYfwqvMX8RyPe3JseguJb3qA0MmGgAb5CM8APdrAVuezB8QYyHg5PqJIazX83ICyTMJPhjceI9NDPJAU0t6zSaCWIo2oJuaKwDmAUW2fCo4PLNyuxom0vOsK4KGALFIkvHysiV2lXyDBwTK5sd4EIKm1UJPoZKG3jRHBCGKwT7t9BRcWYZaVxVkqi0wa0oWcROv7Hg4EbEtwZDi5o9RI8orwO1EUc4rPOVTI7fj71cKREAz";

        firebaseAuth = FirebaseAuth.getInstance();

        // Starte UserInterface, wenn benutzer bereits eingeloggt ist.
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, UserInterface.class));
            finish();
        }

        etPasswordLogin = (EditText) findViewById(R.id.etPasswordLogin);
        etEmailLogin = (EditText) findViewById(R.id.etEmailLogin);

        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("login"));

    }

    private void userLogin() {

        String email = etEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email-address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Plase enter your password!.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Trying to login\n");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                // Informiere Benutzer, wenn Loginvorgang fehlgeschlagen ist.
                if(!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(Login.this, "Error! - Can't connect to login service. Is internet available?",
                                Toast.LENGTH_LONG).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(Login.this, "Error! - Account not available or wrong password!",
                                Toast.LENGTH_LONG).show();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(Login.this, "Error! - Account not available or wrong password!",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(Login.this, "Error! - Can not login.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Überprüfe, ob die Email Adresse des Benutzers validiert ist
                    checkVerified();
                }
            }
        });

    }

    @Override
    public void onClick(View view) {

        if (view == btnLogin) {
            userLogin();
        } else if (view == tvLogin) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    private void checkVerified() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {
            // Sende Usertoken zum Server und starte UserInterface, wenn Email verifiziert ist
            sendTokenToServer();


        } else {
            // Logout wenn Email Adresse des Benutzers nicht verifiziert ist.
            Toast.makeText(getApplicationContext(), "Email is not verified", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            FirebaseAuth.getInstance().signOut();

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("POSTTOKENFAIL")) {
                // Token konnte nicht gesendet werden + User ausloggen.
                showDialog("Error","Could not login!");
                FirebaseAuth.getInstance().signOut();
                progressDialog.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "You are now logged in", Toast.LENGTH_SHORT).show();
                Intent in = new Intent(getApplicationContext(),UserInterface.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();
            }


        }
    };

    private void sendTokenToServer() {

        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            // Hole UserToken von Firebase
            mUser.getToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                        public void onComplete(@NonNull Task<GetTokenResult> task) {

                            if (task.isSuccessful()) {

                                String idToken = FirebaseInstanceId.getInstance().getToken();
                                uID = mUser.getUid();



                                try {
                                    // Sende User Token zum Server
                                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                                    JSONObject token = new JSONObject();
                                    token.put("token", idToken);
                                    token.put("key", newTokenKey);
                                    myIntent.putExtra("payload", token.toString());
                                    myIntent.putExtra("method", "POST");
                                    myIntent.putExtra("from", "NEW_TOKEN");
                                    myIntent.putExtra("url", getString(R.string.DOMAIN) + "/users/" + uID + "/token/" + idToken);
                                    //call http service
                                    startService(myIntent);

                                } catch (JSONException e) {
                                    FirebaseAuth.getInstance().signOut();
                                    showDialog("Error", "Could not send your usertoken!");


                                }
                            }
                        }
                    });
        } catch (NullPointerException e) {
            // Senden des Tokens fehlgeschlagen
            FirebaseAuth.getInstance().signOut();
            showDialog("Error", "Could not send usertoken to server!");
        }
    }
    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing())
        alertDialog.show();
    }
}
