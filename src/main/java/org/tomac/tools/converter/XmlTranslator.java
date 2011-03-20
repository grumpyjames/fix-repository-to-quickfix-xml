/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.tomac.tools.converter.QuickFixField.QuickFixValue;
import org.tomac.tools.converter.nordic.NordicUtils;

public class XmlTranslator {
	
	private boolean isFixT = false;
	final ArrayList<QuickFixField> fixTFields = new ArrayList<QuickFixField>();
	
	private void addField(Element e, QuickFixField f) {
		e.addElement("field").addAttribute("name", f.name).addAttribute("required", f.reqd != null && f.reqd.equals("1") ? "Y" : "N");

	}

	/*
	 * <fix major="4" type="FIX" servicepack="0" minor="2"> <header> <field
	 * name="BeginString" required="Y"/> <field name="BodyLength" required="Y"/>
	 * .. </header> <messages> <message name="Heartbeat" msgcat="admin"
	 * msgtype="0"> <field name="TestReqID" required="N"/> ..
	 */

	public Document createDocument(FixRepositoryDom fixDom) {
		final Document document = DocumentHelper.createDocument();
		Element root;
		if (FixRepositoryToQuickFixXml.isStrictQuickFix) {
			if (fixDom.type.equals("FIXT")) isFixT = true;
			root = document.addElement("fix").addAttribute("major", fixDom.major).addAttribute("minor", fixDom.minor)
				.addAttribute("servicepack", fixDom.servicepack).addAttribute("type", fixDom.type);
		} else {
			root = document.addElement("fix").addAttribute("major", fixDom.major).addAttribute("minor", fixDom.minor)
			.addAttribute("servicepack", fixDom.servicepack).addAttribute("type", fixDom.type).addAttribute("package", FixRepositoryToQuickFixXml.javaPackage)
			.addAttribute("flavour", FixRepositoryToQuickFixXml.fixFlavour);
		}

		final Element header = root.addElement("header");

		if(!FixRepositoryToQuickFixXml.isStrictQuickFix || isFixT || Integer.valueOf(fixDom.major)<5) {
			for (final QuickFixField f : fixDom.quickFixHeader.fields) {
				fixTFields.add(f);
				addField(header, f);
			}
		}

		final Element messages = root.addElement("messages");
		for (final QuickFixMessage m : fixDom.quickFixMessages) {
			String name;
			
			// strict quickfix has separate xml for FIXT
			if (FixRepositoryToQuickFixXml.isStrictQuickFix && isFixT && !m.msgcat.equals("Session")) {
				continue;
			} else if (FixRepositoryToQuickFixXml.isStrictQuickFix && m.msgcat.equals("Session") && Integer.valueOf(fixDom.major)>4) {
				continue;
			}
			
			if (m.name.equalsIgnoreCase("XML_non_FIX")) { // fixprotocol.org bug, empty message
				continue;
			}

			if (FixRepositoryToQuickFixXml.isNasdaqOMX && m.specialization != null) {
				final String n = m.specialization.replaceAll("-", "").replaceAll(" ", "").replaceAll("[(]fill[)]", "Fill");
				if (n.length() > 0) {
					name = n;
				} else {
					name = m.name.replaceAll(" ", "");
				}
			} else {
				name = m.name.replaceAll(" ", "");
			}

			Element message;
			if (FixRepositoryToQuickFixXml.isNasdaqOMX && NordicUtils.isMessageWithSubMsgType(name)) {
				message = messages.addElement("message").addAttribute("name", name).addAttribute("msgcat", getMsgCat(m.msgcat))
				.addAttribute("msgtype", m.msgtype).addAttribute("msgsubtype", NordicUtils.getMessageSubMsgType(name) );
			} else {
				message = messages.addElement("message").addAttribute("name", name).addAttribute("msgcat", getMsgCat(m.msgcat))
					.addAttribute("msgtype", m.msgtype);
			}

			final ArrayList<QuickBase> qQ = new ArrayList<QuickBase>();
			qQ.addAll(m.fields);
			qQ.addAll(m.components);

			Collections.sort(qQ);

			for (final QuickBase q : qQ) {

				if (q instanceof QuickFixField) {
					final QuickFixField f = (QuickFixField) q;
					if (f.name.toLowerCase().contains("no longer used") || f.name.toLowerCase().contains("not defined")) {
						continue;
					}
					addField(message, f);
					fixTFields.add(f);
				}

				// <component name="Parties" required="N"/>
				if (q instanceof QuickFixComponent) {
					final QuickFixComponent c = (QuickFixComponent) q;

					if (c.name.equals("StandardHeader") || c.name.equals("StandardTrailer")) {
						continue;
					}

					message.addElement("component").addAttribute("name", c.name).addAttribute("required", c.reqd.equals("0")?"N":"Y");
				}

			}

		}

		final Element trailer = root.addElement("trailer");

		if(!FixRepositoryToQuickFixXml.isStrictQuickFix || isFixT || Integer.valueOf(fixDom.major)<5) {
			for (final QuickFixField f : fixDom.quickFixTrailer.fields) {
				fixTFields.add(f);
				addField(trailer, f);
			}
		}

		final Element components = root.addElement("components");
		for (final QuickFixComponent c : fixDom.quickFixComponents) {
			if (c.name.equals("StandardHeader")) {
				continue;
			}
			if (c.name.equals("StandardTrailer")) {
				continue;
			}
			
			// ugh hardcoded components for FIXT
			// strict quickfix has separate xml for FIXT
			if (FixRepositoryToQuickFixXml.isStrictQuickFix && isFixT && !c.name.equals("HopGrp") && !c.name.equals("MsgTypeGrp")) {
				continue;
			} else if (FixRepositoryToQuickFixXml.isStrictQuickFix && (c.name.equals("HopGrp") || c.name.equals("MsgTypeGrp")) && Integer.valueOf(fixDom.major)>4) {
				continue;
			}

			for (QuickFixField f : c.fields)
				fixTFields.add(f);
			
			final Element component = components.addElement("component").addAttribute("name", c.name);

			final ArrayList<QuickBase> qq = new ArrayList<QuickBase>();
			qq.addAll(c.fields);

			Collections.sort(qq);
			QuickFixField qf = null;
			for (final QuickBase q : qq) {

				if (q instanceof QuickFixField) {
					qf = (QuickFixField) q;
					break;
				}
			}			
			
			final Element group = c.isRepeating?component.addElement("group").addAttribute("name", qf.name).addAttribute("required", qf.reqd.equals("0")?"N":"Y"):component;

			final ArrayList<QuickBase> qQ = new ArrayList<QuickBase>();
			qQ.addAll(c.fields);
			qQ.addAll(c.components);

			Collections.sort(qQ);

			for (final QuickBase q : qQ) {

				if (q instanceof QuickFixField) {
					final QuickFixField f = (QuickFixField) q;

					if (f.name.toLowerCase().contains(" ")) {
						continue;
					}
					
					if (f.type.equalsIgnoreCase("NUMINGROUP")) {
						continue;
					}

					group.addElement("field").addAttribute("name", f.name).addAttribute("number", f.number).addAttribute("type", getType(f.type));
				}

				if (q instanceof QuickFixComponent) {
					final QuickFixComponent cc = (QuickFixComponent) q;
					group.addElement("component").addAttribute("name", cc.name).addAttribute("required", cc.reqd.equals("0")?"N":"Y");
				}
			}
		}

		final Element fields = root.addElement("fields");

		if (FixRepositoryToQuickFixXml.isStrictQuickFix) {
			for (final QuickFixField f : fixTFields) {
				addField(f, fields);
			}
		} else {
			for (final QuickFixField f : fixDom.quickFixFields) {
				addField(f, fields);
			}
		}

		return document;
	}

	HashMap<String, QuickFixField> fieldsUnique = new HashMap<String, QuickFixField>();
	
	private void addField(QuickFixField f, Element fields) {
		
		if (f.name.toLowerCase().contains(" ")) {
			return;
		}

		if (null != fieldsUnique.put(f.name, f)) {
			return; // field already added
		}

		Element field;
		if (FixRepositoryToQuickFixXml.isNasdaqOMX && NordicUtils.isTagWithLength(f.number)) {
			field = fields.addElement("field").addAttribute("name", f.name).addAttribute("number", f.number)
				.addAttribute("type", getType(f.type)).addAttribute("length", NordicUtils.getTagLength(f.number));
		} else {
			field = fields.addElement("field").addAttribute("name", f.name).addAttribute("number", f.number)
			.addAttribute("type", getType(f.type));
		}

		HashMap<String, QuickFixValue> quickFixNamedValue = new HashMap<String, QuickFixValue>();
		for (final QuickFixValue v : f.quickFixValues) {
			quickFixNamedValue.put(v.fixEnum, v);
		}
		// NasdaqOMX bug
		if (FixRepositoryToQuickFixXml.isNasdaqOMX && f.name.equalsIgnoreCase("ExecTransType")) {
			for (final QuickFixValue v : NordicUtils.getExecTransAdditionalTypes(f)) {
				quickFixNamedValue.put(v.fixEnum, v);
			}
		}

		for (final QuickFixValue v : quickFixNamedValue.values()) {
			if (v.fixEnum.length() == 0) continue; // NasdaqOMX bug 
			field.addElement("value").addAttribute("enum", v.fixEnum.replaceAll("[\\s\\xA0]", "")).addAttribute("description", getDescription(v.description));
		}
	}

	private String getDescription(String description) {
		String tmp = description.toUpperCase().replaceAll(" ", "_").replaceAll("__", "_").
		replaceAll("[^0-9A-Z_]", "");
		if (tmp.substring(0,1).matches("[0-9]")) tmp = "I" + tmp; 
		
		// to long for name differentiation 
		if( tmp.contains("EXACT_MATCH_ON_TRADE_DATE_STOCK_SYMBOL_QUANTITY") ) {
			tmp = tmp.replace("EXACT_MATCH_ON_TRADE_DATE_STOCK_SYMBOL_QUANTITY", "");
		}
		
		return tmp.substring(0, tmp.length() > 64 ? 64 : tmp.length());
	}

	private String getMsgCat(String msgcat) {
		if (msgcat.equals("Session")) {
			return "admin";
		}
		return "app";
	}

	private String getType(String type) {
		if(type.toUpperCase().equals("MULTIPLEVALUESTRING")) return "MULTIPLESTRINGVALUE"; // Nasdaq OMX bug
		return type.toUpperCase();
	}

	public void translate(FixRepositoryDom fixDom, File outputDir) throws IOException {

		final Document doc = createDocument(fixDom);
		write(doc, outputDir);
	}

	public void write(Document document, File outputDir) throws IOException {

		// lets write to a file
		final OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(outputDir), format);
		writer.write(document);
		writer.close();

		// Pretty print the document to System.out
		writer = new XMLWriter(System.out, format);
		writer.write(document);

	}

}
