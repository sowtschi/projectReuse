package com.th_koeln.steve.klamottenverteiler.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Michael on 24.01.2018.
 */

public class RequestAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    //Kleidungsstuecke in einer Arraylist
    private ArrayList<ClothingOffer> clothingList;

    //Kontext und die Kleidungsliste als Constructor
    public RequestAdapter(Context c, ArrayList<ClothingOffer> clothingList) {
        this.clothingList = clothingList;
        this.mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return clothingList.size();
    }

    @Override
    public Object getItem(int i) {
        return clothingList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = mInflater.inflate(R.layout.show_request_layout, null);
        ClothingOffer cTmp = clothingList.get(i);

        //Kleidungsstueck von der jeweiligen Position holen
        ClothingOffer clothingoffer = clothingList.get(i);
        TextView textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
        TextView textViewSize = (TextView) v.findViewById(R.id.textViewSize);
        TextView textViewTime = (TextView) v.findViewById(R.id.textViewTime);
        ImageView imgShowClothingPicture = (ImageView) v.findViewById(R.id.imgShowClothingPicture);

        if(cTmp.getTitle()!="" && !cTmp.getTitle().isEmpty()) {
            textViewTitle.setText(cTmp.getTitle());
        }else{
            textViewTitle.setText(cTmp.getArt());
        }
        textViewSize.setText(cTmp.getSize());

        //Entweder wir die Distanz oder der Stoff angezeigt
        //Somit ist dieser Adapter mehrfach nutzbar, da die Distanz nicht immer gegeben ist
        if(cTmp.getDistance()>-100){
            textViewTime.setText(String.valueOf((int)cTmp.getDistance())+" KM entfernt");
        }else if(cTmp.getDistance()>-300){
            textViewTime.setText(cTmp.getFabric());
        }else if(cTmp.getDistance()>-400){
            textViewTime.setText(cTmp.getArt());
        }else{
            switch (cTmp.getStatus()) {
                case "open":textViewTime.setText("Status: " + "Waiting for response..");
                    break;
                case "accepted":textViewTime.setText("Status: Ready to clarify details");
                    break;
                case "waiting":
                    if (cTmp.getConfirmed().equals(cTmp.getuId())) {
                        textViewTime.setText("Status: Waiting for confirmation..");
                    } else {
                        textViewTime.setText("Status: Waiting for your confirmation..");
                    }
                    break;
                case "confirmed":textViewTime.setText("Status: Request confirmed..");
                    break;
                case "success":textViewTime.setText("Status: Waiting for User Rating..");
                    break;
                case "closed":textViewTime.setText("Status: Request is finished..");
                    break;
                default:textViewTime.setText("Status: "+cTmp.getStatus());
                    break;
            }
        }

        String getFromPath = cTmp.getImagePath();
        if(getFromPath!="" && !getFromPath.isEmpty()) {
            File imageFile = new File(getFromPath);
            byte[] bytes = new byte[(int) imageFile.length()];
            try{
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imageFile));
                DataInputStream duf = new DataInputStream(buf);
                duf.readFully(bytes);
            }catch (IOException e){
                e.printStackTrace();
            }
            Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imgShowClothingPicture.setImageBitmap(img);
        }
        return v;
    }
}