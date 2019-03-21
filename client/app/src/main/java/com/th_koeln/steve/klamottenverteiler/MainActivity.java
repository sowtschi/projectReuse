package com.th_koeln.steve.klamottenverteiler;

/**
 * Um das System nutzen zu können ist es nötig sich mit einer Email-Adresse und einem Passwort zu registrieren.
 * Diese Klasse präsentiert dem Benutzer eine Eingebemöglichkeit seiner Daten für die Registrierung.
 * Die Daten werden anschließend an den Firebase Server gesendet, um dort einen neuen Account anzulegen.
 *
 * Sollte die Registrierung bei Firebase fehlerfrei verlaufen, werden die Daten ebenfalls zum Server gesendet,
 * um dort ein Profil für den jeweiligen User anlegen zu können.
 *
 * Nach einer Erfolgreichen Registrierung initialisiert die Klasse das Senden einer Verifizierungs-Email an
 * die vom Benutzer hinterlegte Email-Adresse.
 *
 * Created by Steffen Owtschinnikow on 10.01.2018.
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvGoLogin;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private boolean isFirebaseCalledOnce =false;
    private String newTokenKey;


    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        // Starte UserInterface, wenn benutzer bereits eingeloggt ist.
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, UserInterface.class));
        }

        progressDialog = new ProgressDialog(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        etEmail = (EditText) findViewById(R.id.etEmailRegistration);
        etPassword = (EditText) findViewById(R.id.etPasswordRegister);
        tvGoLogin = (TextView) findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
        // 256-Bit Key um Berechtigung am Server zu erhalten
        newTokenKey= "AgwGYtZUPYKavwAQLRHw0kjxGKI7KQekKhS9MStDXxcVmQZ9ipUmkBoId7AAxC605I9UkpNcO9wQcbSNyIcDVx4uhCFZOErGLFp88tlzhJiCAbCY1FI8sydBHacK6UGfzlMQJ4qx3XreLxX4aBKqTtl5emHzCf0c9FVrpwQnsYEwjmuw2CtKe0FYriX1KxBybUVBRevYnLwif3eLOvGOHxu5bFWwwV7A4rLmHtuqDGWtcfw8xtn48XwS4tTmwbgxMIITv3UrfijcDoAThudrrpDO9B6kj9G07bsP68ydUn9ZkMalgJ6sP3G0ePPcKOVlkhjyDpYeOr8qZTIVZJNr3CEsnWTUQhXYf9lCRUTQp5i1PPZUJHsi7vFm71QdnOiJQWS3l8htAjecmqT2WSOZfKjANc7da6aooKwbllIrrBikUk0z46KAIt87C2CdjjSxoLi8080poQQ4oZimi7jrHuiixHwai0rAjqMyDpD2ippE5t2jAEy3Z4Li";

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("main"));

    }

    private void registerUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        isFirebaseCalledOnce=true;


        // Überprüfe ob notwendige Informationen eingegeben wurden
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show();
            return;

        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Trying to registrate user..");
        progressDialog.show();

        // Registriere Benutzer mit Email Und Passwort
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
             if (task.isSuccessful()) {

                 String uId = firebaseAuth.getCurrentUser().getUid().toString();
                 Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                 // define parameters for Service-Call
                 JSONObject payload = new JSONObject();
                 try {
                     payload.put("uId", uId);
                     payload.put("key", newTokenKey);
                     // Sende Aufruf zum erstellen eines Userprofiles
                     myIntent.putExtra("payload",payload.toString());
                     myIntent.putExtra("method","POST");
                     myIntent.putExtra("from","NEWUSER");
                     myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/");
                     //call http service
                     startService(myIntent);
                 } catch (JSONException e) {
                     // Userprofil konnte nicht erstellt werden.
                     showDialog("Error", "Could not add your profile!");
                 }
             } else {
                 try {
                     throw task.getException();
                 } catch (FirebaseNetworkException e) {
                     Toast.makeText(MainActivity.this, "Error! - Can't connect to registration service. Is internet available?",
                             Toast.LENGTH_LONG).show();
                 } catch (FirebaseAuthWeakPasswordException  e) {
                     Toast.makeText(MainActivity.this, "Error! - Password should be at least 6 characters!",
                             Toast.LENGTH_LONG).show();
                 } catch (FirebaseAuthUserCollisionException e) {
                     Toast.makeText(MainActivity.this, "Error! - This email adress is allready registered",
                             Toast.LENGTH_LONG).show();
                 } catch (FirebaseAuthInvalidCredentialsException e) {
                     Toast.makeText(MainActivity.this, "The email address is badly formatted.",
                             Toast.LENGTH_LONG).show();
                 } catch (Exception e) {
                     Toast.makeText(MainActivity.this, "Error! - Can not register.",
                             Toast.LENGTH_LONG).show();
                 } finally {
                    progressDialog.dismiss();
                 }
             }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String from = intent.getStringExtra("from");
            if (from.equals("POSTUSERFAIL")) {
                // Fehler beim erstellen des Userprofiles
                deleteAccount();
                showDialog("Error","Could not add userprofile!");
                progressDialog.dismiss();
            } else {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Sende Verifizierúngs-Mail, wenn User eingeloggt ist
                if (user != null) {
                    sendMail();
                }
            }

        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
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

    private void sendMail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();


                            startActivity(new Intent(getApplicationContext(), Login.class));
                        } else {
                            // Email konnte nicht gesendet werden. Account wird gelöscht.
                            deleteAccount();
                            Toast.makeText(getApplicationContext(), "Fehler beim Senden der Email", Toast.LENGTH_SHORT).show();
                            startActivity(getIntent());
                        }
                    }
                });
    }

    private void deleteAccount() {
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUser.delete();
    }
}
