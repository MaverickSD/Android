package ex.clmanager.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CallsDBProvider extends ContentProvider {

	public final static String AUTHORITY = "ex.clmanager.db.CallsDBProvider";
	public final static Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/calls");
	
	private final static String TAG = "CLM:CallsDBProvider";

	private final static String DATABASE_NAME = "calls.db";
	private final static int DATABASE_VERSION = 1;
    private final static String CALLS_TABLE_NAME = "calls";

    // Column Names
    public final static String KEY_ID 		 = "_id";	
    public final static String KEY_NUMBER 	 = "number";
    public final static String KEY_CONTACT 	 = "contact";
    public final static String KEY_DATE 	 = "date";
    public final static String KEY_DURATION  = "duration";
    public final static String KEY_LONGITUDE = "longitude";
    public final static String KEY_LATITUDE  = "latitude";
    public final static String KEY_CALLTYPE  = "calltype";
	
	// Column indexes
	public final static int NUMBER_COLUMN 	 = 1;
	public final static int CONTACT_COLUMN 	 = 2;
	public final static int DATE_COLUMN 	 = 3;
	public final static int DURATION_COLUMN  = 4;
	public final static int LONGITUDE_COLUMN = 5;
	public final static int LATITUDE_COLUMN  = 6;
	public final static int CALLTYPE_COLUMN  = 7;	
	
	private final static String DEFAULT_SORT_ORDER = KEY_DATE+" DESC";
		   
	// Create the constants used to differentiate between the different URI requests.
	private final static int CALLS 	 = 1;
	private final static int CALL_ID = 2;
	
	private DatabaseHelper mOpenHelper;
	private final static UriMatcher uriMatcher;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI( AUTHORITY, "calls", CALLS );
		uriMatcher.addURI( AUTHORITY, "calls/#", CALL_ID );
	}
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+CALLS_TABLE_NAME+" (" +
					KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
					KEY_NUMBER   +" TEXT," +
					KEY_CONTACT  +" TEXT," +
					KEY_DATE	 +" INTEGER," +
					KEY_DURATION +" INTEGER," +
					KEY_LONGITUDE+" INTEGER," +
					KEY_LATITUDE +" INTEGER," +
					KEY_CALLTYPE +" INTEGER);");                        
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+CALLS_TABLE_NAME);
            onCreate(db);
        }
    } //DatabaseHelper

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Query method
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables( CALLS_TABLE_NAME );
        
        switch ( uriMatcher.match(uri) ) {
		    case CALL_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
		    	break;
        }

        // If no sort order is specified use the default
        String orderBy = TextUtils.isEmpty(sortOrder) ? DEFAULT_SORT_ORDER : sortOrder;

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * getType method
     */
    @Override
    public String getType(Uri uri) {
    	switch( uriMatcher.match(uri) ) {
    		case CALLS: return "vnd.android.cursor.dir/vnd.ex.clmanager.call";
    		case CALL_ID: return "vnd.android.cursor.item/vnd.ex.clmanager.call";
    		default: throw new IllegalArgumentException("Unsupported URI: " + uri);
    	}    	
    }

    /**
     * Insert method
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }	    	
    	
    	// Insert the new row, will return the row number if successful.	    	
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert( CALLS_TABLE_NAME, KEY_NUMBER, values );
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId( CONTENT_URI, rowId );
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new IllegalStateException( "Failed to insert row into " + uri);
    }

    /**
     * Delete method
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        
    	int count;
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch ( uriMatcher.match(uri) ) {
        case CALLS:
            count = db.delete( CALLS_TABLE_NAME, where, whereArgs );
            break;

        case CALL_ID:
            String callId = uri.getPathSegments().get(1);
            count = db.delete( CALLS_TABLE_NAME, KEY_ID + "=" + callId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

	    @Override
	    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

	    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	        int count;
	        switch( uriMatcher.match(uri) ) {
	        case CALLS:
	            count = db.update( CALLS_TABLE_NAME, values, where, whereArgs );
	            break;

	        case CALL_ID:
	            String noteId = uri.getPathSegments().get(1);
	            count = db.update( CALLS_TABLE_NAME, values, KEY_ID + "=" + noteId
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;

	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
	    }

}
