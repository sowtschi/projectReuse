package com.th_koeln.steve.klamottenverteiler.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by steve on 31.10.17.
 */
public class HttpsService extends IntentService {
    public HttpsService() {
        super("name");
    }

    private String response = null;
    private String method;
    private String from;
    private String layer;
    HttpURLConnection connection = null;
    //HttpsURLConnection connection = null;
    Handler mHandler;

    public void onCreate() {
        super.onCreate();
         mHandler = new Handler();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String payload = intent.getStringExtra("payload");
        String uri = intent.getStringExtra("url");
        method = intent.getStringExtra("method");
        from = intent.getStringExtra("from");
        //Spezifisch fuer die Outfitdarstellung
        if(intent.hasExtra("layer")){layer = intent.getStringExtra("layer"); }

        /*Um die Kommunikation über HTTPs mit einem  Zertifikat zu ermöglichen, wird im Folgender ein Trusting-Manager angelegt, der jedes SSL-Zertifikat
        annimmt. https://developer.android.com/training/articles/security-ssl.html#SelfSigned
         Um einen sicheren Datenverkehr zu gewährleisten muss der Trust Manager aus der Applikation entfernt werden und ein geprüftes Zertifikat verwendet werden. */

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
           // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            //HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL url = null;

            // Einstellungen für HTTPS-Verbindungen vornehmen
                url = new URL(uri);
                connection = (HttpsURLConnection) url.openConnection();
                // definiere HTTP Methode
                connection.setRequestMethod(method);
                // setze Timeout für die Operation fest
                connection.setConnectTimeout(10000);

                if (method.equals("POST") || method.equals("PUT")) {
                    // http request mit Payload
                    connection.setDoOutput(true);
                    // http response mit Payload
                    connection.setDoInput(true);
                    // definiere den "Content-Type" des Payloads als JSON
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    // StreamWriter für den Payload wird bereitgestellt
                    OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
                    //übertrage Payload in den streamWriter
                    streamWriter.write(payload);
                    streamWriter.flush();
                } else if (method.equals("GET")) {
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    connection.connect();
                }
                // Sende HTTP-Request
                sendJSON(uri, payload);
        }  catch (SocketTimeoutException e) {
            reactOnTimeout(from);
        } catch (IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HttpsService.this, "Can not send Data to Server!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HttpsService.this, "Can not send Data to Server!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (KeyManagementException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HttpsService.this, "Can not send Data to Server!", Toast.LENGTH_LONG).show();
                }
            });
        }
        }

    private void sendJSON(String uri, String payload) throws KeyManagementException, NoSuchAlgorithmException {

        try {
            // Bereite Datencontainer für Payload des Response vor
            StringBuilder stringBuilder = new StringBuilder();
            // Status Code des Response festhalten
            int status = connection.getResponseCode();
            Intent intent = new Intent();
            // analyse Status code
            switch (status) {
                case 200:
                case 201:
                    InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(streamReader);

                    // Speichert Response Payload
                    while ((response = bufferedReader.readLine()) != null) {
                        stringBuilder.append(response + "\n");
                    }
                    bufferedReader.close();


                    /*From enthält einen String über den identifiziert wird, welche Aktivität eine Antwort auf den jeweiligen Request erwartet.*/
                    switch (from) {
                        //from wird ausgewertet und die Daten an die passende Stelle geleitet.
                        case "SEARCH":
                            intent = new Intent("clothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCH");
                            break;

                        case "GMAPS":
                            intent = new Intent("gmaps");
                            intent.putExtra("mapsData", stringBuilder.toString());
                            intent.putExtra("from", "GMAPS");
                            break;

                        case "CLOTHINGOPTIONS":
                            intent = new Intent("clothingOptions");
                            intent.putExtra("optionsData", stringBuilder.toString());
                            intent.putExtra("from", "CLOTHINGOPTIONS");
                            break;

                        case "SEARCHPREFER":
                            intent = new Intent("prefer");
                            intent.putExtra("prefer", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHPREFER");
                            break;

                        case "SHOWDETAILS":
                            intent = new Intent("showdetails");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SHOWDETAILS");
                            break;

                        case "GETCLOTHINGDETAIL":
                            intent = new Intent("getClothingDetail");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "GETCLOTHINGDETAIL");
                            break;

                        case "GETCLOTHINGDETAIL2":
                            intent = new Intent("getClothingDetail2");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("layer",layer);
                            intent.putExtra("from", "GETCLOTHINGDETAIL2");
                            break;

                        case "PROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("profile", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHPROFILE");
                            break;

                        case "GETPROFILE":
                            intent = new Intent("getProfile");
                            intent.putExtra("profile", stringBuilder.toString());
                            intent.putExtra("from", "GETPROFILE");
                            break;

                        case "MYCLOTHING":
                            intent = new Intent("myclothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "MYCLOTHING");
                            break;

                        case "EDITCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "EDITCLOTHING");
                            break;

                        case "SEARCHOUTFIT":
                            intent = new Intent("showoutfit");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHOUTFIT");
                            break;

                        case "SHOWREQUESTS":
                            intent = new Intent("showrequests");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("success", "0");
                            intent.putExtra("from", "SHOWREQUESTS");
                            break;

                        case "GETOWNREQUESTS":
                            intent = new Intent("getOwnRequests");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("success", "0");
                            intent.putExtra("from", "GETOWNREQUESTS");
                            break;

                        case "GETCONVERSATION":
                            intent = new Intent("chat");
                            intent.putExtra("params", stringBuilder.toString());
                            intent.putExtra("from", "GETCONVERSATION");
                            break;

                        case "ADDCLOTHING":
                            intent = new Intent("addclothing");
                            intent.putExtra("from", "ADDCLOTHING");
                            intent.putExtra("success", "1");
                            break;
                        case "PUTREQUEST":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "PUTREQUEST");
                            intent.putExtra("success", "1");
                            break;
                        case "DELETEREQUEST":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "PUTREQUEST");
                            intent.putExtra("success", "2");
                            break;
                        case "POSTRATING":
                            intent = new Intent("RATEUSER");
                            intent.putExtra("from", "POSTRATING");
                            break;
                        case "NEW_TOKEN":
                            intent = new Intent("login");
                            intent.putExtra("from", "POSTTOKEN");
                            break;
                        case "NEWUSER":
                            intent = new Intent("main");
                            intent.putExtra("from", "POSTUSER");
                            break;
                        case "NEWREQUEST":
                            intent = new Intent("showclothing");
                            intent.putExtra("from", "NEWREQUEST");
                            break;
                        case "PUTPROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("from", "PUTPROFILE");
                            break;


                    }
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    break;
                case 401:
                case 500:
                    switch (from) {
                        /* Sollte ein Request fehlschlagen, wird die zugehörige Aktivität über das Problem im Folgenden benachrichtigt*/
                        case "ADDCLOTHING":
                            intent = new Intent("addclothing");
                            intent.putExtra("from", "ADDCLOTHING");
                            intent.putExtra("success", "0");
                            break;
                        case "SEARCHOUTFIT":
                            intent = new Intent("showoutfit");
                            intent.putExtra("from", "SEARCHOUTFITFAIL");
                            intent.putExtra("success", "0");
                            break;
                        case "GETCONVERSATION":
                            intent = new Intent("chat");
                            intent.putExtra("from", "GETCONVERSATIONFAIL");
                            break;
                        case "POSTMESSAGE":
                            intent = new Intent("chat");
                            intent.putExtra("from", "POSTMESSAGEFAIL");
                            break;
                        case "EDITCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("from", "EDITCLOTHINGFAIL");
                            break;
                        case "PUTCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("from", "PUTCLOTHINGFAIL");
                            break;
                        case "PROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("from", "SEARCHPROFILEFAIL");
                            break;
                        case "PUTPROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("from", "PUTPROFILEFAIL");
                            break;
                        case "NEW_TOKEN":
                            intent = new Intent("login");
                            intent.putExtra("from", "POSTTOKENFAIL");
                            break;
                        case "NEWUSER":
                            intent = new Intent("main");
                            intent.putExtra("from", "POSTUSERFAIL");
                            break;
                        case "SHOWDETAILS":
                            intent = new Intent("showdetails");
                            intent.putExtra("from", "SHOWDETAILSFAIL");
                            break;

                        case "MYCLOTHING":
                            intent = new Intent("myclothing");
                            intent.putExtra("from", "MYCLOTHINGFAIL");
                            break;
                        case "POSTRATING":
                            intent = new Intent("RATEUSER");
                            intent.putExtra("from", "POSTRATINGFAIL");
                            break;
                        case "SEARCH":
                            intent = new Intent("clothing");
                            intent.putExtra("from", "SEARCHFAIL");
                            break;
                        case "SEARCHPREFER":
                            intent = new Intent("clothing");
                            intent.putExtra("from", "SEARCHPREFERFAIL");
                            break;
                        case "NEWREQUEST":
                            intent = new Intent("showclothing");
                            intent.putExtra("from", "NEWREQUESTFAIL");
                            break;
                        case "SUBSCRIBECLOTHING":
                            intent = new Intent("showoutfit");
                            intent.putExtra("from", "SUBSCRIBECLOTHINGFAIL");
                            break;
                        case "SHOWREQUESTS":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "SHOWREQUESTSFAIL");
                            intent.putExtra("success", "0");
                            break;


                    }
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    break;
                default:
                    Log.e("HTTPs Response: ", connection.getResponseMessage());
                    break;
            }
        } catch (SocketTimeoutException e) {
            reactOnTimeout(from);
        } catch (Exception exception) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HttpsService.this, "Could not process Server-Response!", Toast.LENGTH_LONG).show();
                }
            });

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void reactOnTimeout(String from) {
        Intent intent;

        switch (from) {

            case "SEARCHOUTFIT":
                intent = new Intent("showoutfit");
                intent.putExtra("from", "SEARCHOUTFITFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;

            case "NEW_TOKEN":
                intent = new Intent("login");
                intent.putExtra("from", "POSTTOKENFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;

            case"NEWUSER" :
                intent = new Intent("main");
                intent.putExtra("from", "POSTUSERFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case"SHOWREQUESTS" :
                intent = new Intent("showrequests");
                intent.putExtra("from", "SHOWREQUESTSFAIL");
                intent.putExtra("success", "0");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case "SEARCH":
                intent = new Intent("clothing");
                intent.putExtra("from", "SEARCHFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case "PROFILE":
                intent = new Intent("profile");
                intent.putExtra("from", "SEARCHPROFILEFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case "CLOTHINGOPTIONS":
                intent = new Intent("addclothing");
                intent.putExtra("from", "CLOTHINGOPTIONSFAIL");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            default:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HttpsService.this, "Could not connect to server. Internet missing?", Toast.LENGTH_LONG).show();
                    }
                });
        }
    }
}
