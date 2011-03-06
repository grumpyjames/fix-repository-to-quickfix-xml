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
	public static boolean isNasdaqOMX = true;

	
    public static void main (String[] args)
    {
        if (args.length < 1)
        {
            System.out.println ("Usage: FixRepositoryToQuickFixXml [FIX repository directory] [output file]");

            return;
        }

        File repositoryDir = new File (args[0]);

        if (! repositoryDir.exists () || ! repositoryDir.isDirectory())
        {
            System.out.println ("FIX Repository dir " + args[0] + " cannot be found!");

            return;
        }

        File outputFile = new File (System.getProperty ("user.dir") + "quickfix.xml");

        if (args.length > 1)
        {
            outputFile = new File (args[1]);

            if (! outputFile.exists ())
            {

                try {
					outputFile.createNewFile();
				} catch (IOException e) {
					System.out.println("Error: " + e);

					e.printStackTrace();

					return;
				}
            }
        }
        
		FixRepositoryDom fixDom = new FixRepositoryDom();
		
		try {
			xmlLoadAndValidate(repositoryDir, fixDom);
		} catch (Exception e) {

			System.out.println("Error: " + e);

			e.printStackTrace();

			return;
		}
        
        try
        {
            new XmlTranslator ().translate (fixDom, outputFile);
        }
        catch (Exception e)
        {
            System.out.println ("Error: " + e);

            e.printStackTrace ();

            return;
        }

        System.out.println ("Done.");
    }

    static void xmlLoadAndValidate (File repositoryDir, FixRepositoryDom fixDom) throws Exception
    {
        String[] files = repositoryDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.equalsIgnoreCase(MSGTYPE_XML)) return true;
				if (name.equalsIgnoreCase(COMPONENTS_XML)) return true;
				if (name.equalsIgnoreCase(FIELDS_XML)) return true;
				if (name.equalsIgnoreCase(ENUMS_XML)) return true;
				if (name.equalsIgnoreCase(MSGCONTENTS_XML)) return true;
				return false;
			}
		});

        // the order is important - from top MsgType -> Components and Fields -> Enums and finaly MsgContents
        String[] ss = {MSGTYPE_XML, COMPONENTS_XML, FIELDS_XML, ENUMS_XML, MSGCONTENTS_XML };
        for (String file :  ss ) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read (repositoryDir + System.getProperty("file.separator") + file);

            if (file.equalsIgnoreCase(MSGTYPE_XML)) 
            	fixDom.parseMsgType (doc.getRootElement());
            if (file.equalsIgnoreCase(COMPONENTS_XML)) 
            	fixDom.parseComponents (doc.getRootElement());
            if (file.equalsIgnoreCase(FIELDS_XML)) 
            	fixDom.parseFields (doc.getRootElement());
            if (file.equalsIgnoreCase(ENUMS_XML)) 
            	fixDom.parseEnums (doc.getRootElement());
            if (file.equalsIgnoreCase(MSGCONTENTS_XML)) 
            	fixDom.parseMsgContents (doc.getRootElement());
        	
        }
    }    
	
	
}
