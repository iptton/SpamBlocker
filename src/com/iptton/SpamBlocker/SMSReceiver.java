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
	public static final String KEY_ACTION = "Action";
	public static final String UPDATE = "Update";
	public static final String ACTION_UPDATE = "com.iptton.update";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "received msg");
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		String[] numbers = helper.getNumbers();
		boolean isChanged = false;
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
						helper.saveSpamMessage(sender, msg);
						isChanged = true;
						abortBroadcast();
					}else{
						for(int i=0,l=numbers.length;i<l;++i){
							String number = numbers[i];
							Pattern pattern = Pattern.compile(number); 
							if(pattern.matcher(sender).matches()){
								helper.saveSpamMessage(sender, msg);
								isChanged = true;
								abortBroadcast();
								break;
							}
						}
					}
				}
			}
		}else if(intent.getAction().equals(mmsuri)){
			abortBroadcast();
		}
		if(isChanged){
			Intent i = new Intent();
			i.setAction(ACTION_UPDATE);
			i.putExtra(KEY_ACTION, UPDATE);
			context.sendBroadcast(i);
		}
		db.close();
	}
}
