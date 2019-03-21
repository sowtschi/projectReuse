package com.th_koeln.steve.klamottenverteiler.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Michael on 20.01.2018.
 */

public class ClothingOfferAdapter extends RecyclerView.Adapter<ClothingOfferAdapter.ClothingOfferViewHolder> {


    //Der Kontext in dem wir das Infalten vornehmen
    private Context con;

    //Kleidungsstuecke in einer Arraylist
    private ArrayList<ClothingOffer> clothingList;

    //Kontext und die Kleidungsliste als Constructor
    public ClothingOfferAdapter(Context c, ArrayList<ClothingOffer> clothingList) {
        this.con = c;
        this.clothingList = clothingList;
    }

    @Override
    public ClothingOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(con);
        View view = inflater.inflate(R.layout.search_clothing_layout, parent,false);
        return new ClothingOfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClothingOfferViewHolder holder, int position) {
        //Kleidungsstueck von der jeweiligen Position holen
        ClothingOffer clothingoffer = clothingList.get(position);

        if(clothingoffer.getTitle()!="" && !clothingoffer.getTitle().isEmpty()) {
            holder.textViewTitle.setText(clothingoffer.getTitle());
        }else{
            holder.textViewTitle.setText(clothingoffer.getArt());
        }
        holder.textViewSize.setText(clothingoffer.getSize());

        //Entweder wir die Distanz oder der Stoff angezeigt
        //Somit ist dieser Adapter mehrfach nutzbar, da die Distanz nicht immer gegeben ist
        if(clothingoffer.getDistance()>-100){
            holder.textViewTime.setText(String.valueOf((int)clothingoffer.getDistance())+" KM entfernt");
        }else if(clothingoffer.getDistance()>-300){
            holder.textViewTime.setText(clothingoffer.getFabric());
        }else if(clothingoffer.getDistance()>-400){
            holder.textViewTime.setText(clothingoffer.getArt());
        }

        String getFromPath = clothingoffer.getImagePath();
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

            holder.imgShowClothingPicture.setImageBitmap(img);
            holder.setIsRecyclable(false);
        }

    }


    @Override
    public int getItemCount() {
        return clothingList.size();
    }


    class ClothingOfferViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewSize, textViewTime;
        ImageView imgShowClothingPicture;

        public ClothingOfferViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            textViewSize = (TextView) itemView.findViewById(R.id.textViewSize);
            textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);
            imgShowClothingPicture = (ImageView) itemView.findViewById(R.id.imgShowClothingPicture);
        }
    }
}