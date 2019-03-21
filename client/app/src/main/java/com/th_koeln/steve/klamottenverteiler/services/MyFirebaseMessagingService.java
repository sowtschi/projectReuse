package com.th_koeln.steve.klamottenverteiler.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.th_koeln.steve.klamottenverteiler.Chat;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.ShowClothing;
import com.th_koeln.steve.klamottenverteiler.ShowRequest;
import com.th_koeln.steve.klamottenverteiler.UserInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Frank on 06.01.2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    public void onMessageReceived(RemoteMessage remoteMessage) {

        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage message) {
        try {

        Map<String, String> params = message.getData();
        JSONObject paramsJson = new JSONObject(params);
        String from = paramsJson.getString("sender");
        Intent myIntent = new Intent();

            switch (from) {
                case "missing":
                    myIntent = new Intent(getApplicationContext(), ShowClothing.class);
                    myIntent.putExtra("clothingID", paramsJson.getString("cId") );
                    break;
                case "postRequest":
                    myIntent = new Intent(getApplicationContext(), ShowRequest.class);
                    myIntent.putExtra("from", "showNotification");
                    break;

                case "accepted":
                    myIntent = new Intent(getApplicationContext(),ShowRequest.class);
                    myIntent.putExtra("from", "showNotification");
                    break;
                case "success":
                    myIntent = new Intent(getApplicationContext(),ShowRequest.class);
                    myIntent.putExtra("from", "showNotification");
                    break;
                case "confirmed":
                    myIntent = new Intent(getApplicationContext(),ShowRequest.class);
                    myIntent.putExtra("from", "showNotification");
                    break;
                case "waiting":
                    myIntent = new Intent(getApplicationContext(),ShowRequest.class);
                    myIntent.putExtra("from", "showNotification");
                    break;
                case "message":
                    if (Chat.active == true) {
                        Intent intent = new Intent("chat");
                        intent.putExtra("params", paramsJson.toString());
                        intent.putExtra("from", "newMessage");

                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    } else if (Chat.active == false) {
                        myIntent = new Intent(getApplicationContext(), Chat.class);
                        myIntent.putExtra("from", "newMessage");
                        myIntent.putExtra("to", paramsJson.getString("ouId"));
                    }
                    break;
                default :
                        myIntent = new Intent(getApplicationContext(),UserInterface.class);
                    break;

        }

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setContentTitle(message.getNotification().getTitle())
                        .setContentText(message.getNotification().getBody())
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentIntent(pendingIntent);

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }





    }


}