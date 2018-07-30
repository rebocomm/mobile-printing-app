package com.rebocomm.rebocomm.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rebocomm.rebocomm.R;
import com.rebocomm.rebocomm.adapter.NetPrinterAdapter;
import com.rebocomm.rebocomm.common.Common;
import com.rebocomm.rebocomm.helper.PrinterClass;
import com.rebocomm.rebocomm.helper.RecyclerItemListener;
import com.rebocomm.rebocomm.models.Device;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrinterSettingActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.no_device)
    TextView noDevice;


    public static List<Device> deviceList = new ArrayList<Device>();
    NetPrinterAdapter printerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setting);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        printerAdapter = new NetPrinterAdapter(deviceList);
        recyclerView.setAdapter(printerAdapter);

        if (MainActivity.pl != null) {
            if (!MainActivity.pl.IsOpen()) {
                MainActivity.pl.open(this);
            }
            if (deviceList != null) {
                deviceList.clear();
            }
            MainActivity.pl.scan();
            deviceList = MainActivity.pl.getDeviceList();
            printerAdapter.setList(deviceList);
            printerAdapter.notifyDataSetChanged();
        }
        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), recyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View v, int position) {
                        Device printer = deviceList.get(position);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("printer", printer.modelName);
//                        editor.putString("address", printer.ipAddress);
//                        editor.putString("macAddress", printer.macAddress);
//                        editor.apply();
                        SharedPreferences.Editor editor = getSharedPreferences("REBOCOMM", MODE_PRIVATE).edit();
                        editor.putString("mac_printer", printer.deviceAddress);
                        editor.apply();

                        MainActivity.pl.connect(printer.deviceAddress);
                        Toast.makeText(PrinterSettingActivity.this, printer.deviceName, Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    }
                    public void onLongClickItem(View v, int position) {
                    }
                }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.printer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (!MainActivity.pl.IsOpen()) {
                    MainActivity.pl.open(this);
                }
                if (deviceList != null) {
                    deviceList.clear();
                }
                MainActivity.pl.scan();
                deviceList = MainActivity.pl.getDeviceList();
                printerAdapter.setList(deviceList);
                printerAdapter.notifyDataSetChanged();

                return true;
            case R.id.menu_setting:
                Intent bluetoothSettings = new Intent(
                        android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(bluetoothSettings,
                        Common.ACTION_BLUETOOTH_SETTINGS);
                return true;
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
