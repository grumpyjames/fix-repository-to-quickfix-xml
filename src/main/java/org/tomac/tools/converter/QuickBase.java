/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

public class QuickBase implements Comparable<QuickBase> {
	public String position;

	//@Override
	public int compareTo(QuickBase o) {
		final QuickBase q = (QuickBase) o;
		if (q.getInt() > getInt()) {
			return -1;
		}
		if (q.getInt() < getInt()) {
			return 1;
		} else {
			return 0;
		}
	}

	int getInt() {
		final String intPosition = position.split("[.]")[0];
		return Integer.valueOf(intPosition);
	}
}
