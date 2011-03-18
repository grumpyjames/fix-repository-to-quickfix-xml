/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;

public class FixRepositoryDom {

	public String major;
	
	public String type;
	
	public String servicepack;
	
	public String minor;
	
	public ArrayList<QuickFixMessage> quickFixMessages = new ArrayList<QuickFixMessage>();

	public HashMap<String, QuickFixMessage> quickFixNamedMessages = new HashMap<String, QuickFixMessage>();

	public HashMap<String, QuickFixMessage> quickFixMsgIDMessages = new HashMap<String, QuickFixMessage>();

	public ArrayList<QuickFixComponent> quickFixComponents = new ArrayList<QuickFixComponent>();

	public HashMap<String, QuickFixComponent> quickFixNamedComponents = new HashMap<String, QuickFixComponent>();

	public HashMap<String, QuickFixComponent> quickFixMsgIDComponents = new HashMap<String, QuickFixComponent>();

	public QuickFixComponent quickFixHeader = new QuickFixComponent();

	public QuickFixComponent quickFixTrailer = new QuickFixComponent();

	public ArrayList<QuickFixField> quickFixFields = new ArrayList<QuickFixField>();

	public HashMap<String, QuickFixField> quickFixNamedFields = new HashMap<String, QuickFixField>();
	
	public FixRepositoryDom() {
		String v = FixRepositoryToQuickFixXml.fixVersion;
		type = v.substring(0,4).equals("FIXT")?"FIXT":"FIX";
		if (type.equals("FIX")) {
			major = v.substring(4,5);
			minor = v.substring(6,7);
		} else { // FIXT
			major = v.substring(5,6);
			minor = v.substring(7,8);
		}
		servicepack = v.split("P").length>1?v.split("P")[1]:"0";
	}

	/*
	 * <Components> <NasdaqOMX/> <ComponentType>Block</ComponentType>
	 * <ComponentName>StandardHeader</ComponentName>
	 * <Category>Session</Category> <MsgID>1001</MsgID> </Components>
	 *    <Components>
      <ComponentName>SettlParties</ComponentName>
      <ComponentType>BlockRepeating</ComponentType>
      <Category>Common</Category>
      <MsgID>1017</MsgID>
   	*	</Components>
	 * 
	 */
	public void parseComponents(Element element) {

		for (final Iterator<Element> i = element.elementIterator("Components"); i.hasNext();) {
			final QuickFixComponent m = new QuickFixComponent();
			final Element e = i.next();

			for (final Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				final Element n = j.next();

				if (n.getName().equals("MsgID")) {
					m.msgId = n.getText().trim();
				}
				if (n.getName().equals("ComponentName")) {
					m.name = n.getText().trim();
				}
				if (n.getName().equals("ComponentType")) {
					m.isRepeating = n.getText().trim().endsWith("BlockRepeating")?true:false;
				}
			}
			quickFixComponents.add(m);
			quickFixNamedComponents.put(m.name, m);
			quickFixMsgIDComponents.put(m.msgId, m);
		}
	}

	/*
	 * <Enums> <NasdaqOMX> <Specialization>Accepted Cancel
	 * Replace</Specialization> </NasdaqOMX> <Tag>20</Tag> <Enum>0</Enum>
	 * <Description>New</Description> </Enums>
	 */
	public void parseEnums(Element element) {

		for (final Iterator<Element> i = element.elementIterator("Enums"); i.hasNext();) {
			String description = null;
			String tag = null;
			String fixEnum = null;

			final Element e = i.next();

			for (final Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				final Element n = j.next();

				if (n.getName().equals("Description")) {
					description = n.getText().trim();
				}
				if (n.getName().equals("Tag")) {
					tag = n.getText().trim();
				}
				if (n.getName().equals("Enum")) {
					fixEnum = n.getText().trim();
				}
			}
			final QuickFixField f = quickFixNamedFields.get(tag);
			f.quickFixValues.add(f.new QuickFixValue(fixEnum, description));
		}

	}

	/*
	 * <Fields> <NasdaqOMX> <Comment> </Comment> </NasdaqOMX> <Tag>103</Tag>
	 * <FieldName>OrdRejReason</FieldName> <LenRefers>0</LenRefers>
	 * <Type>int</Type> <Desc>Code to identify reason for order
	 * rejection.</Desc> </Fields>
	 */
	public void parseFields(Element element) {

		for (final Iterator<Element> i = element.elementIterator("Fields"); i.hasNext();) {
			final QuickFixField m = new QuickFixField();
			final Element e = i.next();

			for (final Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				final Element n = j.next();

				if (n.getName().equals("Type")) {
					m.type = n.getText().trim();
				}
				if (n.getName().equals("Tag")) {
					m.number = n.getText().trim();
				}
				if (n.getName().equals("FieldName")) {
					m.name = n.getText().trim();
				}
			}
			quickFixFields.add(m);
			quickFixNamedFields.put(m.number, m);
		}

	}

	/*
	 * <MsgContents> <NasdaqOMX> <Comment>ISIN code</Comment> <Reqd>1</Reqd>
	 * <Specialization>Accepted Cancel Replace</Specialization> </NasdaqOMX>
	 * <Indent>0</Indent> <Description> </Description> <MsgID>30000</MsgID>
	 * <Reqd>0</Reqd> <Position>20</Position> <TagText>48</TagText>
	 * </MsgContents>
	 */
	public void parseMsgContents(Element element) {

		for (final Iterator<Element> i = element.elementIterator("MsgContents"); i.hasNext();) {
			String msgId = null;
			String reqd = null;
			String tagText = null;
			final int indentOld = 0;
			int indent = 0;
			String position = null;

			final Element e = i.next();

			for (final Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				final Element n = j.next();

				if (n.getName().equals("MsgID")) {
					msgId = n.getText().trim();
				}
				if (n.getName().equals("Reqd") && reqd == null) {
					reqd = n.getText().trim();
				}
				if (n.getName().equals("TagText")) {
					tagText = n.getText().trim();
				}
				if (n.getName().equals("Indent")) {
					indent = Integer.valueOf(n.getText().trim());
				}
				if (n.getName().equals("Position")) {
					position = n.getText().trim();
				}

				// NASDAQ OMX special
				if (FixRepositoryToQuickFixXml.isNasdaqOMX) {
					if (n.getName().equals("NasdaqOMX")) {
						((Element) n.elementIterator("Specialization").next()).getText().trim();
						if (n.elementIterator("Reqd").hasNext()) {
							reqd = ((Element) n.elementIterator("Reqd").next()).getText().trim();
						}
					}
				}
			}
			QuickFixField f = null;
			if (quickFixNamedFields.get(tagText) != null) {
				f = new QuickFixField(quickFixNamedFields.get(tagText), reqd, position);
			}
			
			final QuickFixMessage m = quickFixMsgIDMessages.get(msgId);

			QuickFixComponent c = null;
			if (quickFixNamedComponents.get(tagText) != null) {
				c = new QuickFixComponent(quickFixNamedComponents.get(tagText), reqd, position);
			}
			final QuickFixComponent cC = quickFixMsgIDComponents.get(msgId);

			if (m != null && f != null) { // field in message
				m.fields.add(f);
			} 
			
			if (m != null && c != null && f == null) {  // component
														// in
														// message
				m.components.add(c);
			} 
			
			if (m == null && cC != null && f != null) { // field in
														// component
				cC.fields.add(f);
			} 
			
			if (c != null && cC != null && f == null) { // component
														// in
														// component
					cC.components.add(c);
			}

		}

		// special hearder and tail
		quickFixHeader = quickFixNamedComponents.get("StandardHeader");

		quickFixTrailer = quickFixNamedComponents.get("StandardTrailer");

	}

	/*
	 * <MsgType> <NasdaqOMX> <Comment> </Comment> <Specialization>Accepted
	 * Cancel Replace</Specialization> </NasdaqOMX>
	 * <Category>SingleGeneralOrderHandling</Category> <MsgID>30000</MsgID>
	 * <Section>Trade</Section> <ComponentType>Message</ComponentType>
	 * <MsgType>8</MsgType> <MessageName>Execution Report</MessageName>
	 * </MsgType>
	 */
	public void parseMsgType(Element element) throws Exception {

		for (final Iterator<Element> i = element.elementIterator("MsgType"); i.hasNext();) {
			final QuickFixMessage m = new QuickFixMessage();
			final Element e = i.next();

			for (final Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				final Element n = j.next();

				if (n.getName().equals("MsgID")) {
					m.msgId = n.getText().trim();
				}
				if (n.getName().equals("MsgType")) {
					m.msgtype = n.getText().trim();
				}
				if (n.getName().equals("MessageName")) {
					m.name = n.getText().trim();
				}
				if (n.getName().equals("Section")) {
					m.msgcat = n.getText().trim();
				}

				// NASDAQ OMX special
				if (FixRepositoryToQuickFixXml.isNasdaqOMX) {
					if (n.getName().equals("NasdaqOMX")) {
						m.specialization = ((Element) n.elementIterator("Specialization").next()).getText().trim();
					}
				}
			}
			quickFixMessages.add(m);
			quickFixNamedMessages.put(m.specialization != null ? m.specialization : m.name, m);
			quickFixMsgIDMessages.put(m.msgId, m);
		}
	}

}
