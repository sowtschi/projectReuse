package com.th_koeln.steve.klamottenverteiler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.structures.Request;

import java.util.List;

/**
 * Created by Frank on 15.01.2018.
 */

public class RequestListAdapter extends ArrayAdapter<Request> {
    private static final String TAG = "RequestAdapter";
    private Context mContext;
    private int mRessource;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();

    public RequestListAdapter(Context context, int resource, List<Request> objects) {
        super(context, resource, objects);
        mContext=context;
        mRessource = resource;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();

        String size = getItem(position).getSize();
        String brand = getItem(position).getBrand();
        String status = getItem(position).getStatus();
        String from = getItem(position).getFrom();
        String ouId = getItem(position).getOuId();
        String confirmed = getItem(position).getConfirmed();
        String closed = getItem(position).getClosed();
        String finished = getItem(position).getFinished();
        String title = getItem(position).getTitle();
        String art = getItem(position).getArt();

        Request request = new Request(name, size, status, from, ouId, confirmed, closed, finished, title, art);


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mRessource,parent,false);

        TextView txtRequestName = (TextView) convertView.findViewById(R.id.txtRequestName);
        TextView txtRequestArt = (TextView) convertView.findViewById(R.id.txtRequestArt);
        TextView txtRequestSize = (TextView) convertView.findViewById(R.id.txtRequestSize);
        TextView txtRequestBrand = (TextView) convertView.findViewById(R.id.txtRequestBrand);
        TextView txtRequestStatus = (TextView) convertView.findViewById(R.id.txtRequestStatus);
        txtRequestName.setText("Name : " + title);
        txtRequestSize.setText("Size: " + size);
        txtRequestBrand.setText("Brand: " + brand);
        txtRequestArt.setText("Art:" + art);

        switch (status) {
            case "open":
                txtRequestStatus.setText("Status: " + "Waiting for response..");
                break;
            case "accepted":
                txtRequestStatus.setText("Ready to clarify details");
                break;
            case "waiting":
                if (confirmed.equals(uId)) {
                    txtRequestStatus.setText("Status: Waiting for confirmation..");
                } else {
                    txtRequestStatus.setText("Status: Waiting for your confirmation..");
                }
                break;
            case "confirmed":
                    txtRequestStatus.setText("Status: Request confirmed..");
                break;
            case "success":
                txtRequestStatus.setText("Status: Waiting for User Rating..");
            break;
            case "closed":
                txtRequestStatus.setText("Status: Request is finished..");

            default:
                txtRequestStatus.setText(status);
                break;
        }

        return convertView;
    }
}
