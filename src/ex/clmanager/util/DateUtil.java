package ex.clmanager.util;

import java.util.Date;
import org.apache.http.impl.cookie.DateUtils;


/**
 * Class-wrapper for converting date and time
 * @author sdukhnich
 *
 */
public final class DateUtil {
	
	private final static String DATE_FORMAT_STR = "dd.MM.yyyy";
	private final static String TIME_FORMAT_STR = "hh:mm:ss";
	
	private DateUtil(){}
	
	public static String getFormattedDate( long date ){
		return DateUtils.formatDate( new Date(date), DATE_FORMAT_STR );
	}
		
	public static String getFormattedTime( long date ){
		return DateUtils.formatDate( new Date(date), TIME_FORMAT_STR );
	}		
	
}
