package com.th_koeln.steve.klamottenverteiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOptionsAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.services.ListViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Michael on 22.01.2018.
 */

public class SearchClothingFilter extends AppCompatActivity implements View.OnClickListener {

    public static final int CHOOSE_OPTION = 77;
    private int OPT = 1, choosenOption, progressDist;
    private ArrayList<String> clothingOptionsLevel1 = new ArrayList<String>();
    private JSONArray clothingOptions;

    private Button btnSearch, btnCancel;
    private ListView listViewOptions;
    private TextView textViewProgress;
    private SeekBar seekBarDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity_layout);

        listViewOptions = (ListView) findViewById(R.id.listViewOptions);
        textViewProgress = (TextView) findViewById(R.id.textViewProgress);

        Intent in = getIntent();
        progressDist = in.getIntExtra("distance",0);

        seekBarDistance = (SeekBar) findViewById(R.id.seekBarDistance);
        seekBarDistance.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarDistance.setProgress(progressDist);
        textViewProgress.setText("Entfernung in KM: "+progressDist);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("clothingOptions"));

        Intent optionsIntent = new Intent(getApplicationContext(), HttpsService.class);
        optionsIntent.putExtra("method","GET");
        optionsIntent.putExtra("from","CLOTHINGOPTIONS");
        optionsIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothingOptions/");
        startService(optionsIntent);

        listViewOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showDetailActivity = new Intent(getApplicationContext(), ClothingOptionsDetail.class);
                choosenOption = i;

                try {
                    JSONObject tmpObject = new JSONObject(clothingOptions.get(i).toString());
                    JSONArray tmpArray = tmpObject.getJSONArray("options");
                    JSONObject tmpObject2 = tmpArray.optJSONObject(0);
                    if(tmpObject2==null){OPT = 2;}else{OPT = 1;}
                    showDetailActivity.putExtra("items", tmpObject.toString());
                    showDetailActivity.putExtra("option", OPT);
                    startActivityForResult(showDetailActivity, CHOOSE_OPTION);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void fillListView(ArrayList<String> options) {
        ClothingOptionsAdapter optAdapter;
        optAdapter = new ClothingOptionsAdapter(this, options);
        listViewOptions.setAdapter(optAdapter);
        ListViewHelper.getListViewSize(listViewOptions);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:
                String art="", gender="" , size="", style="", color="", fabric="", brand="";

                for(int i=0;listViewOptions.getAdapter().getCount()>i;i++){
                    View tmpView = getViewByPosition(i,listViewOptions);
                    TextView nameTextView = (TextView) tmpView.findViewById(R.id.nameTextView);
                    TextView selectionTextView = (TextView) tmpView.findViewById(R.id.selectionTextView);
                    String tmpTopic = nameTextView.getText().toString();
                    String tmpSelection = selectionTextView.getText().toString();

                    switch(tmpTopic){
                        case "Art":
                            art = tmpSelection;
                            break;
                        case "Gender":
                            if(tmpSelection != "" && !size.isEmpty()){
                                switch(tmpSelection){
                                    case "Männlich Erwachsen":
                                    case "Männlich Kind":
                                        tmpSelection = "M";
                                        break;

                                    case "Weiblich Erwachsen":
                                    case "Weiblich Kind":
                                        tmpSelection = "W";
                                        break;

                                    case "Unisex Erwachsen":
                                    case "Unisex Kind":
                                        tmpSelection = "U";
                                        break;
                                }
                            }
                            gender = tmpSelection;
                            break;
                        case "Size":
                            size = tmpSelection;
                            break;
                        case "Style":
                            style = tmpSelection;
                            break;
                        case "Color":
                            color = tmpSelection;
                            break;
                        case "Fabric":
                            fabric = tmpSelection;
                            break;
                        case "Brand":
                            brand = tmpSelection;
                            break;
                    }
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("art",art);
                //resultIntent.putExtra("gender",gender);
                resultIntent.putExtra("size",size);
                resultIntent.putExtra("style",style);
                resultIntent.putExtra("color",color);
                //resultIntent.putExtra("fabric",fabric);
                resultIntent.putExtra("brand",brand);
                resultIntent.putExtra("distance",progressDist);
                setResult(CHOOSE_OPTION, resultIntent);
                finish();
                break;
            case R.id.btnCancel:
                Intent cancelIntent = new Intent();
                setResult(CHOOSE_OPTION, cancelIntent);
                finish();
                break;
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHOOSE_OPTION){
            if(data!=null) {
                String StringResult = data.getStringExtra("StringResult");
                String area = data.getStringExtra("area");
                View tmpView = getViewByPosition(choosenOption, listViewOptions);
                TextView selectionTextView = (TextView) tmpView.findViewById(R.id.selectionTextView);
                selectionTextView.setText(StringResult);
                selectionTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getStringExtra("from").equals("CLOTHINGOPTIONS")){
                String rawData = intent.getStringExtra("optionsData");
                try {
                    clothingOptions = new JSONArray(rawData);
                    for(int i=0;clothingOptions.length()>i;i++){
                        JSONObject tmpObject = new JSONObject(clothingOptions.get(i).toString());
                        clothingOptionsLevel1.add(tmpObject.getString("topic"));
                    }
                    fillListView(clothingOptionsLevel1);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            textViewProgress.setText("Entfernung in KM: " + i);
            progressDist = i;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
