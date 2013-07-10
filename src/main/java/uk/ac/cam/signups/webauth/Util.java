/* This file is part of the University of Cambridge Web Authentication
 * System Java Toolkit
 *
 * Copyright 2012 University of Cambridge
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
 */
package uk.ac.cam.signups.webauth;

/** private utility methods */
public class Util {

	/*
	 * Split a string on delim. Note this has different semantics than the java
	 * standard library split
	 */
	static String[] split(char delim, String text) {

		if (text.length() == 0)
			return new String[0];

		int nFields = 0;
		for (int pos = 0; pos < text.length(); ++pos)
			if (text.charAt(pos) == delim)
				++nFields;
		String[] list = new String[nFields + 1];

		int toIndex = 0;
		int fromIndex = 0;
		int ctr = 0;

		while (fromIndex <= text.length()) {
			toIndex = text.indexOf(delim, fromIndex);
			if (toIndex == -1)
				toIndex = text.length();
			list[ctr++] = text.substring(fromIndex, toIndex);
			fromIndex = toIndex + 1;
		}

		return list;

	}
}