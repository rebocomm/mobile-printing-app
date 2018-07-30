package com.rebocomm.rebocomm.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rebocomm.rebocomm.R;
import com.rebocomm.rebocomm.models.Device;

import java.util.List;

public class NetPrinterAdapter extends
        RecyclerView.Adapter<NetPrinterAdapter.MyViewHolder> {

    private List<Device> printers;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView mac;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            mac = view.findViewById(R.id.mac);
        }
    }

    public NetPrinterAdapter(List<Device> countryList) {
        this.printers = countryList;
    }
    public void setList(List<Device> countryList) {
        this.printers = countryList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Device c = printers.get(position);
        holder.name.setText(c.deviceName);
        holder.mac.setText(String.valueOf(c.deviceAddress));
    }


    @Override
    public int getItemCount() {
        return printers.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_print, parent, false);
        return new MyViewHolder(v);
    }
}