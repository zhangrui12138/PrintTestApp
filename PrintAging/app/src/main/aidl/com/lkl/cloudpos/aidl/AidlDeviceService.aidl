package com.lkl.cloudpos.aidl;

interface AidlDeviceService{

    /** 获取系统服务接口  */
	IBinder getSystemService();
	
	/** 获取磁条卡设备操作实例  */
	IBinder getMagCardReader();
	
	/** 获取密码键盘操作实例  */
	IBinder getPinPad(int devid);		//参数标识内置外置密码键盘
	
	/** 接触式IC卡设备实例  */
	IBinder getInsertCardReader();
	
	/** 非接触式IC卡设备实例  */
	IBinder getRFIDReader();
	
	/** 获取PSAM卡设备操作实例 */
	IBinder getPSAMReader(int devid);
	
	/** 获取串口操作实例  */
	IBinder getSerialPort(int port);
	
	/** 获取打印机操作实例  */
	IBinder getPrinter();
	
	/** 获取EMV操作实例  */
	IBinder getEMVL2();
	
	/** 获取ShellMonitor操作实例  */
	IBinder getShellMonitor();

	/** cpu卡的读写*/
	IBinder getCPUCard();


	/** 外置非接触式卡设备实例  */
	IBinder getExternalRFCardReader();

	/*获取底座操作实例*/
	IBinder getPedestal();

	/*获取camera操作实例*/
 	IBinder getCameraManager();
}