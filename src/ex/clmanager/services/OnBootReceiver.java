package ex.clmanager.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews.ActionException;
import ex.clmanager.ui.SettingsActivity;

/**
 * Receiver class that activate after 
 * BOOT_COMPLETE event and starts CollLogService 
 * @author sdukhnich
 *
 */
public class OnBootReceiver extends BroadcastReceiver {

	  private static final String TAG = "CLM:OnBootReceiver";
	
	  @Override
	  public void onReceive(Context context, Intent intent) {
		  
	    if( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ) {	    	
	    	
	    	//trying to get saved server state
	    	SharedPreferences settings = null;
	    	try{
		    	//settings = context.getSharedPreferences( SettingsActivity.PREF_STORE_NAME, 0 );
	    		settings = PreferenceManager.getDefaultSharedPreferences( context );
		    	if( settings.getBoolean( SettingsActivity.SERVICE_STATE, false ) ){
		    		Log.w( TAG, "Service disabled!" );
		    		return;
		    	}else{
		    		Log.w( TAG, "Service enabled!" );
		    	}
		    	Log.w( TAG, context.getPackageCodePath() );
	    	}catch( Exception ex ){
		    	Log.w( TAG, "Saved preferences was not found!" );
		    }
	    	runService( context ); //service will be started by default
	    }
	  }
	  
	  /**
	   * Run CollLogService
	   * @param context - app context
	   */
	  private void runService( Context context ){
	    Intent serviceLauncher = new Intent(context, CallLogService.class);
	    context.startService(serviceLauncher);
	    Log.v( TAG, "CallLogService is starting...");		  
	  }
}
