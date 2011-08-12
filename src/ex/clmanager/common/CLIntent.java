package ex.clmanager.common;

import android.content.Intent;

public class CLIntent extends Intent {
	
	//action for location provider change
	public final static String CLM_ACTION_LOC_PROVIER_CHANGED = "clmLocProviderChangedAction";
	//action for log change
 	public final static String CLM_ACTION_DATA_CHANGED = "clmDataChangedAction";		
 	//show call on the map action
 	public final static String CLM_ACTION_SHOW_CALL_ON_MAP = "clmShowCallOnMap";
 	//Date tag for ACTION_SHOW_CALL_ON_MAP
 	private final static String CLM_EXTRA_CALL_DATE_TAG = "DateTag"; 	

 	
 	public CLIntent( String action ){
 		super( action );
 	}
 	 	
 	public static Intent createShowCallMapAction( long selectedCallDate ){
 		return new CLIntent( CLM_ACTION_SHOW_CALL_ON_MAP )
 					.putExtra(CLM_EXTRA_CALL_DATE_TAG, CLM_EXTRA_CALL_DATE_TAG );
 	}
 	
 	public static long getLastSelectedCallDateFromIntent( Intent intent ){
 		return intent.getLongExtra( CLM_EXTRA_CALL_DATE_TAG, 0 );
 		
 	}
 	
}
