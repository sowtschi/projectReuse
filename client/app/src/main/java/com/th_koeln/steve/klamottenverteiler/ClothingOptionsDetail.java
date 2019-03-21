package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOptionsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Steffen Owtschinnikow on 18.01.2018.
 */

public class ClothingOptionsDetail extends AppCompatActivity {

    private ArrayList<String> clothingOptionsLevel = new ArrayList<String>();
    private JSONArray optionsArray;
    private ListView listViewOptionsDetail;
    private int OPT = 1;
    public static final int CHOOSE_OPTION = 77;
    private String area = "";
    private boolean isArt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clothing_options_detail_layout);

        listViewOptionsDetail = (ListView) findViewById(R.id.listViewOptionsDetail);

        Intent in = getIntent();
        String data = in.getStringExtra("items");
        area = in.getStringExtra("area");
        final int whichOne = in.getIntExtra("option",-1);

        try{
            JSONObject clothingOptions = new JSONObject(data);
            optionsArray = new JSONArray(clothingOptions.getJSONArray("options").toString());
            if(clothingOptions.getString("topic").equals("Art")){isArt = true;}else{isArt=false;}
            if(whichOne == 1){
                for(int i=0;optionsArray.length()>i;i++){
                    JSONObject tmpObject = new JSONObject(optionsArray.get(i).toString());
                    clothingOptionsLevel.add(tmpObject.getString("topic"));
                    fillListView(clothingOptionsLevel);
                }
            }else if(whichOne == 2){
                for(int i=0;optionsArray.length()>i;i++){
                    String tmpString = new String(optionsArray.get(i).toString());
                    clothingOptionsLevel.add(tmpString);
                    fillListView(clothingOptionsLevel);
                }
            }
        }catch(JSONException e){
            showDialog("Error", "Could not process request data!");
        }

        listViewOptionsDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showDetailActivity = new Intent(getApplicationContext(), ClothingOptionsDetail.class);
                try {
                    if(whichOne == 1){
                        JSONObject tmpObject = new JSONObject(optionsArray.get(0).toString());
                        JSONArray tmpArray = tmpObject.getJSONArray("options");
                        JSONObject tmpObject2 = tmpArray.optJSONObject(0);
                        if(tmpObject2==null){OPT = 2;}
                    }

                    if(whichOne == 1) {
                        JSONObject tmpObject = new JSONObject(optionsArray.get(i).toString());
                        if(isArt) {
                            String tmpString = new String(tmpObject.getString("topic"));
                            area = tmpString;
                        }
                        showDetailActivity.putExtra("items", tmpObject.toString());
                        showDetailActivity.putExtra("option", OPT);
                        showDetailActivity.putExtra("area", area);
                        startActivityForResult(showDetailActivity, CHOOSE_OPTION);
                    }

                    if(whichOne == 2){
                        Intent resultIntent = new Intent();
                        String text = new String(optionsArray.get(i).toString());
                        resultIntent.putExtra("StringResult",text);
                        resultIntent.putExtra("area",area);
                        setResult(CHOOSE_OPTION,resultIntent);
                        finish();
                    }
                }catch(JSONException e){
                    showDialog("Error", "Could not process request data!");
                }
            }
        });

    }

    private void fillListView(ArrayList<String> options) {
        ClothingOptionsAdapter optAdapter;
        optAdapter = new ClothingOptionsAdapter(this, options);
        listViewOptionsDetail.setAdapter(optAdapter);
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHOOSE_OPTION){
            if(data!=null) {
                String StringResult = data.getStringExtra("StringResult");
                String AreaResult = data.getStringExtra("area");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("StringResult", StringResult);
                resultIntent.putExtra("area", AreaResult);
                setResult(CHOOSE_OPTION, resultIntent);
                finish();
            }
        }
    }
}