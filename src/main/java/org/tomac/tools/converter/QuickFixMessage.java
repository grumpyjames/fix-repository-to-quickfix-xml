package org.tomac.tools.converter;

import java.util.ArrayList;

/* <message name="Reject" msgcat="admin" msgtype="3"> */
public class QuickFixMessage {
	String name = "Reject";
	String msgcat = "admin";
	String msgtype = "3";
	ArrayList<QuickFixField> fields = new ArrayList<QuickFixField>();
	ArrayList<QuickFixComponent> components = new ArrayList<QuickFixComponent>();
	public String msgId;
	public String specialization;
	


}
