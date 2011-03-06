package org.tomac.tools.converter;

public class QuickBase implements Comparable {
	public String position;

	@Override
	public int compareTo(Object o) {
		QuickBase q = (QuickBase)o;
		if (q.getInt()>getInt()) return -1;
		if (q.getInt()<getInt()) return 1;
		else return 0;
	}
	
	int getInt() {
		String intPosition = position.split("[.]")[0];
		return Integer.valueOf(intPosition);
	}
}
