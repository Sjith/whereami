/**
 * This class is based on the code here
 * http://code.google.com/p/android-phonefinder/
 */

package org.addhen.whereami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	
	private SmsMessage[] buildSmsMessage(Intent intent)
	{
		SmsMessage retMsgs[] = null;
		Bundle bdl = intent.getExtras();
		try{
			Object pdus[] = (Object [])bdl.get("pdus");
			retMsgs = new SmsMessage[pdus.length];
			for(int n=0; n < pdus.length; n++)
			{
				byte[] byteData = (byte[])pdus[n];
				retMsgs[n] = SmsMessage.createFromPdu(byteData);
			}	
			
		}catch(Exception e)
		{
			Log.e("GetMessages", "fail", e);
		}
		return retMsgs;
	}
	
	public void onReceive(Context context, Intent intent) 
	{
		
		if(!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		{
			return;
		}
		SmsMessage msg[] = buildSmsMessage(intent);
		
		for(int i=0; i < msg.length; i++)
		{
			String message = msg[i].getDisplayMessageBody();
			if(message != null && message.length() > 0)
			{
				Log.i("MessageListener:",  message);
				
				SharedPreferences vCode = context.getSharedPreferences(Whereami.VERIFICATION_CODE, 0);
				String vCodeStr = vCode.getString(Whereami.VERIFICATION_CODE, null);
					
				if (message.trim().toLowerCase().contains(vCodeStr.toLowerCase())) // Verify code included is ok!
				{
					Intent mIntent = new Intent(context, LocationService.class);
					mIntent.putExtra("dest", msg[i].getOriginatingAddress());
					context.startService(mIntent);
					Toast.makeText(context, "LocationService Started!", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}

}
