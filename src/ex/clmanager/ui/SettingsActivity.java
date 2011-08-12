package ex.clmanager.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import ex.clmanager.R;
import ex.clmanager.services.CallLogService;

/**
 * Activity for manage app settings such as 
 * 	- LocationProvider
 *  - Service control 
 * @author sdukhnich
 *
 */
public class SettingsActivity extends PreferenceActivity{

	public final static String TAG = "CLM:SettingsActivity";
	
	//Location provider param name	
	public static final String LOCATION_PROVIDER = "LocationProvider";

	//service state param name
	public static final String SERVICE_STATE = "ServiceState";
	
	private SharedPreferences sp = null;
	
	private OnSharedPreferenceChangeListener preferenceListener = new OnSharedPreferenceChangeListener(){

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if( key.equals( "serviceState" ) ){
				if( sharedPreferences.getBoolean( SERVICE_STATE, false ) ){
			        Log.v( TAG, "onClick: Starting service.");
			        startService( new Intent(SettingsActivity.this, CallLogService.class));
				}else{
			        Log.v( TAG, "onClick: Stopping service.");
			        stopService( new Intent(SettingsActivity.this, CallLogService.class));					
				}
			}			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		sp = PreferenceManager.getDefaultSharedPreferences( this );
		sp.registerOnSharedPreferenceChangeListener( preferenceListener );
		Log.w( TAG, this.getPackageCodePath() );
//		initUIWithPrefs();
	}
			
	@Override
	protected void onDestroy() {
		sp.unregisterOnSharedPreferenceChangeListener( preferenceListener );		
		super.onDestroy();
	}
	
	private void initUIWithPrefs(){
	}
}
