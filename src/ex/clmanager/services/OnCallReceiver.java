package ex.clmanager.services;

import ex.clmanager.db.entity.Call;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for detect outcoming calls
 * @author sdukhnich
 *
 */
public class OnCallReceiver extends BroadcastReceiver {

	private static final String TAG = "CLM:OnCallReceiver";	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String phoneNumber = getResultData();
		CallResolver callResolver = CallResolver.getInstance();
		if( callResolver != null )
			callResolver.registerNewCall( phoneNumber, Call.CT_OUTCOMING );
		Log.d( TAG, "NEW OUTCOMING CALL REGISTERED. NUMBER IS " + phoneNumber );		
	}

}
