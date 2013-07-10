/* This file is part of the University of Cambridge Web Authentication
 * System Java Toolkit
 *
 * Copyright 2005 University of Cambridge
 *
 * This toolkit is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * The toolkit is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this toolkit; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * $Id: WebauthRequest.java,v 1.5 2005/07/28 08:33:56 jw35 Exp $
 *
 */

package uk.ac.cam.signups.webauth;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Represents an authentication request message.
 * 
 * @see <a href="http://raven.cam.ac.uk/project/waa2wls-protocol.txt">The
 *      Cambridge Web Authentication System: WAA->WLS communication protocol</a>
 * 
 * @version $Revision: 1.5 $ $Date: 2005/07/28 08:33:56 $
 */

public class WebauthRequest implements Serializable {

	private static final long serialVersionUID = -8570777065447980574L;
	private static final String DEFAULT_VER = "1";
	private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

	private static final String[] FIELD_NAME = { "ver", "url", "desc", "aauth",
			"iact", "msg", "params", "date", "fail" };

	private HashMap<String, String> data = new HashMap<String, String>();

	/**
	 * Default constructor. "ver" defaults to 1, "date" to the current date and
	 * time.
	 */

	public WebauthRequest() {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		data.put("ver", DEFAULT_VER);
		data.put("date", format.format(new Date()));
	}

	/**
	 * Returns the number of fields found in the request
	 * 
	 * @return the number of fields
	 */

	public int length() {
		return data.size();
	}

	/**
	 * Returns an iterator of all the field names that this request currently
	 * contains.
	 * 
	 * @return a java.util.Iterator of all the field names
	 */

	public Iterator<String> getFieldNames() {
		return data.keySet().iterator();
	}

	// Set

	/**
	 * Sets a field in this request to a string value
	 * 
	 * @param field
	 *            the name of the to set
	 * @param value
	 *            the value
	 */

	public void set(String field, String value) {
		data.put(field, value);
	}

	/**
	 * Sets a field in this request to an int value
	 * 
	 * @param field
	 *            the name of the field to set
	 * @param value
	 *            the value
	 */

	public void set(String field, int value) {
		set(field, String.valueOf(value));
	}

	/**
	 * Sets a field in this request to a long value expressing a date. The date
	 * is represented as the number of milliseconds since January 1, 1970 GMT.
	 * 
	 * @param field
	 *            the name of the field to set
	 * @param value
	 *            the date value
	 */

	public void set(String field, long value) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		set(field, format.format(new Date(value)));
	}

	/**
	 * Sets a field in this request to the values from a java.util.Collection
	 * 
	 * @param field
	 *            the name of the field to set
	 * @param value
	 *            the value
	 */

	public void set(String field, Collection<String> value) {
		StringBuffer buff = new StringBuffer();
		for (Iterator<String> it = value.iterator(); it.hasNext();) {
			buff.append(it.next().trim());
			if (it.hasNext())
				buff.append(",");
		}
		set(field, buff.toString());
	}

	// Get

	/**
	 * Returns a string containing the value of the specified field from this
	 * request
	 * 
	 * @param the
	 *            field to return
	 * 
	 * @return the value of the field as a string
	 */

	public String get(String field) {
		if (field == null || data.get(field) == null)
			return "";
		return (String) data.get(field);
	}

	/**
	 * Returns an integer value expressing the value of the specified field from
	 * this request.
	 * 
	 * @param the
	 *            field to return
	 * 
	 * @return an integer expressing the value of the request field or -1 if the
	 *         request doesn't have a field of this name or it was empty
	 * 
	 * @throws NumberFormatException
	 *             if the field can't be converted into an int
	 * 
	 */

	public int getInt(String field) throws NumberFormatException {
		if (get(field).equals(""))
			return -1;
		return Integer.parseInt(get(field));
	}

	/**
	 * Returns the value of the specified request field as a long value that
	 * represents a date. The date is returned as the number of milliseconds
	 * since January 1, 1970 GMT.
	 * 
	 * @param the
	 *            name of the field to return
	 * 
	 * @return an integer expressing the value of the specified request field as
	 *         a long value that represents a date
	 * 
	 * @throws ParseException
	 *             if the field can't be converted into an date
	 * 
	 */

	public long getDate(String field) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		return format.parse(get(field)).getTime();
	}

	/**
	 * Returns a java.util.Collection containing the comma-seperated strings
	 * from the specified field from this request
	 * 
	 * @param the
	 *            name of the field to return
	 * 
	 * @return a java.utils.Collection containing the comma-seperated strings
	 *         from the specified request field. Returns an empty collection if
	 *         the request doesn't have a field of this name or it was empty
	 */

	public Collection<String> getColl(String field) {
		HashSet<String> set = new HashSet<String>();
		for (String token : Util.split(',', get(field))) {
			set.add(token.trim());
		}
		return set;
	}

	/**
	 * Returns a human-readable representation of this entire request
	 * 
	 * @return a string representation of the request
	 */

	public String toString() {

		StringBuffer str = new StringBuffer("Webauth request: ");

		for (int i = 0; i < FIELD_NAME.length; ++i) {
			if (i != 0)
				str.append(", ");
			str.append(FIELD_NAME[i] + ": " + get(FIELD_NAME[i]));
		}

		return str.toString();

	}

	/**
	 * Returns this request in the format of a URL query string
	 * 
	 * @return a string representing the request in URL query format
	 */

	// Note: the deprecated (since 1.4) URLEncoder.encode(String) form
	// is used here for 1.3 compatibility. Should be
	// URLEncoder.encode(String,String) throws
	// UnsupportedEncodingException

	public String toQString() {

		StringBuffer str = new StringBuffer();

		for (int i = 0; i < FIELD_NAME.length; ++i) {
			if (get(FIELD_NAME[i]) == null || get(FIELD_NAME[i]).equals(""))
				continue;
			if (i != 0)
				str.append("&");
			try {
				str.append(FIELD_NAME[i] + "="
						+ URLEncoder.encode(get(FIELD_NAME[i]), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// Shouldn't happen
				throw new Error(e);
			}
		}

		return str.toString();

	}

}