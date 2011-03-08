/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

import java.util.ArrayList;

/*
 <field number="4" name="AdvSide" type="CHAR">
 <value enum="B" description="BUY"/>
 <value enum="S" description="SELL"/>
 <value enum="T" description="TRADE"/>
 <value enum="X" description="CROSS"/>
 </field>
 */
public class QuickFixField extends QuickBase {

	public class QuickFixValue {
		public String fixEnum;
		public String description;

		public QuickFixValue(String fixEnum, String description) {
			this.fixEnum = new String(fixEnum);
			this.description = new String(description);
		}
	}

	public String reqd;

	public String number;
	public String name;
	public String type;
	public ArrayList<QuickFixValue> quickFixValues = new ArrayList<QuickFixValue>();
	public boolean belongsToMessage = false;

	public QuickFixField() {
	}

	public QuickFixField(QuickFixField q, String req, String position) {
		number = q.number;
		name = q.name;
		type = q.type;
		quickFixValues = q.quickFixValues;
		belongsToMessage = q.belongsToMessage;
		reqd = new String(req != null ? req : "0");
		this.position = position;
	}
}
