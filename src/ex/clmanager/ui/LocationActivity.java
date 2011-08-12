package ex.clmanager.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import ex.clmanager.R;
import ex.clmanager.common.CLIntent;
import ex.clmanager.db.CallsDBProvider;
import ex.clmanager.db.entity.Call;
import ex.clmanager.util.DateUtil;

/**
 * Activity for draw calls location on google map
 * @author sdukhnich
 *
 */
public class LocationActivity extends MapActivity{

	/**
	 * ID's of options menu items
	 */
	private final static int MENU_ALL   = 1;
	private final static int MENU_LAST  = 2;
	private final static int MENU_MODE  = 3;
	
	//last selected mode
	private int lastMode = MENU_ALL; 

	//map view
	private MapView mapView;
	
    //overlays for every type of calls
    private PointOverlay incomingOverlay; 
    private PointOverlay outcomingOverlay;
    private PointOverlay missedOverlay;        
	 
    //last viewed call location
    private long lastTimePoint = 0; 
    
    /**
     * overriden onCreate
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.map_layout);
       
        initMapView();
        //registered receiver for data changing event
        registerReceiver( new DataChangedReciver(), new IntentFilter( CLIntent.CLM_ACTION_DATA_CHANGED ) );
        //registered receiver for show call location event
        registerReceiver( new CallViewReciver(), new IntentFilter( CLIntent.CLM_ACTION_SHOW_CALL_ON_MAP ) );
        
    }
	
	/**
	 * initializing map
	 */
	private void initMapView(){
        
		mapView = (MapView) findViewById(R.id.mapview);    
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite( true );
        mapView.setLongClickable( true );

        registerForContextMenu( mapView );	        
        
        MapController mc = mapView.getController(); 
        mc.setZoom(1);
                
        initOverlays();
        
        runUpdateTask();        		
	}
	
	/**
	 * update map's points
	 */
	private void runUpdateTask(){
		new Updater().execute();
	}
	
	/**
	 * Task for updating points on the map
	 * @author sdukhnich
	 *
	 */
	private class Updater extends AsyncTask<Void, Void, Void>{

		private Toast informer = Toast.makeText( LocationActivity.this,getString( R.string.initializing_title ), 100 );
		
		@Override
		protected Void doInBackground(Void... params) {			
			publishProgress();
	        updateMap();	        	        	        
	        return null;
		}		
		
		@Override
		protected void onProgressUpdate(Void... values) {
			if( mapView.isShown() )
				informer.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			invalidateMap();
			informer.cancel();
		}
	}
	
	/**
	 * creating options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
    	menu.add(0,MENU_ALL,0,getString( R.string.menu_all_calls ) ).setIcon( android.R.drawable.ic_menu_search );
    	menu.add(0,MENU_LAST,1,getString( R.string.menu_last_call) ).setIcon( android.R.drawable.ic_menu_mylocation);
    	menu.add(0,MENU_MODE,1,getString( R.string.menu_map_mode) ).setIcon( android.R.drawable.ic_menu_mapmode );    	
		return true;
	}
	
	/**
	 * optionsmenu click collback
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {				
		resolveOptionMenuItemPressed( item.getItemId() );
		return true;		
	}
	
	/**
	 * OptionMenu press resolving method
	 * @param intemId - item menu id
	 */
	private void resolveOptionMenuItemPressed( int intemId ){
		switch( intemId ){
			case MENU_ALL:
					lastMode = MENU_ALL;
					runUpdateTask();
					break;
			case MENU_LAST:					
					if( lastTimePoint == 0 )
						Toast.makeText( this,"Last call not specified!", 3 ).show();
					else{
						lastMode = MENU_LAST;
						runUpdateTask();
					}
					break;
			case MENU_MODE:
					this.openContextMenu( mapView );
					break;		
		}		
	}
	
	/**
	 * creating context menu for changing map viewing type
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle( R.string.cm_header );
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.map_context_menu, menu );		
	}
	
	/**
	 * callback for selecting current map viewing type
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		resolveContextMenuItemPressed( item.getItemId() );
		return true;
	}
	
	/**
	 * ContextMenu press resolving method
	 * @param intemId - item menu id
	 */
	private void resolveContextMenuItemPressed( int itemId ){		
		switch( itemId ){
			case R.id.satellite:
				mapView.setSatellite( true );
				break;
			case R.id.map:
				mapView.setSatellite( false );
				break;
		}
		
		runUpdateTask();		
	}
	
	/**
	 * Creating of drawables and overlays
	 */
	private void initOverlays(){

		//creating icons
	    Drawable incoming = this.getResources().getDrawable( R.drawable.pin_blue);
	    Drawable outcoming = this.getResources().getDrawable( R.drawable.pin_green);
	    Drawable missed = this.getResources().getDrawable( R.drawable.pin_red );        		
		
	    //creating overlays
        incomingOverlay = new PointOverlay( incoming, this );
        outcomingOverlay = new PointOverlay( outcoming, this );
        missedOverlay = new PointOverlay( missed,this );
		
	}
		
	@Override
	protected boolean isRouteDisplayed() {		
		return false;
	}
	
	/**
	 * Overlay class for managing of point's list 
	 * which will be drawed on map
	 * @author sdukhnich
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class PointOverlay extends ItemizedOverlay {
		
		//list of point
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		//app context
		private Context mContext; 
		
		/**
		 * default constructor
		 * @param defaultMarker - default icon for point
		 * @param mContext - app context
		 */
		public PointOverlay(Drawable defaultMarker, Context mContext) {
			super( boundCenterBottom(defaultMarker) );
			this.mContext = mContext;
		}
			
		
		/**
		 * onTap listener
		 * show dialog with call info
		 */
		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);  
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());  
			dialog.setMessage(item.getSnippet());  
			dialog.show();
			return true;	
		}

		/**
		 * Add overlay item
		 * @param overlay
		 */
		public void addOverlay( OverlayItem overlay ){
		    mOverlays.add(overlay);    
		    populate();		
		}
		
		@Override
		protected OverlayItem createItem(int i) {		
			return mOverlays.get(i);
		}

		@Override
		public int size() {		
			return mOverlays.size();
		}	
		
		public void clear(){
			mOverlays.clear();
		}
	}
		
	/**
	 * Update existed map when data changed
	 */
	private void updateMap(){
		
		mapView.getOverlays().clear();		
        List<Overlay> mapOverlays = mapView.getOverlays();
        
        int drawableCalls = 0; 
        outcomingOverlay.clear();
        incomingOverlay.clear();
        missedOverlay.clear();
        
        GeoPoint point = null;
        
        List<Call> calls = getCallListFromDB();//CallsDBHelper.getInstance().getCallsList();
        for( Call c : calls ){
        	if( !(c.getLatitude() != 0 && c.getLongitude() != 0) 
        			|| ( lastMode == MENU_LAST && lastTimePoint != c.getDate() ) ) 
        		continue;
            point = new GeoPoint( c.getLatitude(),c.getLongitude() );            
            OverlayItem overlayitem = new OverlayItem(point, c.getContact(), getString( R.string.number_label)+c.getNumber()
            				+"\n"+getString( R.string.date_label )+DateUtil.getFormattedDate( c.getDate() ) 
            				+"\n"+getString( R.string.time_label)+DateUtil.getFormattedTime( c.getDate() ) );
            switch( c.getCalltype() ){
            	case Call.CT_OUTCOMING: outcomingOverlay.addOverlay( overlayitem );
            			break;
            	case Call.CT_INCOMING: incomingOverlay.addOverlay( overlayitem );
            			break;
            	case Call.CT_MISSED: missedOverlay.addOverlay( overlayitem );
            			break;            			
            }        	
            drawableCalls++;
        }
                
        if( incomingOverlay.size() > 0 )        
        	mapOverlays.add( incomingOverlay );
        if( outcomingOverlay.size() > 0 )        
        	mapOverlays.add( outcomingOverlay );
        if( missedOverlay.size() > 0 )
        	mapOverlays.add( missedOverlay );

//        mapView.invalidate();

        if( lastMode == MENU_LAST && point != null ){
        	MapController mc = mapView.getController(); 
        	mc.animateTo( point );
        }
	}
	
	private List<Call> getCallListFromDB(){
		
		List<Call> callList = new ArrayList<Call>();
		ContentResolver cr = getContentResolver();
		
		// Return all the saved calls
		Cursor c = cr.query( CallsDBProvider.CONTENT_URI, null, null, null, null);
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

				callList.add( new Call( number, contact, date, duration, longitude, latitude, calltype )  );
			} while(c.moveToNext());
		}
		return callList;
	}
	
	private void invalidateMap(){
		mapView.invalidate();		
	}
	
	/**
	 * Reciver for event ACTION_DATA_CHANGED
	 * @author sdukhnich
	 *
	 */
	private class DataChangedReciver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			runUpdateTask();
		}		
	}

	/**
	 * Reciver for event ACTION_SHOW_CALL_ON_MAP
	 * @author sdukhnich
	 *
	 */
	private class CallViewReciver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			lastTimePoint = CLIntent.getLastSelectedCallDateFromIntent( intent );
			lastMode = MENU_LAST;
			runUpdateTask();			
		}		
	}	
	
}
