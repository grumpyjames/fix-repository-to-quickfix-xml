package org.tomac.tools.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.tomac.tools.converter.QuickFixDom.DomFixComponent;
import org.tomac.tools.converter.nordic.NordicUtils;

public class QuickFixToFixRepositoryXml {

	static String MSGTYPE_XML = "MsgType.xml";
	static String COMPONENTS_XML = "Components.xml";
	static String FIELDS_XML = "Fields.xml";
	static String ENUMS_XML = "Enums.xml";
	static String MSGCONTENTS_XML = "MsgContents.xml";
	public static String fixVersion = "FIX.5.0SP2";
	public static String fixFlavour = "FIXRepository_FIX.5.0SP2 from www.fixprotocol.org";

	public static void main(String[] args) {
		
		QuickFixToFixRepositoryXml qf2fr = new QuickFixToFixRepositoryXml();
		
		if (args.length < 1) {
			System.out.println("Usage: QuickFixToFixRepositoryXml quick_fix_xml_spec [FIX repository output directory]");
			System.out.println("Property(default)");
			System.out.println("fixVersion(FIX.5.0SP2");
			System.out.println("fixFlavour(FIXRepository_FIX.5.0SP2 from www.fixprotocol.org)");

			return;
		}

		fixVersion = System.getProperty("fixVersion", fixVersion);
		System.out.println("fixVersion=" + fixVersion);
		fixFlavour = System.getProperty("fixFlavour", fixFlavour);
		System.out.println("fixFlavour=" + fixFlavour);

		final File inputFile = new File(args[0]);
		
		if (!inputFile.exists() || !inputFile.isFile()) {
			
			System.out.println("Quick FIX xml file " + args[0] + " cannot be found!");

			return;
		}

		String outputFile = "FixRepository";
		if (args.length > 1) outputFile = args[1];
		
		File outputDir = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + outputFile);

		if (!outputDir.exists()) {
			outputDir.mkdir();
		} else if (!outputDir.isDirectory())
			return;

		final QuickFixDom quickDom;

		try {
			quickDom = qf2fr.xmlLoadAndValidate(inputFile);

			qf2fr.translate(outputDir, quickDom);
			
		} catch (final Exception e) {
			System.out.println("Error: " + e);

			e.printStackTrace();

			return;
		}
		
		// validate our creation
		final FixRepositoryDom fixDom = new FixRepositoryDom();

		try {
			FixRepositoryToQuickFixXml.xmlLoadAndValidate(outputDir, fixDom);
		} catch (final Exception e) {

			System.out.println("Error: " + e);

			e.printStackTrace();

			return;
		}
		System.out.println("Done.");
	}

	private QuickFixDom xmlLoadAndValidate(File inputFile) throws Exception {
		final QuickFixDom fixDom = new QuickFixDom();

		final SAXReader reader = new SAXReader();
		final Document doc = reader.read(inputFile);

		fixDom.buildFrom(doc.getRootElement());

		return fixDom;
	}

	private void translate(File repositoryDir, QuickFixDom quickDom) throws Exception {
		repositoryDir.list(new FilenameFilter() {

			//@Override
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
			Document doc = null;
			
			if (file.equalsIgnoreCase(MSGTYPE_XML)) {
				doc = genMessages(quickDom);
			}
			else if (file.equalsIgnoreCase(MSGCONTENTS_XML)) {
				doc = genMsgContents(quickDom);
			}
			else if (file.equalsIgnoreCase(COMPONENTS_XML)) {
				doc = genComponents(quickDom);
			}
			else if (file.equalsIgnoreCase(FIELDS_XML)) {
				doc = genFields(quickDom);
			} 
			else if (file.equalsIgnoreCase(ENUMS_XML)) {
				doc = genEnums(quickDom);
			}
			writeXml(doc, new File(repositoryDir.getAbsolutePath() + System.getProperty("file.separator") + file) );
		}
	}
	
	private void writeXml(Document document, File file) throws IOException {
		// lets write to a file
		final OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(file), format);
		writer.write(document);
		writer.close();		
	}

	/*
  <MessageName>Heartbeat</MessageName> 
  <ComponentType>Message</ComponentType> 
  <Category>Session</Category> 
  <MsgID>1</MsgID> 
  <Section>Session</Section> 
  <AbbrName>Heartbeat</AbbrName> 
  <OverrideAbbr>Heartbeat</OverrideAbbr> 
  <Volume>Volume2</Volume> 
  <NotReqXML>1</NotReqXML> 
  </MsgType>
	*/
	private Document genMessages(QuickFixDom quickDom) {
		final Document document = DocumentHelper.createDocument();
		Element dataroot;
		
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		dataroot = document.addElement("dataroot").addAttribute("generated", dateFormatLocal.format(Calendar.getInstance().getTime())).addAttribute("latestEP", "DRAFT");

		int msgID = 0;
		for (final QuickFixDom.DomFixMessage m : quickDom.domFixMessages) {
			Element messages = dataroot.addElement("MsgType");
			
			messages.addElement("MsgType").addText(m.msgtype);
			messages.addElement("MessageName").addText(m.name);
			messages.addElement("ComponentType").addText("Message");
			messages.addElement("Category").addText(m.msgcat.equals("admin") ? "Session" : "Application");
			messages.addElement("MsgID").addText(String.valueOf(++msgID));
			messages.addElement("Section").addText(m.msgcat.equals("admin") ? "Session" : "Application");
			messages.addElement("AbbrName").addText(m.name);
			messages.addElement("OverrideAbbr").addText(m.name);
			messages.addElement("Volume").addText("Volume1");
			messages.addElement("NotReqXML").addText("1");
		}

		return document;
	}
	
	/*
<MsgContents>
<Indent>0</Indent>
<Position>1</Position>
<TagText>StandardHeader</TagText>
<Reqd>1</Reqd>
<Description>
      MsgType = 0
    </Description>
<MsgID>1</MsgID>
</MsgContents>
		 */
		private Document genMsgContents(QuickFixDom quickDom) {
			final Document document = DocumentHelper.createDocument();
			Element dataroot;
			
			SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			dataroot = document.addElement("dataroot").addAttribute("generated", dateFormatLocal.format(Calendar.getInstance().getTime())).addAttribute("latestEP", "DRAFT");

			int msgID = 0;
			for (final QuickFixDom.DomFixMessage m : quickDom.domFixMessages) {
				++msgID;

				Element messages = dataroot.addElement("MsgContents");
				int pos = 1;
				// standard header
				messages.addElement("Indent").addText("0");
				messages.addElement("Position").addText(String.valueOf(pos));
				messages.addElement("TagText").addText("StandardHeader");
				messages.addElement("Reqd").addText("1");
				messages.addElement("Description").addText("MsgType = " + m.msgtype);
				messages.addElement("MsgID").addText(String.valueOf(msgID));
				
				for (final QuickFixDom.DomBase b : m.fieldsAndComponents) {

					if (b instanceof QuickFixDom.DomFixField) {
						messages = dataroot.addElement("MsgContents");
						pos++;
						QuickFixDom.DomFixField f = (QuickFixDom.DomFixField)b;
						
						messages.addElement("Indent").addText("0");
						messages.addElement("Position").addText(String.valueOf(pos));
						messages.addElement("TagText").addText(f.number);
						messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
						messages.addElement("Description").addText(f.name);
						messages.addElement("MsgID").addText(String.valueOf(msgID));
					} else if (b instanceof QuickFixDom.DomFixComponentRef) {
						QuickFixDom.DomFixComponentRef f = (QuickFixDom.DomFixComponentRef)b;
						
						if (f.isRepeating()) {
							messages = dataroot.addElement("MsgContents");
							pos++;
							messages.addElement("Indent").addText(String.valueOf("0"));
							messages.addElement("Position").addText(String.valueOf(pos));
							messages.addElement("TagText").addText(f.noInGroupTag());
							messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
							messages.addElement("Description").addText(f.name);
							messages.addElement("MsgID").addText(String.valueOf(msgID)); // TODO get ordered pos in Components + 1000
						}

						messages = dataroot.addElement("MsgContents");
						pos++;
						messages.addElement("Indent").addText(String.valueOf(f.isRepeating()?"0":"1"));
						messages.addElement("Position").addText(String.valueOf(pos));
						messages.addElement("TagText").addText(f.name);
						messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
						messages.addElement("Description").addText(f.name);
						messages.addElement("MsgID").addText(String.valueOf(getCompnentMsgID(f.name, quickDom.domFixComponents)));
					}

				}

				// standard trailer
				messages = dataroot.addElement("MsgContents");
				pos++;
				messages.addElement("Indent").addText("0");
				messages.addElement("Position").addText(String.valueOf(pos));
				messages.addElement("TagText").addText("StandardTrailer");
				messages.addElement("Reqd").addText("1");
				messages.addElement("Description").addText("MsgType = " + m.msgtype);
				messages.addElement("MsgID").addText(String.valueOf(msgID));
				
			}
			
			// now all the components
			msgID = 1000 - 1;
			for (final QuickFixDom.DomFixComponent m : quickDom.domFixComponents) {
				++msgID;

				genMsgContentsComponent(quickDom, m, dataroot, msgID);
			}
			
			++msgID;
			genMsgContentsComponent(quickDom, quickDom.domFixHeader, dataroot, msgID);

			++msgID;
			genMsgContentsComponent(quickDom, quickDom.domFixTrailer, dataroot, msgID);
			
			return document;
		}


	private void genMsgContentsComponent(QuickFixDom quickDom, QuickFixDom.DomFixComponent m, Element dataroot, int msgID) {
		Element messages = dataroot.addElement("MsgContents");
		int pos = 1;
		
		for (final QuickFixDom.DomBase b : m.fieldsAndComponents) {

			if (b instanceof QuickFixDom.DomFixField) {
				messages = dataroot.addElement("MsgContents");
				pos++;
				QuickFixDom.DomFixField f = (QuickFixDom.DomFixField)b;
				
				messages.addElement("Indent").addText("0");
				messages.addElement("Position").addText(String.valueOf(pos));
				messages.addElement("TagText").addText(f.number);
				messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
				messages.addElement("Description").addText(f.name);
				messages.addElement("MsgID").addText(String.valueOf(msgID));
			} else if (b instanceof QuickFixDom.DomFixComponentRef) {
				QuickFixDom.DomFixComponentRef f = (QuickFixDom.DomFixComponentRef)b;
				
				if (f.isRepeating()) {
					messages = dataroot.addElement("MsgContents");
					pos++;
					messages.addElement("Indent").addText(String.valueOf("0"));
					messages.addElement("Position").addText(String.valueOf(pos));
					messages.addElement("TagText").addText(f.noInGroupTag());
					messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
					messages.addElement("Description").addText(f.name);
					messages.addElement("MsgID").addText(String.valueOf(msgID)); // TODO get ordered pos in Components + 1000
				}

				messages = dataroot.addElement("MsgContents");
				pos++;
				messages.addElement("Indent").addText(String.valueOf(f.isRepeating()?"0":"1"));
				messages.addElement("Position").addText(String.valueOf(pos));
				messages.addElement("TagText").addText(f.name);
				messages.addElement("Reqd").addText(f.reqd.equals("Y")? "1" : "0");
				messages.addElement("Description").addText(f.name);
				messages.addElement("MsgID").addText(String.valueOf(getCompnentMsgID(f.name, quickDom.domFixComponents)));
			}

		}	}

	private int getCompnentMsgID(String name, ArrayList<DomFixComponent> domFixComponents) {
		int id = 1000;
		for (DomFixComponent c : domFixComponents) {
			if (c.name.equals(name)) return id;
			id++;
		}
		return id;
	}

	/*
<Components>
<ComponentName>CommissionData</ComponentName>
<ComponentType>Block</ComponentType>
<Category>Common</Category>
<MsgID>1000</MsgID>
<AbbrName>Comm</AbbrName>
<NotReqXML>0</NotReqXML>
</Components>
	 * 
<Components>
<ComponentName>CommissionData</ComponentName>
<ComponentType>Block</ComponentType>
<Category>Common</Category>
<MsgID>1000</MsgID>
<AbbrName>Comm</AbbrName>
<NotReqXML>0</NotReqXML>
</Components>
	 */
	private Document genComponents(QuickFixDom quickDom) {
		final Document document = DocumentHelper.createDocument();
		Element dataroot;
		
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		dataroot = document.addElement("dataroot").addAttribute("generated", dateFormatLocal.format(Calendar.getInstance().getTime())).addAttribute("latestEP", "DRAFT");

		int msgID = 1000;
		for (final QuickFixDom.DomFixComponent m : quickDom.domFixComponents) {
			Element messages = dataroot.addElement("Components");
			
			messages.addElement("ComponentName").addText(m.name);
			messages.addElement("ComponentType").addText(m.isRepeating?"BlockRepeating":"Block");
			messages.addElement("Category").addText("Common");
			messages.addElement("MsgID").addText(String.valueOf(msgID++));
			messages.addElement("AbbrName").addText(m.name);
			messages.addElement("NotReqXML").addText("0");
		}
		
		// StandardHeader
		Element messages = dataroot.addElement("Components");
		
		messages.addElement("ComponentName").addText("StandardHeader");
		messages.addElement("ComponentType").addText("Block");
		messages.addElement("Category").addText("Session");
		messages.addElement("MsgID").addText(String.valueOf(msgID++));
		messages.addElement("AbbrName").addText("StandardHeader");
		messages.addElement("NotReqXML").addText("0");
		
		messages = dataroot.addElement("Components");
		
		// StandardTrailer
		messages.addElement("ComponentName").addText("StandardTrailer");
		messages.addElement("ComponentType").addText("Block");
		messages.addElement("Category").addText("Session");
		messages.addElement("MsgID").addText("StandardTrailer");
		messages.addElement("AbbrName").addText(String.valueOf(msgID++));
		messages.addElement("NotReqXML").addText("0");	
		
		return document;
	}

	/*
<Fields>
<Tag>1</Tag>
<FieldName>Account</FieldName>
<Type>String</Type>
<Desc>
      Account mnemonic as agreed between buy and sell sides, e.g. broker and institution or investor/intermediary and fund manager.
    </Desc>
<AbbrName>Acct</AbbrName>
<NotReqXML>0</NotReqXML>
</Fields>
	 */
	private Document genFields(QuickFixDom quickDom) {
		final Document document = DocumentHelper.createDocument();
		Element dataroot;
		
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		dataroot = document.addElement("dataroot").addAttribute("generated", dateFormatLocal.format(Calendar.getInstance().getTime())).addAttribute("latestEP", "DRAFT");

		for (final QuickFixDom.DomFixField m : quickDom.domFixFields) {
			Element messages = dataroot.addElement("Fields");
			
			messages.addElement("Tag").addText(m.number);
			messages.addElement("FieldName").addText(m.name);
			messages.addElement("Type").addText(m.type.toLowerCase());
			messages.addElement("Desc").addText(m.name);
			messages.addElement("AbbrName").addText(m.name);
			messages.addElement("NotReqXML").addText("0");
		}

		return document;
	}

	/*
<Enums>
<Group />
<Sort>1</Sort>
<Tag>4</Tag>
<Enum>B</Enum>
<Description>Buy</Description>
</Enums>
<Enums>
<Group />

	 */
	private Document genEnums(QuickFixDom quickDom) {
		final Document document = DocumentHelper.createDocument();
		Element dataroot;
		
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		dataroot = document.addElement("dataroot").addAttribute("generated", dateFormatLocal.format(Calendar.getInstance().getTime())).addAttribute("latestEP", "DRAFT");

		for (final QuickFixDom.DomFixField m : quickDom.domFixFields) {
			
			if (m.domFixValues.size()<1) continue;
			
			int pos = 0;
			for ( QuickFixDom.DomFixField.DomFixValue v : m.domFixValues) {
				pos++;
				Element messages = dataroot.addElement("Enums");
			
				messages.addElement("Group");
				messages.addElement("Sort").addText(String.valueOf(pos));
				messages.addElement("Tag").addText(m.number);
				messages.addElement("Enum").addText(v.fixEnum);
				messages.addElement("Description").addText(v.description.replace('_', ' '));
			
			}
			
		}

		return document;
	}



}
