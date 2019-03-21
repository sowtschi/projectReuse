package com.th_koeln.steve.klamottenverteiler;

/** Die Klasse UserInterface ist die zentrale Anlaufstelle für Benutzer um das Erledigen von Aufgaben
 * einzuleiten. Dem Benutzer werden die einzelnen Funktionionen, die zur Auswahl stehen, präsentiert
 * und bei Auswahl einer Funktion, wird die entsprechende Klasse aufgerufen.
 *
 * Created by Steffen Owtschinnikow on 05.11.17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;

public class UserInterface extends AppCompatActivity implements View.OnClickListener {
    private Button btnAddClothingUserInterface;
    private Button btnSearchClothingUserInterface;
    private Button btnLogout;
    private Button btnMyClothing;
    // private Button btnShowPrefer;
    private Button btnShowOutfit;
    private Button btnEditProfile;
    private Button btnTimePlan;
    private Button btnRequests;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        btnAddClothingUserInterface = (Button) findViewById(R.id.btnAddClothingUserInterface);
        btnAddClothingUserInterface.setOnClickListener(this);

        btnSearchClothingUserInterface = (Button) findViewById(R.id.btnSearchClothingUserInterface);
        btnSearchClothingUserInterface.setOnClickListener(this);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);

        btnMyClothing = (Button) findViewById(R.id.btnMyClothing);
        btnMyClothing.setOnClickListener(this);

        btnShowOutfit = (Button) findViewById(R.id.btnShowOutfit);
        btnShowOutfit.setOnClickListener(this);

        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(this);

        btnTimePlan = (Button) findViewById(R.id.btnTimePlan);
        btnTimePlan.setOnClickListener(this);

        btnRequests = (Button) findViewById(R.id.btnRequests);
        btnRequests.setOnClickListener(this);

        firebaseAuth=FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSearchClothingUserInterface:
                startActivity(new Intent(getApplicationContext(), SearchClothing.class));
                break;

            case R.id.btnAddClothingUserInterface:
                startActivity(new Intent(getApplicationContext(), AddClothing.class));
                break;

            case R.id.btnLogout:
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                break;

            case R.id.btnShowOutfit:
                startActivity(new Intent(getApplicationContext(),ShowOutfit.class));
                break;

            case R.id.btnEditProfile:
                startActivity(new Intent(getApplicationContext(), EditProfile.class));
                break;

            case R.id.btnMyClothing:
                startActivity(new Intent(getApplicationContext(), MyClothing.class));
                break;

            case R.id.btnRequests:
                startActivity(new Intent(getApplicationContext(), ShowRequest.class));
                break;

            case R.id.btnTimePlan:
                startActivity(new Intent(getApplicationContext(),TimePlan.class));
                break;

        }
    }
}
