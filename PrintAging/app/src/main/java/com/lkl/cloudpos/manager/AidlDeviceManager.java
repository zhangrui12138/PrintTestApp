package com.lkl.cloudpos.aidl.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.lkl.cloudpos.aidl.AidlDeviceService;

public class AidlDeviceManager {

    private static AidlDeviceManager mAidlDeviceManager;
    private AidlDeviceService mAidlDeviceService;

    public static AidlDeviceManager getInstance() {
        if (mAidlDeviceManager == null) {
            mAidlDeviceManager = new AidlDeviceManager();
        }
        return mAidlDeviceManager;
    }

    public void bindAidlDeviceService(Context context) {
        Intent intent = new Intent();
        intent.setAction("lkl_cloudpos_device_service");
        intent.setPackage("com.android.topwise.lklusdkservice");
        try {
            context.bindService(intent, connection, context.BIND_AUTO_CREATE);
        } catch (Exception e) {
        }
    }

    public void unbindAidlDeviceService(Context context) {
        try {
            context.unbindService(connection);
        } catch (Exception e) {
        }
    }


    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAidlDeviceService = AidlDeviceService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAidlDeviceService = null;
        }
    };

    public IBinder getSystemService() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getSystemService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IBinder getMagCardReader() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getMagCardReader();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getPinPad(int devid) {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getPinPad(devid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getInsertCardReader() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getInsertCardReader();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getRFIDReader() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getRFIDReader();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IBinder getPSAMReader(int devid) {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getPSAMReader(devid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IBinder getSerialPort(int port) {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getSerialPort(port);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IBinder getPrinter() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getPrinter();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getEMVL2() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getEMVL2();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getShellMonitor() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getShellMonitor();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getCPUCard() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getCPUCard();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getExternalRFCardReader() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getExternalRFCardReader();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getPedestal() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getPedestal();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public IBinder getCameraManager() {
        try {
            if(mAidlDeviceService != null) {
                return mAidlDeviceService.getCameraManager();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

}
