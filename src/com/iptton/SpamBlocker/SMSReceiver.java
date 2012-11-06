package com.iptton.SpamBlocker;

import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	private static final String TAG = "SMSReceiver";
	private static final String smsuri = "android.provider.Telephony.SMS_RECEIVED";
	private static final String mmsuri = "android.provider.Telephony.WAP_PUSH_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "received msg");
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		String[] numbers = DBHelper.getNumbers(db);
		
		if (intent.getAction().equals(smsuri)) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] smg = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					smg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				
				for (SmsMessage cursmg : smg) {
					String sender = cursmg.getDisplayOriginatingAddress();
					String msg = cursmg.getMessageBody();
					
					if(-1 != java.util.Arrays.asList(numbers).indexOf(sender)){
						DBHelper.saveSpamMessage(db, sender, msg);
						abortBroadcast();
					}else{
						for(int i=0,l=numbers.length;i<l;++i){
							String number = numbers[i];
							Pattern pattern = Pattern.compile(number); 
							if(pattern.matcher(sender).matches()){
								DBHelper.saveSpamMessage(db, sender, msg);
								abortBroadcast();
								break;
							}
						}
					}
				}
			}
		}else if(intent.getAction().equals(mmsuri)){
			// 拦截所有wap推送
			// TODO 保存到数据库
			abortBroadcast();
		}
		db.close();
	}
}
