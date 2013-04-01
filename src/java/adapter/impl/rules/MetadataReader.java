package adapter.impl.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MetadataReader extends DefaultHandler{
	
	private static List data;
	private static String searchedElement;
	private static HashMap<String,String> searchedAttrs;
	MetadataReader(){
		data = new ArrayList();
		searchedElement = new String();
		searchedAttrs = new HashMap<String,String>();
	}
	
	
	public List getMetadata(String filename,String element,HashMap<String,String> filter)  {
	   System.out.println("metadata  = "+filename);
	   System.out.println(searchedElement+ " = "+element);
	   searchedElement = element;
	   searchedAttrs = filter;
	   System.out.println(searchedElement+ " = "+element);
	    // Use an instance of ourselves as the SAX event handler
	   //DefaultHandler handler = new MetadataReader();
	   DefaultHandler handler = this;
	   // Use the default (non-validating) parser
	   SAXParserFactory factory = SAXParserFactory.newInstance();
	   try {
		  // Parse the input
		  SAXParser saxParser = factory.newSAXParser();
		 
		  saxParser.parse( new File(filename), handler);
		
		  
	   } catch (Throwable t) {
		   System.err.println(t.getLocalizedMessage());
		   t.printStackTrace();
	   }
	   return data;
	}

	
	
	    //===========================================================
	    // SAX DocumentHandler methods
	    //===========================================================

	    public void startDocument()
	    throws SAXException
	    {
	        
	    }

	    public void endDocument()
	    throws SAXException
	    {
	       ;
	    }

	    public void startElement(String namespaceURI,
	                             String lName, // local name
	                             String qName, // qualified name
	                             Attributes attrs)
	    throws SAXException
	    {
	    	boolean elemFound = true;
	    	int nameIndex = -1;
	    	String eName = lName; // element name
	    	if ("".equals(eName)) eName = qName; // namespaceAware = false
	    	/*System.out.println("entrou no parse...");*/
	    	//System.out.println("elemento procurado = "+searchedElement);
	        if(eName.equalsIgnoreCase(searchedElement)){ //so imprime elementos topic
	        	//System.out.println("achou "+searchedElement);
	        	if (attrs != null) {
	        		for (int i = 0; i < attrs.getLength(); i++) {
	        			
	        			String aName = attrs.getQName(i); // Attr name
	        			String aValue = attrs.getValue(i);
	        			System.out.println("<interface "+aName+"="+aValue);
	        			String filterValue = (String) searchedAttrs.get(aName);
	        			if((filterValue != null)&&(!filterValue.equalsIgnoreCase(aValue)))
	        				elemFound = false;
	        			System.out.println("attribute "+aName+" = "+attrs.getValue(i));
	        			if(aName.equalsIgnoreCase("name"))
	        				nameIndex = i;
	        			
	        		}
	        		if(elemFound && nameIndex > -1)
	        			data.add(attrs.getValue(nameIndex));
	        	}
	        	
	        }
	       
	    }

	    public void endElement(String namespaceURI,
	                           String sName, // simple name
	                           String qName  // qualified name
	                          )
	    throws SAXException
	    {
	     //   emit("</"+sName+">");
	    }

	    public void characters(char buf[], int offset, int len)
	    throws SAXException
	    {
	       // String s = new String(buf, offset, len);
	        //emit(s);
	    }

	   
	   
	


}
