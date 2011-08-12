package ex.clmanager.db.entity;


/**
 * 
 * @author S. Dukhnich
 * Entity class for storing call info
 */
public class Call {
	
	/**
	 * outcomming call type
	 */
	public final static int CT_OUTCOMING	= 0;
	/**
	 * incoming call type
	 */
	public final static int CT_INCOMING 	= 1;
	/**
	 * missed call type
	 */
	public final static int CT_MISSED 	 	= 2;	
	
	//fields
	//private long id;
	private String number;
	private String contact;
	private long date;
	private int duration;
	private int longitude;
	private int latitude;
	private int calltype;	
	
	/**
	 * default constructor
	 */
	public Call(){}
	
	/**
	 * alternative constructor
	 * @param id 
	 * @param number
	 * @param contact
	 * @param date
	 * @param durability
	 * @param longitude
	 * @param latitude
	 * @param calltype
	 */
	public Call(/*long id,*/ String number, String contact, long date, 
			int durability, int longitude, int latitude, int calltype) {
		super();
		//this.id = id;
		this.number = number;
		this.contact = contact;
		this.date = date;
		this.duration = durability;
		this.longitude = longitude;
		this.latitude = latitude;
		this.calltype = calltype;
	}
	
//	public long getId() {
//		return id;
//	}
//	public void setId(long id) {
//		this.id = id;
//	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getLongitude() {
		return longitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public int getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	public int getCalltype() {
		return calltype;
	}
	public void setCalltype(int calltype) {
		this.calltype = calltype;
	}	
}
