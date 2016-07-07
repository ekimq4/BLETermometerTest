package com.q4tech.bletermometertest.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.q4tech.bletermometertest.Activity.MainActivity;
import com.q4tech.bletermometertest.Model.BleDeviceInfo;
import com.q4tech.bletermometertest.R;

import java.util.ArrayList;

/**
 * Created by ekim on 7/6/16.
 */
public class MainAdapter extends ArrayAdapter<BleDeviceInfo> {

    private Activity activity;
    private ArrayList data;
    public Resources res;
    BleDeviceInfo tempValues=null;
    LayoutInflater inflater;

    /*************  CustomAdapter Constructor *****************/
    public MainAdapter(MainActivity activitySpinner, int textViewResourceId, ArrayList objects, Resources resLocal) {
        super(activitySpinner, textViewResourceId, objects);

        /********** Take passed values **********/
        activity = activitySpinner;
        data     = objects;
        res      = resLocal;

        /***********  Layout inflator to call external xml layout () **********************/
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(R.layout.adapter_main_list, parent, false);

        /***** Get each Model object from Arraylist ********/
        tempValues = null;
        tempValues = (BleDeviceInfo) data.get(position);

        TextView textName = (TextView)row.findViewById(R.id.textName);
        TextView textAddress = (TextView)row.findViewById(R.id.textAddress);
        TextView textRssi = (TextView)row.findViewById(R.id.textRssi);

        if(position == 0){
            // Default selected Spinner item
            textName.setText(data.size() > 1 ? "Seleccione dispositivo..." : "No se ha encontrado ningun dispositivo.");
            textAddress.setText("");
            textRssi.setText("");
        }
        else
        {
            // Set values for spinner each row
            textName.setText(tempValues.getBluetoothDevice().getName() != null ? tempValues.getBluetoothDevice().getName() : "Unknown");
            textAddress.setText(tempValues.getBluetoothDevice().getAddress() != null ? tempValues.getBluetoothDevice().getAddress() : "Unknown");
            String dBm = String.valueOf(tempValues.getRssi()) + " dBm";
            textRssi.setText(dBm);
        }

        return row;
    }

}
