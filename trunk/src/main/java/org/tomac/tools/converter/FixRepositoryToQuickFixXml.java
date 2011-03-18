/**
 * Copyright (c) 2011 Sebastian Tomac (tomac.org)
 * Licensed under LGPL licenses.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 **/
package org.tomac.tools.converter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class FixRepositoryToQuickFixXml {

	static String MSGTYPE_XML = "MsgType.xml";
	static String COMPONENTS_XML = "Components.xml";
	static String FIELDS_XML = "Fields.xml";
	static String ENUMS_XML = "Enums.xml";
	static String MSGCONTENTS_XML = "MsgContents.xml";
	public static boolean isStrictQuickFix = false;
	public static boolean isNasdaqOMX = false;
	public static String fixVersion = "FIX.5.0SP2";
	public static String fixFlavour = "FIXRepository_FIX.5.0SP2 from www.fixprotocol.org";
	public static String javaPackage = "org.tomac.protocol.fix.messaging";

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: FixRepositoryToQuickFixXml [FIX repository directory] [output file]");
			System.out.println("Property(default)");
			System.out.println("isStrictQuickFix(false)]");
			System.out.println("isNasdaqOMX(false)]");
			System.out.println("fixVersion(FIX.5.0SP2");
			System.out.println("fixFlavour(FIXRepository_FIX.5.0SP2 from www.fixprotocol.org)");

			return;
		}

		final File repositoryDir = new File(args[0]);
		
		isNasdaqOMX = Boolean.valueOf(System.getProperty("isNasdaqOMX", String.valueOf(isNasdaqOMX)));
		System.out.println("isNasdaqOMX=" + isNasdaqOMX);
		fixVersion = System.getProperty("fixVersion", fixVersion);
		System.out.println("fixVersion=" + fixVersion);
		fixFlavour = System.getProperty("fixFlavour", fixFlavour);
		System.out.println("fixFlavour=" + fixFlavour);
		isStrictQuickFix = Boolean.valueOf(System.getProperty("isStrictQuickFix", "false"));
		System.out.println("isStrictQuickFix=" + isStrictQuickFix);

		if (!repositoryDir.exists() || !repositoryDir.isDirectory()) {
			System.out.println("FIX Repository dir " + args[0] + " cannot be found!");

			return;
		}

		File outputFile = new File(System.getProperty("user.dir") + "quickfix.xml");

		if (args.length > 1) {
			outputFile = new File(args[1]);

			if (!outputFile.exists()) {

				try {
					outputFile.createNewFile();
				} catch (final IOException e) {
					System.out.println("Error: " + e);

					e.printStackTrace();

					return;
				}
			}
		}

		final FixRepositoryDom fixDom = new FixRepositoryDom();

		try {
			xmlLoadAndValidate(repositoryDir, fixDom);
		} catch (final Exception e) {

			System.out.println("Error: " + e);

			e.printStackTrace();

			return;
		}

		try {
			new XmlTranslator().translate(fixDom, outputFile);
		} catch (final Exception e) {
			System.out.println("Error: " + e);

			e.printStackTrace();

			return;
		}

		System.out.println("Done.");
	}

	static void xmlLoadAndValidate(File repositoryDir, FixRepositoryDom fixDom) throws Exception {
		repositoryDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.equalsIgnoreCase(MSGTYPE_XML)) {
					return true;
				}
				if (name.equalsIgnoreCase(COMPONENTS_XML)) {
					return true;
				}
				if (name.equalsIgnoreCase(FIELDS_XML)) {
					return true;
				}
				if (name.equalsIgnoreCase(ENUMS_XML)) {
					return true;
				}
				if (name.equalsIgnoreCase(MSGCONTENTS_XML)) {
					return true;
				}
				return false;
			}
		});

		// the order is important - from top MsgType -> Components and Fields ->
		// Enums and finaly MsgContents
		final String[] ss = { MSGTYPE_XML, COMPONENTS_XML, FIELDS_XML, ENUMS_XML, MSGCONTENTS_XML };
		for (final String file : ss) {
			final SAXReader reader = new SAXReader();
			final Document doc = reader.read(repositoryDir + System.getProperty("file.separator") + file);

			if (file.equalsIgnoreCase(MSGTYPE_XML)) {
				fixDom.parseMsgType(doc.getRootElement());
			}
			if (file.equalsIgnoreCase(COMPONENTS_XML)) {
				fixDom.parseComponents(doc.getRootElement());
			}
			if (file.equalsIgnoreCase(FIELDS_XML)) {
				fixDom.parseFields(doc.getRootElement());
			}
			if (file.equalsIgnoreCase(ENUMS_XML)) {
				fixDom.parseEnums(doc.getRootElement());
			}
			if (file.equalsIgnoreCase(MSGCONTENTS_XML)) {
				fixDom.parseMsgContents(doc.getRootElement());
			}

		}
	}

}
