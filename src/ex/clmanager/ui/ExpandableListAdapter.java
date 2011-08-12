package ex.clmanager.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ex.clmanager.R;
import ex.clmanager.common.CLIntent;
import ex.clmanager.db.entity.Call;
import ex.clmanager.util.DateUtil;

/**
 * Adapter for EpandableList of calls
 * @author sdukhnich
 *
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter{
	
	//app context
	private Context context;
	//list of top groups
    private ArrayList<String> groups;
    //list of children for every group
    private ArrayList<ArrayList<Call>> children;

    private ELClickListener clickListener = new ELClickListener(); 
    
    /**
     * constructor
     * @param context - app context
     * @param groups - list of groups
     * @param children - list of children
     */
    public ExpandableListAdapter(Context context, ArrayList<String> groups,
            ArrayList<ArrayList<Call>> children) {
        this.context = context;
        this.groups = groups;
        this.children = children;
    }	
	
    /**
     * add new call info into children and(or) groups lists 
     * @param call - new call
     */
    public void addItem(Call call) {
    	
    	String date = DateUtil.getFormattedDate( call.getDate() );     	
        if (!groups.contains( date ) ) {
            groups.add( date );
        }
        int index = groups.indexOf( date );
        if (children.size() < index + 1) {
            children.add(new ArrayList<Call>());
        }
        children.get(index).add( call );
    }
        
	@Override
	public Object getChild(int gropIndex, int childIndex ) {
		return children.get(gropIndex).get(childIndex);
	}

	@Override
	public long getChildId(int gropIndex, int childIndex) {
		return childIndex;
	}

	/**
	 * fill child row ui's components with data
	 */
	@Override
	public View getChildView(int groupIndex, int childIndex, boolean isLastChild, View convertView,	ViewGroup parent) {
	       Call call = (Call)getChild( groupIndex, childIndex );
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater)context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.call_child_layout, null);
	        }
	        
	        setCallIcon( call, (ImageView) convertView.findViewById( R.id.callTypeImg ) );

	        //phone number
	        TextView tvNumber = (TextView) convertView.findViewById(R.id.numberView);
	        tvNumber.setText( call.getNumber() );

	        //contact
	        TextView tvContact = (TextView) convertView.findViewById(R.id.contactView);
	        String contact = call.getContact();
	        tvContact.setText( contact );	        

	        //call time
	        TextView tvTime = (TextView) convertView.findViewById(R.id.timeView);
	        tvTime.setText( DateUtil.getFormattedTime( call.getDate() ) );
	        
	        //call duration
	        setCallDuration( call, (TextView) convertView.findViewById(R.id.durationView) );
	        	        	        
	        //location image ( shows posibility for view call location on map )
	        setLocationIcon( call, (ImageView) convertView.findViewById( R.id.mapImg ) );
	        
	        return convertView;
	}

	private void setCallIcon( Call call, final ImageView iView ){
        switch( call.getCalltype() ){
    	case Call.CT_OUTCOMING: //outcoming call
    		iView.setImageResource( android.R.drawable.sym_call_outgoing );
    		break;
    	case Call.CT_INCOMING: //incoming call
    		iView.setImageResource( android.R.drawable.sym_call_incoming );
    		break;
    	case Call.CT_MISSED: //missed call
    		iView.setImageResource( android.R.drawable.sym_call_missed );
    		break;	        		
        }		
	}
	
	private void setCallDuration(  Call call, final TextView tvDuration ){
        int duration = call.getDuration(); 
        int hrs = duration / 3600;
        int min = ( duration % 3600 ) / 60;
        int sec = ( duration % 3600 ) % 60;
        String strHrs = hrs < 10 ? "0"+hrs : ""+hrs;
        String strMin = ":"+( min < 10 ? "0"+min : ""+min );
        String strSec = ":"+( sec < 10 ? "0"+sec : ""+sec );	        
        tvDuration.setText( strHrs+strMin+strSec );		        
	}
	
	private void setLocationIcon( Call call, final ImageView mapImage ){
        if( call.getLatitude() != 0 && call.getLongitude() != 0 ) {
        	mapImage.setImageResource( R.drawable.earth );
        	mapImage.setTag( call.getDate() );
        	mapImage.setOnClickListener( clickListener );
        } else {
        	mapImage.setImageResource( R.drawable.earth_delete );
        }
	}
	
	@Override
	public int getChildrenCount(int groupIndex ) {
		return children.get(groupIndex).size();
	}

	/**
	 * count count for every call type and return it as array
	 * @param groupIndex - index of group
	 * @return - array of call types count
	 */
	private int[] getChildrenCountByType( int groupIndex ){
		int[] result = new int[3];
		List<Call> calls = children.get(groupIndex);
		for( Call c : calls ){
			result[ c.getCalltype() ]++; 
		}
		return result;
	}
	
	@Override
	public Object getGroup(int groupIndex) {
		return groups.get( groupIndex );
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupIndex) {
		return groupIndex;
	}

	/**
	 * fill grop row with data
	 * @param groupIndex
	 * @return
	 */	
	@Override
	public View getGroupView(int groupIndex, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String group = (String) getGroup(groupIndex);
		if (convertView == null) {
		    LayoutInflater infalInflater = (LayoutInflater) context
		            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    convertView = infalInflater.inflate(R.layout.date_group_layout, null);
		}
		TextView tvDate = (TextView) convertView.findViewById(R.id.dateGroup);
		int callsCount = getChildrenCount(groupIndex);
		tvDate.setText(group+" ( "+callsCount+" "+( callsCount > 1 ? context.getString( R.string.call_title): 
																 context.getString( R.string.calls_title) )+" )");
		//tvDate.setText( group );
		
		int[] info = getChildrenCountByType( groupIndex );
		TextView tvDateCallInfo = (TextView) convertView.findViewById(R.id.dateGroupInfo);	
		tvDateCallInfo.setText( context.getString( R.string.outcoming_title)+" - "+info[0]+
						   " ,"+context.getString( R.string.incoming_title )+" - "+info[1]+
						   " ,"+context.getString( R.string.missed_title   )+" - "+info[2] );
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private class ELClickListener implements OnClickListener{
		/**
		 * callback for show select call on map
		 */
		@Override
		public void onClick(View v) {
			
			TabActivity rootActivity = (TabActivity)(((Activity)context).getParent());
			rootActivity.getTabHost().setCurrentTab( 1 );		
			
			context.sendBroadcast( CLIntent.createShowCallMapAction(  (Long)v.getTag()) );
		}
	}
	
	/**
	 * clear all lists
	 */
	public void clear(){
		groups.clear();
		children.clear();
	}
}
