package ex.clmanager.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;
import ex.clmanager.R;
import ex.clmanager.common.CLIntent;
import ex.clmanager.db.CallsDBProvider;
import ex.clmanager.db.entity.Call;

/**
 * 
 * @author S. Dukhnich
 * Main activity for show list of calls 
 */
public class LogActivity extends Activity {
	
	// options menu items id
	private final static int MENU_CLEAR  = 1;
	
	//UI list instance
	private ExpandableListView listView = null;
	
	//adapter for list
	private ExpandableListAdapter adapter;
	
	//Content resolver
	private ContentResolver cResolver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_layout);
        cResolver = getContentResolver();
        init();
    }
    
    /**
     * initializing method
     */
    private void init(){    	
//        registerReceiver( new DataChangedReciver(), new IntentFilter( CLIntent.CLM_ACTION_DATA_CHANGED ) );
        //initiating of list
		listView = (ExpandableListView) findViewById(R.id.expListView);
        adapter = new ExpandableListAdapter(this, new ArrayList<String>(),new ArrayList<ArrayList<Call>>() );
        listView.setAdapter(adapter);            	
        
        runUpdateTask();
    }
    
    /*
     * method for runing initializied task
     */
    private void runUpdateTask(){
    	new Updater().execute();
    }
    
    /**
     * Task for updating list
     * @author sdukhnich
     *
     */
	private class Updater extends AsyncTask<Void, Void, Void>{

		private Toast informer = Toast.makeText( LogActivity.this,getString( R.string.initializing_title ), 100 );
		
		@Override
		protected Void doInBackground(Void... params) {			
			publishProgress();
			update();	        	        	        
	        return null;
		}		
		
		@Override
		protected void onProgressUpdate(Void... values) {
			if( listView.isShown() )
				informer.show();
		}		
		
		@Override
		protected void onPostExecute(Void result) {
	        adapter.notifyDataSetChanged();
	        informer.cancel();
		}
	}
    
    @Override
	protected void onStart() {
    	super.onStart();    	
	}

    /**
     * creating option menu
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,MENU_CLEAR,1,"Clear All" ).setIcon( android.R.drawable.ic_menu_close_clear_cancel );
		return true; 
	}

	/**
	 * callback for options menu selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ){
			case MENU_CLEAR:
				pressClearMenuItem();
				break;				
		}
		return true;
	}
	
	/**
	 * clearing list of calls
	 */
	private void pressClearMenuItem(){
		clearCalls();
//		runUpdateTask();
//		sendBroadcast( new Intent( CLIntent.CLM_ACTION_DATA_CHANGED ) );		
	}
	
	/**
	 * update list of calls
	 */
	private void update(){		
        fillAdapter();
	}

	/**
	 * initializing adapter with data from database 
	 * @param adapter - list adapter
	 */
	private void fillAdapter( /*final ExpandableListAdapter adapter*/ ){
		
		adapter.clear();
		
		// Return all the saved calls
		Cursor c = cResolver.query( CallsDBProvider.CONTENT_URI, null, null, null, null);
		if ( c.moveToFirst() ){
			do {
				// Extract the call details.
				String number = c.getString( CallsDBProvider.NUMBER_COLUMN );
				String contact = c.getString( CallsDBProvider.CONTACT_COLUMN );
				long date = c.getLong( CallsDBProvider.DATE_COLUMN );
				int duration = c.getInt( CallsDBProvider.DURATION_COLUMN );
				int latitude = c.getInt( CallsDBProvider.LATITUDE_COLUMN );
				int longitude = c.getInt( CallsDBProvider.LONGITUDE_COLUMN );
				int calltype = c.getInt( CallsDBProvider.CALLTYPE_COLUMN );				

				Call call = new Call( number, contact, date, duration, longitude, latitude, calltype );
				adapter.addItem( call );
			} while(c.moveToNext());
		}				
    }
	
	private void clearCalls(){
		adapter.clear();
		cResolver.delete( CallsDBProvider.CONTENT_URI, null, null );
	}
    
	private class CLMContentObserver extends ContentObserver{
		
		public CLMContentObserver(Handler handler) {
			super(handler); 
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			LogActivity.this.update();
		}		
	}
//	/**
//	 * receiver for data change event
//	 * @author sdukhnich
//	 *
//	 */
//	private class DataChangedReciver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			runUpdateTask();
//		}		
//	}	
}

