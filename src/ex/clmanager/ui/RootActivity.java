package ex.clmanager.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import ex.clmanager.R;

/**
 * 
 * @author S. Dukhnich
 * Main activity with tabs
 */
public class RootActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView( R.layout.root_layout );			
		init();		 		 
	}
	
	/**
	 * Tabbed view initialization
	 */
	private void init(){
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec;  		 	// Resusable TabSpec for each tab
		Intent intent;  				// Resusable intent for each tab
	
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, LogActivity.class);
	
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("history")
						.setIndicator("",res.getDrawable(android.R.drawable.ic_menu_call))		 
	    				.setContent(intent);
		tabHost.addTab(spec);
		 
		// Do the same for the other tabs		 
		intent = new Intent().setClass(this, LocationActivity.class);
		spec = tabHost.newTabSpec("location")
		 				.setIndicator("",res.getDrawable(android.R.drawable.ic_menu_mapmode))
	    				.setContent(intent);
		tabHost.addTab(spec);		 
		 
		intent = new Intent().setClass(this, SettingsActivity.class);
		spec = tabHost.newTabSpec("setttings")
		 				.setIndicator("",res.getDrawable(android.R.drawable.ic_menu_preferences))
	    				.setContent(intent);
		tabHost.addTab(spec);
		 
		tabHost.setCurrentTab( 0 );		
	}
			
}
