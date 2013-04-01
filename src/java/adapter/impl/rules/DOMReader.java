package adapter.impl.rules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DOMReader {
	
	protected Node readFiles(File metadata){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder1;
	    Document document1;
	   
		try {
			//create a builder and document to read the xml
			builder1 = factory.newDocumentBuilder();
			document1 = builder1.parse( metadata );
					
		    Node root1 = document1.getFirstChild();
		    
		    return root1;
		 		    
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;

	}
	
	protected void getProvidedInterfaces(Node n,String element,List<JavaInfo> files){
		if(n==null)
			return;
		else{
			//find the tag implement
			if(n.getNodeName().equalsIgnoreCase(element)){
				String nodeValue = this.getAttribute(n, "interface");
				JavaInfo interfaceInfo = new JavaInfo();
				interfaceInfo.setAbsolutePath(nodeValue);
				files.add(interfaceInfo);
			}
			getProvidedInterfaces(n.getFirstChild(),element,files);
			getProvidedInterfaces(n.getNextSibling(),element,files);
			return;
		}
	}
	
	
	
	protected String getAttribute(Node n,String attName){
		NamedNodeMap nnm = n.getAttributes();
		for(int i=0;i<nnm.getLength();i++){
			Node aux = nnm.item(i);
			String auxName = aux.getNodeName();
			if(auxName.equalsIgnoreCase(attName)){
				return aux.getNodeValue();
			}
		}
		return null;
	}
	
	
	protected void fillJavaInfo(Node n,String element,List<JavaInfo> files){
		if(n==null)
			return;
		else{
			
			//System.out.println("[getInterface]node = "+n.getNodeName());
			//if the node is the element we are looking for...
			if(n.getNodeName().equalsIgnoreCase(element)){
				JavaInfo iinfo = null;
				Node parent  = n.getParentNode();
				//System.out.println("[getInterface] parent = "+parent.getNodeName());
				//get parent node information
				if(parent!=null){
					iinfo = new JavaInfo();
					String value = this.getAttribute(parent, "name");
					//System.out.println("[getInterface] parent.name = "+value);
					if(value!=null)
						iinfo.setAbsolutePath(value);
					String visibility = this.getAttribute(n, "visibility");
					if(visibility != null)
						iinfo.setVisibility(visibility);
				}
				
				Node aux = n.getPreviousSibling();
				while((aux!=null)&&
						( !((aux.getNodeName().equalsIgnoreCase("import")) || 
								(aux.getNodeName().equalsIgnoreCase("package-decl")) )) ){
					aux = aux.getPreviousSibling();
					System.out.println(aux.getNodeName());
				}
				if(aux != null){
					if(aux.getNodeName().equalsIgnoreCase("import")){
						String imp = this.getAttribute(aux, "module");
						if(imp != null)
							iinfo.addImport(imp);
						while((aux!=null)&&(!aux.getNodeName().equalsIgnoreCase("package-decl")))
							aux = aux.getPreviousSibling();
					}
					if((aux != null)&&(aux.getNodeName().equalsIgnoreCase("package-decl"))){
						String pack = this.getAttribute(aux, "name");
						if(pack != null)
							iinfo.setPackage(pack);
					}
					
				}
				files.add(iinfo);
			}
			fillJavaInfo(n.getFirstChild(),element,files);
			fillJavaInfo(n.getNextSibling(),element,files);
		}
			
		return;
	}
	
}
