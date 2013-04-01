package adapter.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



public class XPathHelper {

	private static XPath xPath = null;
	private XPathFactory factory = null;
	private File xmlDocument = null;
	
	
	public XPathHelper(File xmlDocumentFile){
		
		// create a XPathFactory from the static method
		 this.factory = XPathFactory.newInstance();
		
		//create a XPath object
		this.xPath = factory.newXPath();
		
		this.xmlDocument = xmlDocumentFile;
		
	}
	
	
	/*public static void main(String[] args){
		String xml = "output.xml";
		File f = new File(xml);
		XPathHelper xhelper = new XPathHelper(f);
		
		//String expr = "/java-source-program/descendant::class[@name='externalUpdateData']/descendant::method";
		String expr = "/descendant::formal-argument/type";
		NodeList nodes = xhelper.getNodeList(expr);
		for(int i=0;i<nodes.getLength();i++){
			Node n = (Node) nodes.item(i);
			System.out.println(xhelper.getAttribute(n, "name"));
		}
	}*/
	
	protected String evaluate(String expr){
		try {
			XPathExpression xPathExpression = xPath.compile(expr);
			InputSource inputSource = new InputSource(new FileInputStream(xmlDocument));
			String value  = xPathExpression.evaluate(inputSource);
			return value;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	protected NodeList getNodeList(String expression){
		
					
		try {
			//String expression = "/java-source-program/descendant::class[@name='externalUpdateData']/descendant::method";
			//XPathExpression xPathExpression = xPath.compile(expression);
		
			InputSource inputSource = new InputSource(new FileInputStream(xmlDocument));
					
			
			NodeList nodes = (NodeList) xPath.evaluate(expression,inputSource, XPathConstants.NODESET);
			return nodes;
			/*for(int i=0;i<nodes.getLength();i++){
				Node n = (Node) nodes.item(i);
				System.out.println(this.getAttribute(n, "name"));
			}*/
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	protected String getAttribute(Node n,String attName){
		NamedNodeMap nnm = ((org.w3c.dom.Node) n).getAttributes();
		if(nnm!=null)
			for(int i=0;i<nnm.getLength();i++){
				Node aux = (Node) nnm.item(i);
				String auxName = ((org.w3c.dom.Node) aux).getNodeName();
				if(auxName.equalsIgnoreCase(attName)){
					return ((org.w3c.dom.Node) aux).getNodeValue();
				}
			}
		return null;
	}
}

