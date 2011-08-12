package ex.clmanager.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import ex.clmanager.R;
import ex.clmanager.common.CLIntent;
import ex.clmanager.db.entity.Call;
import ex.clmanager.ui.RootActivity;


/**
 * Background service for detecting calls
 * @author sdukhnich
 *
 */
public class CallLogService extends Service {

	private static final String TAG = "CLM:CallLogService";

	//notification id
	private static final int CALL_LOG_NOTIFICATION_ID = 1;	
	//Call Resolver instance
	private CallResolver callResolver = null;
	//application context
	private Context mContext;
	
	/**
	 * listener for detect changing of phone state
	 * such as RINGING, OFFHOOK, IDLE
	 */
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){		
		public void onCallStateChanged(int state, String incomingNumber) {
			switch( state ){
				case TelephonyManager.CALL_STATE_RINGING :
					callResolver.registerNewCall( incomingNumber,Call.CT_INCOMING );
					Log.d( TAG, "NEW INCOMING CALL DETECTED. NUMBER IS "+incomingNumber);
//					TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK :
					callResolver.markLastCallAsAnswered();
					Log.d( TAG, "CALL ANSWERED");
					break;
				case TelephonyManager.CALL_STATE_IDLE :
					callResolver.resolveLastCall();
					sendBroadcast( new Intent( CLIntent.CLM_ACTION_DATA_CHANGED ) );
					Log.d( TAG, "CALL FINISHED");
					break;					
				default:	
					Log.d( TAG, "UKNOWN PHONE STATE");
			}			
		};
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getNotificationManager().cancel( CALL_LOG_NOTIFICATION_ID );
	}

	/**
	 * onStart lifecycle calback. Creates notification, registers phone listener
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		mContext = getApplicationContext();      // application Context		
		callResolver = CallResolver.initialize( mContext ); 

		Intent notificationIntent = new Intent(this, RootActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);	
		Notification notification = getNotification();
		notification.setLatestEventInfo( mContext, mContext.getString( R.string.notification_title),
										mContext.getString( R.string.notification_text), contentIntent);
		//notification.setLatestEventInfo(context, null, null, contentIntent);
		
		getNotificationManager().notify( CALL_LOG_NOTIFICATION_ID, notification );	
		
		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		tm.listen( mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE );		
	}
	
	/**
	 * get system NotificationManager
	 * @return - NotificationManager instance
	 */
	private NotificationManager getNotificationManager(){
		String ns = Context.NOTIFICATION_SERVICE; 
		return (NotificationManager) getSystemService(ns);		
	}
	
	/**
	 * create notification for home
	 * @return - notification
	 */
	private Notification getNotification(){				
		int icon = R.drawable.yinyang; 
		CharSequence tickerText = "Calls Log Manager";
		long when = System.currentTimeMillis();
		return new Notification(icon, tickerText, when);		
	}
	
	
}













