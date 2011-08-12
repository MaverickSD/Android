package ex.clmanager.services;

import java.util.LinkedList;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts.People;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import ex.clmanager.R;
import ex.clmanager.common.CLIntent;
import ex.clmanager.db.CallsDBProvider;
import ex.clmanager.db.entity.Call;
import ex.clmanager.ui.SettingsActivity;

/**
 * Heart of call resolving :)
 * Store information about current call and save it into database after finish
 * for now it works only for one last call but can be adapted for conference call too 
 * @author sdukhnich
 *
 */
public class CallResolver {
	
	private static final String TAG = "CLM:CallResolver";
	
	/**
	 * available call types
	 * @author sdukhnich
	 *
	 */
//	public static enum CallType{
//		CT_OUTCOMING,
//		CT_INCOMING,
//		CT_INCOMING_MISSED
//	}
	
	//stack of calls
	private LinkedList<Call> callStack = new LinkedList<Call>();
	//static instance
	private static CallResolver instance = null;
	//context
	private static Context appContext = null;
	//helper location resolver
	private LocationResolver locationResolver = null;	
	
	/**
	 * default constructor
	 */
	private CallResolver(){
		if( locationResolver == null ){
			locationResolver = new LocationResolver();		
			appContext.registerReceiver( locationResolver, new IntentFilter( CLIntent.CLM_ACTION_LOC_PROVIER_CHANGED ) );
		}				
	}
	
	//time of last call
	private long lastCallTime = 0;
	//time when last call was answered
	private long lastCallAnseredTime = 0;
	//flag of answer
	private boolean lastCallAnsered = false;
	
	/**
	 * return instance of current class
	 * @param context - context
	 * @return - instance of CallResolver
	 */
	public static CallResolver getInstance(){		
		return appContext == null ? null : instance;
	}
	
	/**
	 * set context for curent instance
	 * and initiates of LocationResolver
	 * @param context - app context
	 */
	public static CallResolver initialize( Context context ){
		if( instance == null ){
			appContext = context;
			instance = new CallResolver();
		}
		return instance; 
	}
	
	public static void deinitialize(){
		if( instance != null ){
			instance = null; 
		}
	}
	
	/**
	 * main method that will be called for registering new call
	 * @param number - phone number
	 * @param ctype - phone type
	 */
	public void registerNewCall( String number, int ctype ){

		/**
		 * if stack has unresolved call - return
		 */
		if( callStack.size() > 0 ){
			Log.e( TAG,"Call stack has unresolved call!" );
			return;
		}		
		lastCallTime = System.currentTimeMillis();
		
		//formated phone number
		String formatedNumber = PhoneNumberUtils.formatNumber( number );
		//new call
		Call call = new Call();
		//number
		call.setNumber( formatedNumber );
		//contact
		Resources rec = appContext.getResources();		
		call.setContact( ctype == Call.CT_OUTCOMING ? rec.getString( R.string.my_contact_name ) 
												: findContactByNumber( PhoneNumberUtils.formatNumber( formatedNumber ) ) );		
		//call's date
		call.setDate( lastCallTime );
		
		//location
		Point coordinates = locationResolver.resolveLocation();
		//Point coordinates = resolveLocation();
		//longitude
		call.setLongitude( coordinates.x );
		//latitude 
		call.setLatitude( coordinates.y );
		//call type
		call.setCalltype( ctype );

		//stack clearing
		callStack.clear();
		callStack.addFirst( call );
		
	}
	
	/**
	 * if call was answered - need to use this method
	 */
	public void markLastCallAsAnswered(){
		lastCallAnseredTime = System.currentTimeMillis();
		lastCallAnsered = true;
	}
	
	/**
	 * when call was finished that method 
	 * tune all data and save last call data into database
	 */
	public void resolveLastCall(){
		//check stack size
		if( callStack.size() == 0 ){
			Log.w( TAG,"Call stack is empty!" );
			return;			
		}
		//check context
		if( appContext == null ){
			Log.e( TAG,"Context not resolved!");
			return;
		}		
		
		//last call
		Call call = callStack.poll();
		//change call type if needed
		if( call.getCalltype() == Call.CT_INCOMING && !lastCallAnsered )
			call.setCalltype( Call.CT_MISSED );
		
		//duration in sec
		int duration = 0;
		if( call.getCalltype() == Call.CT_OUTCOMING ){
			duration = (int)(System.currentTimeMillis() - lastCallTime)/1000;
		}else{
			if( lastCallAnsered )
				duration = (int)(System.currentTimeMillis() - lastCallAnseredTime )/1000;
		}		
		call.setDuration( duration );		

		//save call into database
		addNewCall( call );
				
		lastCallTime = 0;
		lastCallAnseredTime = 0;
		lastCallAnsered = false;
	}
				
	/**
	 * add call to database
	 * @param call
	 */
	private void addNewCall( Call call ) {		
		ContentResolver cr = appContext.getContentResolver();
		
		ContentValues values = new ContentValues();
		values.put( CallsDBProvider.KEY_NUMBER,    call.getNumber() );
		values.put( CallsDBProvider.KEY_CONTACT,   call.getContact() );
		values.put( CallsDBProvider.KEY_DATE, 	   call.getDate() );
		values.put( CallsDBProvider.KEY_DURATION,  call.getDuration() );
		values.put( CallsDBProvider.KEY_LATITUDE,  call.getLatitude() );
		values.put( CallsDBProvider.KEY_LONGITUDE, call.getLongitude() );
		values.put( CallsDBProvider.KEY_CALLTYPE,  call.getCalltype() );		

		cr.insert( CallsDBProvider.CONTENT_URI, values );
	}	
	
	/**
	 * find contact by phone number
	 * @param number - phone number
	 * @return -  "Unknown" if contact not found and contact DISPLAY_NAME otherwise
	 */
	private String findContactByNumber( String number ){

		String contactName="Unknown";
		
		String[] contactDetails = new String[]{ People.DISPLAY_NAME };		
		
		Cursor contCursor = appContext.getContentResolver().query(
		                People.CONTENT_URI, contactDetails, People.NUMBER+ "='" + number + "'", null, null );
		int rowCount = contCursor.getCount();
	    if( rowCount > 0) {
	 	     while (contCursor.moveToNext()) {
	 	    	contactName = contCursor.getString(contCursor.getColumnIndex(People.DISPLAY_NAME));
	 	     }
        }
	    if( rowCount > 1 )
	    	Log.w( TAG, "More then 1 contact was found!" );
	    contCursor.close();
	    
		return contactName; 
	}
		
	/**
	 * Resolver for getting current coordinates
	 * @author sdukhnich
	 *
	 */
	private class LocationResolver extends BroadcastReceiver implements LocationListener{
		
		private final static int DATA_TOPICALITY_TIME = 120000; //after 2 minutes location is not valid 
		private String providerName = null;
		private LocationManager locationManager = null;
		private Location lastKnownLocation = null;
		private long lastMeasuredTime = 0; //workaround for AVD. In real device we must to use time from Location
		
		{			
			providerName = PreferenceManager.getDefaultSharedPreferences( appContext )
								   .getString( SettingsActivity.LOCATION_PROVIDER, null );	
			
			if( providerName != null ){
				locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates( providerName, 0, 0, this );
			}
		}
		
		/**
		 * main method for location resolving
		 * if lastMeasuredTime > 2 min then location will be rejected
		 * @return - point of latitude and longitude
		 */
		public Point resolveLocation(){			
				Point coordinates = new Point(0,0);
				if( TextUtils.isEmpty( providerName ) || locationManager == null  )
					return coordinates;
				
				try{
					//LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
					//location = locationManager.getLastKnownLocation( providerName );				
					
					if( lastKnownLocation != null && (System.currentTimeMillis() - lastMeasuredTime ) <= DATA_TOPICALITY_TIME ){
						coordinates.x = (int)(lastKnownLocation.getLongitude()* 1E6);
						coordinates.y = (int)(lastKnownLocation.getLatitude()* 1E6);
						return coordinates;
					}						
				}catch( Exception ex){
					Log.e( TAG, ex.getMessage() );
				}	
				return coordinates;
		}
		
		/**
		 * set provider name and reinit location manager
		 * @param newName - new provider name
		 */
		public void setProviderName( String newName ){			
			synchronized( providerName ){
				providerName = newName;
				if( providerName != null ){
					locationManager.removeUpdates( this );
					locationManager.requestLocationUpdates( providerName, 0, 0, this );
				}
			}			
		}		
		
		/**
		 * reciver for action when type of provider change
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			String newProvider = intent.getExtras().getString( SettingsActivity.LOCATION_PROVIDER );
			if( !TextUtils.isEmpty( newProvider ) ){ 
				setProviderName( newProvider );
				Log.d( TAG,"Provider changed to ["+newProvider+"]");
			}else{
				Log.d( TAG,"Can't change provider" );				
			}
		}
		
		/**
		 * callback for location change called from LocationManager
		 */
		@Override
		public synchronized void onLocationChanged(Location location) {		
			lastKnownLocation = location;
			lastMeasuredTime = System.currentTimeMillis();
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}	
				
	}
}
