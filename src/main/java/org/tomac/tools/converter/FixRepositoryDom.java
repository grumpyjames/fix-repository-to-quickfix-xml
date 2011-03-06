package org.tomac.tools.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixRepositoryDom {

	public String major = System.getProperty("FixMajorVersion", "4");

	public String type = "FIX";

	public String servicepack = "0";

	public String minor = System.getProperty("FixMinorVersion", "2");

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

	/*
	 * <MsgType> <NasdaqOMX> <Comment> </Comment> <Specialization>Accepted
	 * Cancel Replace</Specialization> </NasdaqOMX>
	 * <Category>SingleGeneralOrderHandling</Category> <MsgID>30000</MsgID>
	 * <Section>Trade</Section> <ComponentType>Message</ComponentType>
	 * <MsgType>8</MsgType> <MessageName>Execution Report</MessageName>
	 * </MsgType>
	 */
	public void parseMsgType(Element element) throws Exception {

		for (Iterator<Element> i = element.elementIterator("MsgType"); i
				.hasNext();) {
			QuickFixMessage m = new QuickFixMessage();
			Element e = i.next();

			for (Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				Element n = j.next();

				if (n.getName().equals("MsgID"))
					m.msgId = n.getText().trim();
				if (n.getName().equals("MsgType"))
					m.msgtype = n.getText().trim();
				if (n.getName().equals("MessageName"))
					m.name = n.getText().trim();
				if (n.getName().equals("Section"))
					m.msgcat = n.getText().trim();

				// NASDAQ OMX special
				if (FixRepositoryToQuickFixXml.isNasdaqOMX) {
					if (n.getName().equals("NasdaqOMX")) {
						m.specialization = ((Element) n.elementIterator(
								"Specialization").next()).getText().trim();
					}
				}
			}
			quickFixMessages.add(m);
			quickFixNamedMessages.put(
					m.specialization != null ? m.specialization : m.name, m);
			quickFixMsgIDMessages.put(m.msgId, m);
		}
	}

	/*
	 * <Components> <NasdaqOMX/> <ComponentType>Block</ComponentType>
	 * <ComponentName>StandardHeader</ComponentName>
	 * <Category>Session</Category> <MsgID>1001</MsgID> </Components>
	 */
	public void parseComponents(Element element) {

		for (Iterator<Element> i = element.elementIterator("Components"); i
				.hasNext();) {
			QuickFixComponent m = new QuickFixComponent();
			Element e = i.next();

			for (Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				Element n = j.next();

				if (n.getName().equals("MsgID"))
					m.msgId = n.getText().trim();
				if (n.getName().equals("ComponentName"))
					m.name = n.getText().trim();
			}
			quickFixComponents.add(m);
			quickFixNamedComponents.put(m.name, m);
			quickFixMsgIDComponents.put(m.msgId, m);
		}
	}

	/*
	 * <Fields> <NasdaqOMX> <Comment> </Comment> </NasdaqOMX> <Tag>103</Tag>
	 * <FieldName>OrdRejReason</FieldName> <LenRefers>0</LenRefers>
	 * <Type>int</Type> <Desc>Code to identify reason for order
	 * rejection.</Desc> </Fields>
	 */
	public void parseFields(Element element) {

		for (Iterator<Element> i = element.elementIterator("Fields"); i
				.hasNext();) {
			QuickFixField m = new QuickFixField();
			Element e = i.next();

			for (Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				Element n = j.next();

				if (n.getName().equals("Type"))
					m.type = n.getText().trim();
				if (n.getName().equals("Tag"))
					m.number = n.getText().trim();
				if (n.getName().equals("FieldName"))
					m.name = n.getText().trim();
			}
			quickFixFields.add(m);
			quickFixNamedFields.put(m.number, m);
		}

	}

	/*
	 * <Enums> <NasdaqOMX> <Specialization>Accepted Cancel
	 * Replace</Specialization> </NasdaqOMX> <Tag>20</Tag> <Enum>0</Enum>
	 * <Description>New</Description> </Enums>
	 */
	public void parseEnums(Element element) {

		for (Iterator<Element> i = element.elementIterator("Enums"); i
				.hasNext();) {
			String description = null;
			String tag = null;
			String fixEnum = null;

			Element e = i.next();

			for (Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				Element n = j.next();

				if (n.getName().equals("Description"))
					description = n.getText().trim();
				if (n.getName().equals("Tag"))
					tag = n.getText().trim();
				if (n.getName().equals("Enum"))
					fixEnum = n.getText().trim();
			}
			QuickFixField f = quickFixNamedFields.get(tag);
			f.quickFixValues.add(f.new QuickFixValue(fixEnum, description));
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

		for (Iterator<Element> i = element.elementIterator("MsgContents"); i
				.hasNext();) {
			String msgId = null;
			String reqd = null;
			String tagText = null;
			String specialization = null;
			int indentOld = 0, indent = 0;
			QuickFixMessage mold = null;
			String position = null;

			Element e = i.next();

			for (Iterator<Element> j = e.elementIterator(); j.hasNext();) {
				Element n = j.next();

				if (n.getName().equals("MsgID"))
					msgId = n.getText().trim();
				if (n.getName().equals("Reqd") && reqd == null)
					reqd = n.getText().trim();
				if (n.getName().equals("TagText"))
					tagText = n.getText().trim();
				if (n.getName().equals("Indent"))
					indent = Integer.valueOf(n.getText().trim());
				if (n.getName().equals("Position"))
					position = n.getText().trim();

				// NASDAQ OMX special
				if (FixRepositoryToQuickFixXml.isNasdaqOMX) {
					if (n.getName().equals("NasdaqOMX")) {
						specialization = ((Element) n.elementIterator(
								"Specialization").next()).getText().trim();
						if (n.elementIterator("Reqd").hasNext())
							reqd = ((Element) n.elementIterator("Reqd").next())
									.getText().trim();
					}
				}
			}
			QuickFixField f = null;
			if (quickFixNamedFields.get(tagText) != null)
				f = new QuickFixField(quickFixNamedFields.get(tagText), reqd, position);
			QuickFixMessage m = quickFixMsgIDMessages.get(msgId);
			QuickFixComponent c = null; 
			if (quickFixNamedComponents.get(tagText) != null)
				c = new QuickFixComponent(quickFixNamedComponents.get(tagText), reqd, position);
			QuickFixComponent cC = quickFixMsgIDComponents.get(msgId); 

			if (indent == indentOld) {
				if (m != null && f != null) { // field in message
					m.fields.add(f);
				} else if (m != null && c != null && f == null) { // component in message 
					m.components.add(c);
				} else if (m == null && cC != null && f != null) { // field in component
					cC.fields.add(f);
				} else if (c != null && cC != null && f == null) { // component in component
					cC.components.add(c);
				}
					
			}
			mold = m;

		}

		// specail hearder and tail
		quickFixHeader = quickFixNamedComponents.get("StandardHeader");

		quickFixTrailer = quickFixNamedComponents.get("StandardTrailer");

	}

}
