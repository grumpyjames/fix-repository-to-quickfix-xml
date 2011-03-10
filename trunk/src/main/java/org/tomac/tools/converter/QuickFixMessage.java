/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

import java.util.ArrayList;

/* <message name="Reject" msgcat="admin" msgtype="3"> */
public class QuickFixMessage {
	public String name = "Reject";
	public String msgcat = "admin";
	public String msgtype = "3";
	public String msgsubtype = "";
	public ArrayList<QuickFixField> fields = new ArrayList<QuickFixField>();
	public ArrayList<QuickFixComponent> components = new ArrayList<QuickFixComponent>();
	public String msgId;
	public String specialization;
	public String direction;

}
