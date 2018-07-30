package com.rebocomm.rebocomm.helper;

import android.content.Context;
import android.os.Handler;

public class PrinterClassFactory {
    public static PrinterClass create(int type, Context _context, Handler _mhandler, Handler _handler){
        if(type==0){
               return new BtService(_context,_mhandler, _handler); 
        }
		return null;
  }

}