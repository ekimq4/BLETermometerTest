package com.q4tech.bletermometertest;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ekim on 7/6/16.
 */
public class MainAdapter extends BaseAdapter implements View.OnClickListener {
    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList<BleDeviceInfo> data;
    private static LayoutInflater inflater = null;
    public Resources res;
    BleDeviceInfo tempValues = null;

    /*************  CustomAdapter Constructor *****************/
    public MainAdapter(Activity a, ArrayList<BleDeviceInfo> d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data = d;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public ArrayList<BleDeviceInfo> getData () {return this.data;}
    public void setData(ArrayList<BleDeviceInfo> data) {this.data = data;}

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
        if(data == null)
            return 0;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public RelativeLayout adapterLayout;
        public TextView textName;
        public TextView textAddress;
        public TextView textRssi;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        final ViewHolder holder;

        if(convertView==null){

            /****** Inflate item_main_list.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.adapter_main_list, null);

            /****** View Holder Object to contain item_main_list.xml file elements ******/

            holder = new ViewHolder();
            holder.adapterLayout = (RelativeLayout)vi.findViewById(R.id.adapterLayout);
            holder.textName = (TextView)vi.findViewById(R.id.textName);
            holder.textAddress=(TextView)vi.findViewById(R.id.textAddress);
            holder.textRssi=(TextView)vi.findViewById(R.id.textRssi);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            holder.textName.setText("No Data");
            holder.textAddress.setText("");
            holder.textRssi.setText("");
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            tempValues = null;
            tempValues = (BleDeviceInfo) data.get( position );

            /************  Set Model values in Holder elements ***********/

            holder.textName.setText(tempValues.getBluetoothDevice().getName() != null ? tempValues.getBluetoothDevice().getName() : "Unknown");
            holder.textAddress.setText(tempValues.getBluetoothDevice().getAddress() != null ? tempValues.getBluetoothDevice().getAddress() : "Unknown");
            String dBm = String.valueOf(tempValues.getRssi()) + " dBm";
            holder.textRssi.setText(dBm);

            holder.adapterLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        holder.adapterLayout.setBackgroundColor(res.getColor(R.color.LightGray));
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        holder.adapterLayout.setBackgroundColor(res.getColor(R.color.Transparent));
                    }
                    return false;
                }
            });
            /******** Set Item Click Listener for LayoutInflater for each row *******/

            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            MainActivity sct = (MainActivity)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            //sct.onOrderClick(mPosition);
        }
    }

}
