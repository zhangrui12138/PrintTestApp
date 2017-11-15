package com.lkl.cloudpos.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;
import com.lkl.cloudpos.aidl.manager.AidlDeviceManager;
import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;

public class MainActivity extends Activity  implements View.OnClickListener{

    private TextView mTvBatteryVoltage;
    private TextView mTvBatteryCurrent;
    private TextView mTvBatteryTemperature;
    private TextView mTvCoreTemperature;
    private Button mBtCoreTemperature;
    private Button mBtPrintBlank;
    private EditText mEtPrintBlank;
    private EditText mEtPrint;
    private Button mBtPrint;
    private EditText mEtPrintNum;
    private EditText mEtTimeInterval;
    private Button mBtPrintBlack;
    private Button mBtPrintText;
    private Button mBtPrintMap;
    private Button mBtStopPrint;
    private RadioGroup mRdDensity;
    private RadioButton mRbLight;
    private RadioButton mRbMedium;
    private RadioButton mRbDark;
    private Switch mSwitchCycle;


    private Message message;
    private String batteryVoltage;
    private String batteryCurrent;
    private String batteryTemperature;
    private String coreTemperature;
    private int mRadioButtonCheckedId;
    private String stringDensity;
    private int printNums;
    private int timeInterval;
    private int printNum;
    private boolean setButton;
    private static final String INPUT_BATTERY = "/sys/devices/platform/battery/FG_Battery_CurrentConsumption";
    private boolean ifStop;

    private int voltage;
    private int temperature;
    private String batteryTemperatureputBattery;
    private float inputBattery;
    private boolean ifCycle;
    private String TAG = "wqtest";
    private int intDensity=3;
    private String printData;
    private ToggleButton mBtCycle;
    private Context mContext;
    private AidlDeviceManager mAidlDeviceManage = null;
    private AidlPrinter printerDev = null;
    public static final String LKL_SERVICE_ACTION = "lkl_cloudpos_device_service";
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                voltage = intent.getIntExtra("voltage", 0);

                temperature = intent.getIntExtra("temperature", 0);
                int tens = temperature / 10;
                batteryTemperatureputBattery = Integer.toString(tens) + "." + (temperature - 10 * tens);

                inputBattery = getInputBattery();
//                Log.i(TAG, "BroadcastReceiver电池电流" + inputBattery+"    BroadcastReceiver电池温度"+batteryTemperatureputBattery
//                        +"      BroadcastReceiver电池电压:"+voltage);//电池电流

                mTvBatteryVoltage.setText(voltage + " mV");
                mTvBatteryTemperature.setText(batteryTemperatureputBattery + " ℃");
                mTvBatteryCurrent.setText(inputBattery / 10 + " mA");

                mHandler.removeMessages(0);
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    };

    private float getInputBattery() {
        char[] buffer = new char[1024];
        float batteryElectronic = 0;
        FileReader file = null;
        try {
            file = new FileReader(INPUT_BATTERY);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = Float.valueOf((new String(buffer, 0, len)));
            if (file != null) {
                file.close();
                file = null;
            }
        } catch (Exception e) {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.e(TAG, "getInputElectronic fail");
            }
        }
        return batteryElectronic;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0://更新界面的数据
                    IntentFilter mIntentFilter = new IntentFilter();
                    mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                    registerReceiver(mIntentReceiver, mIntentFilter);
                    break;
                case 1://更新按钮
                    if (setButton) {
                        Toast.makeText(MainActivity.this, "本页打印之后将停止打印。", Toast.LENGTH_SHORT).show();
                    }
                    mBtPrintBlack.setEnabled(setButton);
                    mBtPrintText.setEnabled(setButton);
                    mBtPrintMap.setEnabled(setButton);
                    mBtPrintBlank.setEnabled(setButton);
                    mBtPrint.setEnabled(setButton);
                    break;
                case 2:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "打印321。。。。");
        initial();
        mAidlDeviceManage = AidlDeviceManager.getInstance();
    }

    public void onDeviceConnected(AidlDeviceService serviceManager) {
        try {
            printerDev = AidlPrinter.Stub.asInterface(serviceManager.getPrinter());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //设别服务连接桥
    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            Log.d("zhangrui","aidlService服务连接成功");
            if(serviceBinder != null){	//绑定成功
                AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
                onDeviceConnected(serviceManager);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("zhangrui","AidlService服务断开了");
        }
    };



    //绑定服务
    public void bindService(){
        Intent intent = new Intent();
        intent.setAction(LKL_SERVICE_ACTION);
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this,intent));
        boolean flag = bindService(eintent, conn, Context.BIND_AUTO_CREATE);
        if(flag){
            Log.d("zhangrui","服务绑定成功");
        }else{
            Log.d("zhangrui","服务绑定失败");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        bindService();
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(conn);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initial() {
        mTvBatteryVoltage = (TextView) findViewById(R.id.tv_battery_voltage);//电池电压 mV
        mTvBatteryCurrent = (TextView) findViewById(R.id.tv_battery_current);//电池电流 mA
        mTvBatteryTemperature = (TextView) findViewById(R.id.tv_Battery_temperature);//电池温度 ℃
        mTvCoreTemperature = (TextView) findViewById(R.id.tv_core_temperature);//机芯温度 ℃
        mBtCoreTemperature = (Button) findViewById(R.id.bt_core_temperature);
        mEtPrintBlank = (EditText) findViewById(R.id.et_print_blank);//走纸测试 空走纸行数
        mBtPrintBlank = (Button) findViewById(R.id.bt_print_blank);
        mEtPrint = (EditText) findViewById(R.id.et_print);//按输入打印打印
        mBtPrint = (Button) findViewById(R.id.bt_print);
        mEtPrintNum = (EditText) findViewById(R.id.et_print_num);//打印张数
        mBtCycle = (ToggleButton) findViewById(R.id.bt_cycle_print);//循环打印
        mEtTimeInterval = (EditText) findViewById(R.id.et_time_interval);//打印时间间隔
        mBtPrintBlack = (Button) findViewById(R.id.bt_print_black); //打印纯黑
        mBtPrintText = (Button) findViewById(R.id.bt_print_text); //打印文字
        mBtPrintMap = (Button) findViewById(R.id.bt_print_map); //打印图片
        mBtStopPrint = (Button) findViewById(R.id.bt_stop_print); //停止打印
        mRdDensity = (RadioGroup) findViewById(R.id.rd_density);
        mRbLight = (RadioButton) findViewById(R.id.rb_light);
        mRbMedium = (RadioButton) findViewById(R.id.rb_medium);
        mRbDark = (RadioButton) findViewById(R.id.rb_dark);

        mBtCoreTemperature.setOnClickListener(this);
        mBtPrintBlank.setOnClickListener(this);
        mBtPrint.setOnClickListener(this);
        mBtPrintBlack.setOnClickListener(this);
        mBtPrintText.setOnClickListener(this);
        mBtPrintMap.setOnClickListener(this);
        mBtCycle.setOnClickListener(this);
        mBtStopPrint.setOnClickListener(this);
        mRdDensity.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    public void onClick(View v) {
        getSetData();
        switch (v.getId()) {
            case R.id.bt_core_temperature:
                Toast.makeText(this, "机芯温度无法获得", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_print_blank:
                setButton(false);
                printBlank();
                break;
            case R.id.bt_print:
                setPrintHGray(intDensity);
                setButton(false);
                printStart();
                printData = mEtPrint.getText().toString();
                if (printData != null && printData.length() != 0) {
                    printInputText(printData);
                }
                printEnd();
                break;
            case R.id.bt_print_black:
                setButton(false);
                setPrintHGray(4);
                Log.i(TAG, "点击了打印纯黑");
                printStart();
                printBlack();
                break;
            case R.id.bt_print_text:
                setButton(false);
                setPrintHGray(intDensity);
                Log.i(TAG, "点击了打印文字");
                printStart();
                printText();
                break;
            case R.id.bt_print_map:
                setButton(false);
                setPrintHGray(intDensity);
                Log.i(TAG, "点击了打印图片");
//                printStart();
                printMap();
                break;
            case R.id.bt_stop_print:
                Log.i(TAG, "本页打印之后将停止打印。");
                ifStop = true;
                setButton(true);
                break;
            case R.id.bt_cycle_print:
//                String Cycle = mBtCycle.getText().toString();
//                if (Cycle.equals("正在循环打印")){
//                    mBtCycle.setText("已停止循环打印");
//                    ifCycle = false;
//                } else if (Cycle.equals("已停止循环打印")){
//                    mBtCycle.setText("正在循环打印");
//                    ifCycle = true;
//                }
                ifCycle=mBtCycle.isChecked();
                Log.d("zhangrui","recycle="+mBtCycle.isChecked());
                break;
        }
    }

    //打印字符串
    public void printInputText(String message){
        List<PrintItemObj> messageInput=new ArrayList<PrintItemObj>(){};
        messageInput.add(new PrintItemObj(message));
        try{
            if(printerDev != null){
                printerDev.printText(messageInput,new AidlPrinterListener.Stub() {

                    @Override
                    public void onPrintFinish() throws RemoteException {
                        Toast.makeText(MainActivity.this,"打印完成",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        Toast.makeText(MainActivity.this,"打印出错，错误码为： + arg0",Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(MainActivity.this,"printerDev == null",Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setButton(boolean b) {
        Log.i(TAG,"setButton:"+b);
        setButton = b;
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessageDelayed(1, 0);
    }

    private int picNums=0;

    private void printMap() {
        if (ifStop) {
            return;
        } else {
            Log.i(TAG, "printMap  printNum  :" + printNum + "     printNums:"+printNums+"     ifCycle:"+ifCycle+"    timeInterval="+timeInterval+"picNums="+picNums);
            if ((printNum < printNums)||ifCycle) {
                if(picNums < new PrintPicList().size()) {
                    printBitmaps(picNums);
                }else {
                    printEnd();
                }
            }
            else {
                printEnd();
            }

        }
    }

    //打印图片
    public void printBitmaps(int picNumer){
        try {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.pic_1);
//            printStart();
//            this.printerDev.printBmp(0, bitmap.getWidth(), bitmap.getHeight(), bitmap, mListen);
//            printEnd();
            printStart();
            Bitmap bitmap1 = new PrintPicList().get(picNumer);
            this.printerDev.printBmp(0, bitmap1.getWidth(), bitmap1.getHeight(), bitmap1, mListen);
//            printEnd();
//            printStart();
//            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.pic_3);
//            this.printerDev.printBmp(0, bitmap2.getWidth(), bitmap2.getHeight(), bitmap2, mListen);
//            printEnd();
//            printStart();
//            Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(),R.drawable.pic_4);
//            this.printerDev.printBmp(0, bitmap3.getWidth(), bitmap3.getHeight(), bitmap3, mListen);
//            printEnd();
//            printStart();
//            Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(),R.drawable.pic_5);
//            this.printerDev.printBmp(0, bitmap4.getWidth(), bitmap4.getHeight(), bitmap4, mListen);
//            printEnd();
//            printStart();
//            Bitmap bitmap5 = BitmapFactory.decodeResource(getResources(),R.drawable.pic_6);
//            this.printerDev.printBmp(0, bitmap5.getWidth(), bitmap5.getHeight(), bitmap5, mListen);
//            printEnd();
//            printStart();
//            Bitmap bitmap6 = BitmapFactory.decodeResource(getResources(),R.drawable.pic_7);
//            this.printerDev.printBmp(0, bitmap6.getWidth(), bitmap6.getHeight(), bitmap6, mListen);
//            printEnd();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //打印图片的监听器
    AidlPrinterListener mListen =new AidlPrinterListener.Stub() {
        @Override
        public void onError(int i) throws RemoteException {
            Log.d("zhangrui","打印出错，错误码为：" + i);
        }

        @Override
        public void onPrintFinish() throws RemoteException {
            Log.d("zhangrui","打印完成"+"/////picNums="+picNums+"/////new PrintPicList().size()="+new PrintPicList().size());
            if(picNums<new PrintPicList().size()-1){
                picNums++;
            }else {
                printNum++;
                picNums=0;
            }
            SystemClock.sleep(timeInterval*1000);
            printMap();
        }
    };


    private void printText() {
        if (ifStop) {
            return;
        } else {
            if ((printNum < printNums)||ifCycle) {
                printTexter();
            } else {
                printEnd();
            }
        }
    }

    public void printTexter(){
        Log.i(TAG, "printText  printNum  :" + printNum + "     printNums:"+printNums+"     ifCycle123:"+ifCycle);
        try {
            printerDev.printText(new PrintList(), new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    Log.d("wqtest","打印完成");
                    printNum++;
                    SystemClock.sleep(timeInterval*1000);
                    printText();
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    Log.d("wqtest","打印出错，错误码为：" + arg0);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public void printBlack() {
        if (ifStop) {
            return;
        } else {
            Log.i(TAG, "printBlack  printNum  :" + printNum + "     printNums:"+printNums+"     ifCycle:"+ifCycle);
            if ((printNum < printNums)||ifCycle) {
                try {
                    printerDev.printText(new PrintList(), new AidlPrinterListener.Stub() {

                        @Override
                        public void onPrintFinish() throws RemoteException {
                            Log.d("zhangrui","打印完成");
                            printNum++;
                            SystemClock.sleep(timeInterval*1000);
                            printBlack();
                        }

                        @Override
                        public void onError(int arg0) throws RemoteException {
                            Log.d("zhangrui","打印出错，错误码为：" + arg0);
                        }
                    });
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                printEnd();
            }
        }
    }


    public void printStart() {
        Log.i(TAG, "printStart:"  );
        try {
       printerDev.printText(new PrintTemptureList(), new AidlPrinterListener.Stub() {

           @Override
           public void onPrintFinish() throws RemoteException {
               Log.d("print","打印完成");
           }

           @Override
           public void onError(int arg0) throws RemoteException {
               Log.d("print","打印出错，错误码为：" + arg0);
           }
       });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printEnd() {
        Log.i(TAG, "printEnd:"  );
        try {
            printerDev.printText(new PrintTemptureEndList(), new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    Log.d("print","打印完成");
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    Log.d("print","打印出错，错误码为：" + arg0);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setButton(true);
    }

    private void getSetData() {
        Log.i(TAG, "getSetData:"  );
        printNum = 0;
        batteryVoltage = mTvBatteryVoltage.getText().toString();
        batteryCurrent = mTvBatteryCurrent.getText().toString();
        batteryTemperature = mTvBatteryTemperature.getText().toString();
        coreTemperature = mTvCoreTemperature.getText().toString();
        switch (mRadioButtonCheckedId) {
            case R.id.rb_light:
                stringDensity = mRbLight.getText().toString();
                intDensity = 1;
                break;
            case R.id.rb_medium:
                stringDensity = mRbMedium.getText().toString();
                intDensity = 3;
                break;
            case R.id.rb_dark:
                stringDensity = mRbDark.getText().toString();
                intDensity = 4;
                break;
        }
        String printNumbers=mEtPrintNum.getText().toString();
        if(printNumbers == null || printNumbers.length()==0){printNumbers="3";}
        printNums = stringToInt(printNumbers);
        Log.i(TAG, "打印张数" + printNums);
        timeInterval = stringToInt(mEtTimeInterval.getText().toString());
        Log.i(TAG, "打印间隔" + timeInterval);
        ifStop = false;
    }

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            mRadioButtonCheckedId = checkedId;
        }
    };

    //打印灰度为grayLevel
    public void setPrintHGray(int grayLevel){
        try {
            printerDev.setPrinterGray(grayLevel);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printBlank() {
        int blankNum = stringToInt(mEtPrintBlank.getText().toString());
        if(blankNum>0 && blankNum<960) {
            int i = 0;
            try {
                if (printerDev != null) {
                    while (i < blankNum) {
                        printerDev.printText(new PrintBlankList(), new AidlPrinterListener.Stub() {

                            @Override
                            public void onPrintFinish() throws RemoteException {
                                Toast.makeText(MainActivity.this, "打印完成", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int arg0) throws RemoteException {
                                Toast.makeText(MainActivity.this, "打印出错，错误码为： + arg0", Toast.LENGTH_SHORT).show();
                            }
                        });
                        i++;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "printerDev == null", Toast.LENGTH_SHORT).show();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this,"走纸只能在1~960的范围内!!",Toast.LENGTH_SHORT).show();
        }
        setButton(true);
    }


    public int stringToInt(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (!hasFocus) {
//            ifStop = true;
//            setButton(true);
//        }
//
//    }


    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    class PrintList extends ArrayList<PrintItemObj> {
        PrintList() {
            add(new PrintItemObj("默认打印数据测试"));
            add(new PrintItemObj("默认打印数据测试"));
            add(new PrintItemObj("默认打印数据测试"));
            add(new PrintItemObj("打印数据字体放大", 24));
            add(new PrintItemObj("打印数据字体放大", 24));
            add(new PrintItemObj("打印数据字体放大", 24));
            add(new PrintItemObj("打印数据加粗", 8, true));
            add(new PrintItemObj("打印数据加粗", 8, true));
            add(new PrintItemObj("打印数据加粗", 8, true));
            add(new PrintItemObj("打印数据左对齐测试", 8, false, PrintItemObj.ALIGN.LEFT));
            add(new PrintItemObj("打印数据左对齐测试", 8, false, PrintItemObj.ALIGN.LEFT));
            add(new PrintItemObj("打印数据左对齐测试", 8, false, PrintItemObj.ALIGN.LEFT));
            add(new PrintItemObj("打印数据居中对齐测试", 8, false, PrintItemObj.ALIGN.CENTER));
            add(new PrintItemObj("打印数据居中对齐测试", 8, false, PrintItemObj.ALIGN.CENTER));
            add(new PrintItemObj("打印数据居中对齐测试", 8, false, PrintItemObj.ALIGN.CENTER));
            add(new PrintItemObj("打印数据右对齐测试", 8, false, PrintItemObj.ALIGN.RIGHT));
            add(new PrintItemObj("打印数据右对齐测试", 8, false, PrintItemObj.ALIGN.RIGHT));
            add(new PrintItemObj("打印数据右对齐测试", 8, false, PrintItemObj.ALIGN.RIGHT));
            add(new PrintItemObj("打印数据下划线", 8, false, PrintItemObj.ALIGN.LEFT, true));
            add(new PrintItemObj("打印数据下划线", 8, false, PrintItemObj.ALIGN.LEFT, true));
            add(new PrintItemObj("打印数据下划线", 8, false, PrintItemObj.ALIGN.LEFT, true));
            add(new PrintItemObj("打印数据不换行测试打印数据不换行测试打印数据不换行测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true));
            add(new PrintItemObj("打印数据不换行测试打印数据不换行测试打印数据不换行测试", 8, false, PrintItemObj.ALIGN.LEFT, false, false));
            add(new PrintItemObj("打印数据不换行测试", 8, false, PrintItemObj.ALIGN.LEFT, false, false));
            add(new PrintItemObj("打印数据行间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 40));
            add(new PrintItemObj("打印数据行间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 83));
            add(new PrintItemObj("打印数据行间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 40));
            add(new PrintItemObj("打印数据字符间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 25));
            add(new PrintItemObj("打印数据字符间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 25));
            add(new PrintItemObj("打印数据字符间距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 25));
            add(new PrintItemObj("打印数据左边距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 0, 40));
            add(new PrintItemObj("打印数据左边距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 0, 40));
            add(new PrintItemObj("打印数据左边距测试", 8, false, PrintItemObj.ALIGN.LEFT, false, true, 29, 0, 40));
            add(new PrintItemObj("\n\n\n\n\n\n"));
        }
    }

    class PrintBlankList extends ArrayList<PrintItemObj> {
        PrintBlankList() {
            add(new PrintItemObj("\n"));
        }
    }

    class PrintTemptureList extends ArrayList<PrintItemObj> {
        PrintTemptureList() {
            add(new PrintItemObj("电池电压:"+batteryVoltage));
            add(new PrintItemObj("电池电流:"+batteryCurrent));
            add(new PrintItemObj("电池温度:"+batteryTemperature));
            add(new PrintItemObj("机芯温度:"+coreTemperature));
        }
    }
    class PrintTemptureEndList extends ArrayList<PrintItemObj> {
        PrintTemptureEndList() {
            add(new PrintItemObj("电池电压:"+batteryVoltage));
            add(new PrintItemObj("电池电流:"+batteryCurrent));
            add(new PrintItemObj("电池温度:"+batteryTemperature));
            add(new PrintItemObj("机芯温度:"+coreTemperature));
            add(new PrintItemObj("\n\n\n"));
        }
    }
    class PrintPicList extends ArrayList<Bitmap> {
        PrintPicList() {
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_2));
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_3));
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_4));
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_5));
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_6));
            add(BitmapFactory.decodeResource(getResources(),R.drawable.pic_7));
        }
    }

}