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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XmlTranslator {

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
		final Element root = document.addElement("fix").addAttribute("major", fixDom.major).addAttribute("minor", fixDom.minor)
				.addAttribute("servicepack", "0").addAttribute("type", "FIX");

		final Element header = root.addElement("header");
		for (final QuickFixField f : fixDom.quickFixHeader.fields) {
			addField(header, f);
		}

		final Element messages = root.addElement("messages");
		for (final QuickFixMessage m : fixDom.quickFixMessages) {
			String name;

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

			final Element message = messages.addElement("message").addAttribute("name", name).addAttribute("msgcat", getMsgCat(m.msgcat))
					.addAttribute("msgtype", m.msgtype);

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
				}

				// <component name="Parties" required="N"/>
				if (q instanceof QuickFixComponent) {
					final QuickFixComponent c = (QuickFixComponent) q;

					if (c.name.equals("StandardHeader") || c.name.equals("StandardTrailer")) {
						continue;
					}

					message.addElement("component").addAttribute("name", c.name).addAttribute("required", c.reqd);
				}

			}

		}

		final Element trailer = root.addElement("trailer");
		for (final QuickFixField f : fixDom.quickFixTrailer.fields) {
			addField(trailer, f);
		}

		final Element components = root.addElement("components");
		for (final QuickFixComponent c : fixDom.quickFixComponents) {
			if (c.name.equals("StandardHeader")) {
				continue;
			}
			if (c.name.equals("StandardTrailer")) {
				continue;
			}

			final Element component = components.addElement("component").addAttribute("name", c.name);

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

					component.addElement("field").addAttribute("name", f.name).addAttribute("number", f.number).addAttribute("type", getType(f.type));
				}

				if (q instanceof QuickFixComponent) {
					final QuickFixComponent cc = (QuickFixComponent) q;
					component.addElement("component").addAttribute("name", cc.name).addAttribute("required", cc.reqd);
				}
			}
		}

		final Element fields = root.addElement("fields");
		for (final QuickFixField f : fixDom.quickFixFields) {

			if (f.name.toLowerCase().contains(" ")) {
				continue;
			}

			final Element field = fields.addElement("field").addAttribute("name", f.name).addAttribute("number", f.number)
					.addAttribute("type", getType(f.type));

			for (final QuickFixField.QuickFixValue v : f.quickFixValues) {
				field.addElement("value").addAttribute("enum", v.fixEnum).addAttribute("description", getDescription(v.description));

			}

		}

		return document;
	}

	private String getDescription(String description) {
		final String tmp = description.toUpperCase().replaceAll(" ", "_").replaceAll("[^A-Z_]", "");
		return tmp.substring(0, tmp.length() > 32 ? 32 : tmp.length());
	}

	private String getMsgCat(String msgcat) {
		if (msgcat.equals("Session")) {
			return "admin";
		}
		return "app";
	}

	private String getType(String type) {
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
