package org.tomac.tools.converter;

import java.util.ArrayList;

/*
<component name="Parties">
	<group name="NoPartyIDs" required="N">
		<field name="PartyID" required="N"/>
		<field name="PartyIDSource" required="N"/>
		<field name="PartyRole" required="N"/>
		<group name="NoPartySubIDs" required="N">
			<field name="PartySubID" required="N"/>
			<field name="PartySubIDType" required="N"/>
		</group>
	</group>
</component>
 */
public class QuickFixComponent extends QuickBase {

	public QuickFixComponent(QuickFixComponent q, String reqd, String position) {
		name = q.name;
		noName = q.noName;
		fields = q.fields;
		components = q.components;
		msgId = q.msgId;
		keyTag = q.keyTag;
		this.reqd = new String(reqd);
		this.position = position;
	}
	public QuickFixComponent() {
	}
	String name;
	String noName;
	public ArrayList<QuickFixField> fields = new ArrayList<QuickFixField>();
	public ArrayList<QuickFixComponent> components = new ArrayList<QuickFixComponent>();
	public String msgId;
	public String keyTag;
	public String reqd;
	
}
