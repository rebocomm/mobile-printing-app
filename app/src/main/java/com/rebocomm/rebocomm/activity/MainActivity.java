package com.rebocomm.rebocomm.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rebocomm.rebocomm.R;
import com.rebocomm.rebocomm.common.Utils;
import com.rebocomm.rebocomm.helper.BarcodeCreater;
import com.rebocomm.rebocomm.helper.PrintService;
import com.rebocomm.rebocomm.helper.PrinterClass;
import com.rebocomm.rebocomm.helper.PrinterClassFactory;
import com.rebocomm.rebocomm.helper.PrinterCommands;
import com.rebocomm.rebocomm.models.Device;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edt_barcode)
    EditText edt_barcode;

    @BindView(R.id.edt_receiver)
    EditText edt_receiver;

    @BindView(R.id.edt_phone)
    EditText edt_phone;

    @BindView(R.id.edt_address)
    EditText edt_address;

    @BindView(R.id.btn_print)
    Button btn_print;

    @BindView(R.id.textView_state)
    TextView textView_state;

    Handler mhandler = null;
    Handler handler = null;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    protected static final String TAG = "MainActivity";

    public static PrinterClass pl = null;

    private Bitmap btMap = null;

    private Thread tv_update;
    public static boolean checkState = true;
    public static boolean isCanPrint = false;

    // Nhan be 370, 210
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        edt_barcode.setText("12345678");
        edt_phone.setText("0907490047");
        edt_receiver.setText("Bùi Tiến Dũng");
        edt_address.setText("305 Tô Hiến Thành, Phường 12, Quận 10, Hồ Chí Minh");

        mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        Log.i(TAG, "readBuf:" + readBuf[0]);
                        if (readBuf[0] == 0x13) {
                         /*SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");
                         Log.i(TAG, "0x13:"+sDateFormat.format(new java.util.Date()));*/
                            PrintService.isFUll = true;
                        } else if (readBuf[0] == 0x11) {
                            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");
                            Log.i(TAG, "0x11:" + sDateFormat.format(new java.util.Date()));
                            PrintService.isFUll = false;
                        } else {
                            // construct a string from the valid bytes in the buffer
                            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");
                            Log.i(TAG, "DATA:" + sDateFormat.format(new java.util.Date()));
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            Toast.makeText(getApplicationContext(), readMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MESSAGE_STATE_CHANGE:// 蓝牙连接状
                        switch (msg.arg1) {
                            case PrinterClass.STATE_CONNECTED:// 已经连接
                                Log.i(TAG, "STATE_CONNECTED");
                                canNotPrint();
                                break;
                            case PrinterClass.STATE_CONNECTING:// 正在连接
                                Log.i(TAG, "STATE_CONNECTING");
                                canNotPrint();
                                break;
                            case PrinterClass.STATE_LISTEN:
                            case PrinterClass.STATE_NONE:
                                Log.i(TAG, "STATE_NONE");
                                canNotPrint();
                                break;
                            case PrinterClass.SUCCESS_CONNECT:
                                Log.i(TAG, "SUCCESS_CONNECT");
                                canPrint();
                                break;
                            case PrinterClass.FAILED_CONNECT:
                                Log.i(TAG, "FAILED_CONNECT");
                                canNotPrint();
                                break;
                            case PrinterClass.LOSE_CONNECT:
                                canNotPrint();
                                Log.i(TAG, "LOSE_CONNECT");
                        }
                        break;
                    case MESSAGE_WRITE:

                        break;
                }
                super.handleMessage(msg);
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        break;
                    case 1:// 扫描完毕
                        Device d = (Device) msg.obj;
                        if (d != null) {
//                            if(PrintSettingActivity.deviceList==null)
//                            {
//                                PrintSettingActivity.deviceList=new ArrayList<Device>();
//                            }
//
//                            if(!checkData(PrintSettingActivity.deviceList,d))
//                            {
//                                PrintSettingActivity.deviceList.add(d);
//                            }
                        }
                        break;
                    case 2:// 停止扫描
                        break;
                }
            }
        };
        textView_state.setVisibility(View.GONE);
        tv_update = new Thread() {
            public void run() {
                while (true) {
                    if (checkState) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        textView_state.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (MainActivity.pl != null) {
                                    if (MainActivity.pl.getState() == PrinterClass.STATE_CONNECTED) {
                                        textView_state.setText(MainActivity.this
                                                .getResources().getString(
                                                        R.string.str_connected));
                                    } else if (MainActivity.pl.getState() == PrinterClass.STATE_CONNECTING) {
                                        textView_state.setText(MainActivity.this
                                                .getResources().getString(
                                                        R.string.str_connecting));
                                    } else if (MainActivity.pl.getState() == PrinterClass.LOSE_CONNECT
                                            || MainActivity.pl.getState() == PrinterClass.FAILED_CONNECT) {
                                        checkState = false;
                                        textView_state.setText(MainActivity.this
                                                .getResources().getString(
                                                        R.string.str_disconnected));
                                        Intent i = new Intent(MainActivity.this, PrinterSettingActivity.class);
                                        startActivity(i);
                                        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                    } else {
                                        textView_state.setText(MainActivity.this
                                                .getResources().getString(
                                                        R.string.str_disconnected));
                                    }
                                }
                            }
                        });
                    }
                }
            }
        };
        tv_update.start();

        this.pl = PrinterClassFactory.create(0, this, mhandler, handler);

        SharedPreferences prefs = getSharedPreferences("REBOCOMM", MODE_PRIVATE);
        String mac_printer = prefs.getString("mac_printer", null);
        if (mac_printer != null) {
            MainActivity.pl.connect(mac_printer);
        }
    }

    private void canPrint() {
        isCanPrint = true;
//        btn_print.setEnabled(true);
        btn_print.setTextColor(getResources().getColor(R.color.white));
        btn_print.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button_round_rect_border));

        // set trang giay
        printCMD("1F 1B 1F 80 04 05 06 44");
    }

    private void canNotPrint() {
        isCanPrint = false;
//        btn_print.setEnabled(false);
        btn_print.setTextColor(getResources().getColor(R.color.colorPrimary));
        btn_print.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button_round_rect_border_dis));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.print:
                Intent i = new Intent(this, PrinterSettingActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btn_print)
    public void actionPrint(Button button) {

        if (isCanPrint) {
            String barcode = edt_barcode.getText().toString();
            String receiver = edt_receiver.getText().toString();
            String phone = edt_phone.getText().toString();
            String address = edt_address.getText().toString();

            if (barcode.isEmpty()) {
                Toast.makeText(this, getString(R.string.barcode_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (receiver.isEmpty()) {
                Toast.makeText(this, getString(R.string.receiver_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.isEmpty()) {
                Toast.makeText(this, getString(R.string.phone_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (address.isEmpty()) {
                Toast.makeText(this, getString(R.string.address_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            printPhoto(R.drawable.logo);
            this.pl.write(PrinterCommands.FEED_LINE);
            printBarcode(barcode);
            this.pl.write("--------------------------------".getBytes());
            this.pl.write(PrinterCommands.FEED_LINE);
            printInfo(receiver, phone, address);
            this.pl.write("--------------------------------".getBytes());
            this.pl.write(PrinterCommands.FEED_LINE);
            printFooter("Cho xem hàng, không cho thử");
            this.pl.write(PrinterCommands.FEED_LINE);
            this.pl.write(PrinterCommands.FEED_LINE);
//            printInfo3();
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Đã gửi lệnh In!")
                    .setConfirmText("OK")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
//                            edt_barcode.setText("");
//                            edt_receiver.setText("");
//                            edt_phone.setText("");
//                            edt_address.setText("");
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        } else {
            Intent i = new Intent(this, PrinterSettingActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        }

    }
    public void printCMD(String cmdStr){
        byte[] btcmd = hexStringToBytes(cmdStr);
        MainActivity.pl.write(btcmd);
    }
    /**
     * hex String to byte array
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    img);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public void printBarcode(String barcode) {
        try {
            Bitmap bmp = BarcodeCreater.creatBarcode(this,
                    barcode, 384, 60, true, 1);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public void printInfo2() {
        try {

            TextPaint paintBold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold.setColor(Color.rgb(0, 0, 0));
            paintBold.setTextSize(30);
            paintBold.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//
//            TextPaint paintBold2 = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//            paintBold2.setColor(Color.rgb(0, 0, 0));
//            paintBold2.setTextSize(26);
//            paintBold2.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//
            StaticLayout lbContactName = new StaticLayout(
                    "123456", paintBold, 300, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
//            StaticLayout contactName = new StaticLayout(
//                    "Người nhận: ", paintBold2, 384, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//            StaticLayout lbAddress = new StaticLayout(
//                    "Người nhận: ", paintBold, 384, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//
//            float yT = 0;
//            yT = yT + lbContactName.getHeight() + 5;
//            yT = yT + contactName.getHeight() + 5;
//            yT = yT + lbAddress.getHeight() + 5;

            Bitmap bitmap = Bitmap.createBitmap(370, 212, Bitmap.Config.ARGB_8888);
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStrokeWidth(2);

            canvas.drawLine(0, 0, 0, canvas.getHeight(), p);
            canvas.drawLine(0, 0, canvas.getWidth(), 0, p);
            canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), p);
            canvas.drawLine(canvas.getWidth(), canvas.getHeight() - 1,0, canvas.getHeight()-1, p);

            Bitmap bmp = BarcodeCreater.creatBarcode(this,
                    "123456", 300, 100, true, 1);

            Rect src = new Rect(0, 0, 300, 100);
            Rect dst = new Rect(35, 35, canvas.getWidth() - 35, 35 + 100);
            canvas.drawBitmap(bmp, src, dst, p);

//            float y = 0;
//            int lbContactNameHeight = lbContactName.getHeight();
            canvas.save();
            canvas.translate(35, 40 + 100);
            lbContactName.draw(canvas);
            canvas.restore();
//
//            y = y + lbContactNameHeight + 5;
//            canvas.save();
//            canvas.translate(0, y);
//            contactName.draw(canvas);
//            canvas.restore();
//
//            y = y + contactName.getHeight() + 5;
//            canvas.save();
//            canvas.translate(0, y);
//            lbAddress.draw(canvas);
//            canvas.restore();

            if (bitmap != null) {
                byte[] command = Utils.decodeBitmap(bitmap);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }
    public void printInfo3() {
        try {

            TextPaint paintBold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold.setColor(Color.rgb(0, 0, 0));
            paintBold.setTextSize(30);
            paintBold.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//
            TextPaint paintBold2 = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold2.setColor(Color.rgb(0, 0, 0));
            paintBold2.setTextSize(24);
            paintBold2.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
//
            StaticLayout lbContactName = new StaticLayout(
                    "123456", paintBold, 300, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

//            float yT = 0;
//            yT = yT + lbContactName.getHeight() + 5;
//            yT = yT + contactName.getHeight() + 5;
//            yT = yT + lbAddress.getHeight() + 5;
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.logo);

            Bitmap bitmap = Bitmap.createBitmap(370, 214, Bitmap.Config.ARGB_8888);
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStrokeWidth(2);

            StaticLayout contactName = new StaticLayout(
                    "Máy in nhiệt", paintBold, canvas.getWidth() - 110, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            StaticLayout lbAddress = new StaticLayout(
                    "Đây là nhãn test máy in nhiệt, xin nhẹ tay!", paintBold2, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            //logo
            Rect srcLogo = new Rect(0, 0, 100, 32);
            Rect dstLogo = new Rect(0, 0,100,32);
            canvas.drawBitmap(Bitmap.createScaledBitmap(icon, 100, 32, false), srcLogo, dstLogo, p);


//            canvas.drawLine(0, 0, 0, canvas.getHeight(), p);
//            canvas.drawLine(0, 0, canvas.getWidth(), 0, p);
//            canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), p);
//            canvas.drawLine(canvas.getWidth(), canvas.getHeight() - 1,0, canvas.getHeight()-1, p);

            Bitmap bmp = BarcodeCreater.creatBarcode(this,
                    "123456", 300, 100, true, 1);

            Rect src = new Rect(0, 0, 300, 60);
            Rect dst = new Rect(35, 45, canvas.getWidth() - 35, 45 + 60);
            canvas.drawBitmap(bmp, src, dst, p);

//            float y = 0;
//            int lbContactNameHeight = lbContactName.getHeight();
            canvas.save();
            canvas.translate(35, 50 + 60);
            lbContactName.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(110, 32/2 - contactName.getHeight()/2);
            contactName.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(0, lbContactName.getHeight() + 50 + 60);
            lbAddress.draw(canvas);
            canvas.restore();

            if (bitmap != null) {
                byte[] command = Utils.decodeBitmap(bitmap);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }
    public void printInfo4() {
        try {

            TextPaint paintBold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold.setColor(Color.rgb(0, 0, 0));
            paintBold.setTextSize(30);
            paintBold.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//
            TextPaint paintBold2 = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold2.setColor(Color.rgb(0, 0, 0));
            paintBold2.setTextSize(20);
            paintBold2.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
//
            StaticLayout lbContactName = new StaticLayout(
                    "123456", paintBold, 300, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);


//
//            float yT = 0;
//            yT = yT + lbContactName.getHeight() + 5;
//            yT = yT + contactName.getHeight() + 5;
//            yT = yT + lbAddress.getHeight() + 5;
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.logo);

            Bitmap bitmap = Bitmap.createBitmap(370, 198, Bitmap.Config.ARGB_8888);
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStrokeWidth(2);

            StaticLayout contactName = new StaticLayout(
                    "Máy in nhiệt", paintBold, canvas.getWidth() - 110, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            StaticLayout lbAddress = new StaticLayout(
                    "Đây là nhãn test máy in nhiệt, xin nhẹ tay!", paintBold2, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            //logo
            Rect srcLogo = new Rect(0, 0, 100, 32);
            Rect dstLogo = new Rect(0, 0,100,32);
            canvas.drawBitmap(Bitmap.createScaledBitmap(icon, 100, 32, false), srcLogo, dstLogo, p);


//            canvas.drawLine(0, 0, 0, canvas.getHeight(), p);
//            canvas.drawLine(0, 0, canvas.getWidth(), 0, p);
//            canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), p);
//            canvas.drawLine(canvas.getWidth(), canvas.getHeight() - 1,0, canvas.getHeight()-1, p);

            Bitmap bmp = BarcodeCreater.creatBarcode(this,
                    "123456", 300, 100, true, 1);

            Rect src = new Rect(0, 0, 300, 60);
            Rect dst = new Rect(35, 45, canvas.getWidth() - 35, 45 + 60);
            canvas.drawBitmap(bmp, src, dst, p);

//            float y = 0;
//            int lbContactNameHeight = lbContactName.getHeight();
            canvas.save();
            canvas.translate(35, 50 + 60);
            lbContactName.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(110, 32/2 - contactName.getHeight()/2);
            contactName.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(0, lbContactName.getHeight() + 50 + 60);
            lbAddress.draw(canvas);
            canvas.restore();

            if (bitmap != null) {
                byte[] command = Utils.decodeBitmap(bitmap);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
                this.pl.write(PrinterCommands.FEED_LINE);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public void printInfo(String receiver, String phone, String address) {
        try {

            TextPaint paintBold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold.setColor(Color.rgb(0, 0, 0));
            paintBold.setTextSize(26);
            paintBold.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

            TextPaint paintBold2 = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold2.setColor(Color.rgb(0, 0, 0));
            paintBold2.setTextSize(26);
            paintBold2.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

            StaticLayout lbContactName = new StaticLayout(
                    "Người nhận: " + receiver, paintBold, 384, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            StaticLayout contactName = new StaticLayout(
                    phone, paintBold2, 384, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            StaticLayout lbAddress = new StaticLayout(
                    address, paintBold, 384, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            float yT = 0;
            yT = yT + lbContactName.getHeight() + 5;
            yT = yT + contactName.getHeight() + 5;
            yT = yT + lbAddress.getHeight() + 5;

            Bitmap bitmap = Bitmap.createBitmap(384, (int) Math.ceil(yT), Bitmap.Config.ARGB_8888);
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            float y = 0;
            int lbContactNameHeight = lbContactName.getHeight();
            canvas.save();
            canvas.translate(0, 0);
            lbContactName.draw(canvas);
            canvas.restore();

            y = y + lbContactNameHeight + 5;
            canvas.save();
            canvas.translate(0, y);
            contactName.draw(canvas);
            canvas.restore();

            y = y + contactName.getHeight() + 5;
            canvas.save();
            canvas.translate(0, y);
            lbAddress.draw(canvas);
            canvas.restore();

            if (bitmap != null) {
                byte[] command = Utils.decodeBitmap(bitmap);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public void printFooter(String text) {
        try {

            TextPaint paintBold2 = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintBold2.setColor(Color.rgb(0, 0, 0));
            paintBold2.setTextSize(23);
            paintBold2.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

            StaticLayout lbContactName = new StaticLayout(
                    text, paintBold2, 384, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            float yT = 0;
            yT = yT + lbContactName.getHeight() + 5;

            Bitmap bitmap = Bitmap.createBitmap(384, (int) Math.ceil(yT), Bitmap.Config.ARGB_8888);
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            float y = 0;
            int lbContactNameHeight = lbContactName.getHeight();
            canvas.save();
            canvas.translate(0, 0);
            lbContactName.draw(canvas);
            canvas.restore();

            if (bitmap != null) {
                byte[] command = Utils.decodeBitmap(bitmap);
                this.pl.write(PrinterCommands.ESC_ALIGN_CENTER);
                this.pl.write(command);
                this.pl.write(PrinterCommands.FEED_LINE);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }
}
