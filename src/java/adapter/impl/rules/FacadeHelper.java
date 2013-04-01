package adapter.impl.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import adapter.impl.jarFiles.ClassMetaInfo;

/**
 * 
 * @author Leonardo Pondian Tizzei
 * this class helps in the creation of Facades. It provides methods to create the file and
 * the content of the Facades. Although it's necessary to get facade information before 
 * starting its creation. 
 *
 */
class FacadeHelper {
	
	private String metadataPath = "output.xml";
	
	public static void main(String[] args){
		FacadeHelper f = new FacadeHelper();
		String qualifiedName = "/home/lsd/ra001973/mestrado/transformador/caseStudy/sourceForge/jarupdator/local/rami/Aux/externalUpdateData.java";
		String metadataPath = "output.xml";
		String result = f.initializeConstructorParameters(qualifiedName, metadataPath);
		System.out.println("**********************************");
		System.out.println(result);
		System.out.println("**********************************");
		//String result2 = f.createParameters(method)
		System.out.println("**********************************");
		System.out.println(result);
		System.out.println("**********************************");
	}
	
	protected  boolean createFacade(File newComponentFolder,String newPackage,InterfaceMetaInfo imi,int index){
		if( ( newComponentFolder == null) || ( imi == null) || ( index < 0) )
			return false;
		else{
			String facadeName = "Facade"+index;
			StringBuilder facadeStr = this.facadeCreator(imi, newPackage, facadeName);
			boolean facadeCreated = this.createFacadeFile(facadeStr, newComponentFolder, facadeName);
			return facadeCreated;
		}
		
	}
	
	private boolean createFacadeFile(StringBuilder sb,File newComponentFolder,String facadeName){
		if( ( newComponentFolder != null ) && ( newComponentFolder.exists() ) 
				&& ( sb != null ) && ( facadeName != null ) ){
			try {
				File destination = new File(newComponentFolder.getCanonicalPath()+"/impl/"+facadeName+".java");
				destination.createNewFile();
				if( destination.exists()){
					FileWriter fw = new FileWriter(destination.getCanonicalPath());
					BufferedWriter out = new BufferedWriter(fw);    
					String str = new String(sb);
					out.write(str);
					out.close();
					System.out.println("File "+ destination.getAbsolutePath()+" was created");
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		else{
			System.err.println("Invalid parameters, method: 'createFacadeFile'");
		}
		return false;
	}
	
	private StringBuilder facadeCreator(InterfaceMetaInfo imi, String pack,String facadeName){
		StringBuilder sb = new StringBuilder("\npackage "+ pack +".impl;\n" +
				"import "+pack+".spec.prov.*;\n" + 
				"public class "+facadeName+" implements "+imi.getInterfaceName()+"{\n"+ 
					this.createMethods( imi )+
				"}\n"
				
				);
		return sb;
	}
	
	private String createParametersWithoutType(MyMethod method){
		List<Parameter> parameters = method.getParameters();
		//Collections.sort(parameters);
		String params = new String("");
		if(! parameters.isEmpty() ){
			int i = 0 ;
			for(i=0;i<(parameters.size()-1);i++){
				Parameter p = parameters.get(i);
				params += p.getName()+","; 
			}
			Parameter p = parameters.get(i);
			params += p.getName();
		}
		return params;
	}
	
	private String createParameters(MyMethod method){
		List<Parameter> parameters = method.getParameters();
		//Collections.sort(parameters);
		String params = new String("");
		if(! parameters.isEmpty() ){
			int i = 0 ;
			for(i=0;i<(parameters.size()-1);i++){
				Parameter p = parameters.get(i);
				params += p.getType()+" "+p.getName()+","; 
			}
			Parameter p = parameters.get(i);
			params += p.getType()+" "+p.getName();
		}
		return params;
	}
	
	
	
	private String createMethodBody(MyMethod method, InterfaceMetaInfo imi, boolean returnType){
		List<MyConstructor> constructors = imi.getConstructors();
		String methodBody = new String("");
		
		// get a non-private constructor
		if( ( constructors != null) && ( !constructors.isEmpty() ) ){
			int j = 0;
			MyConstructor c = constructors.get(j);
			while( ( j < constructors.size() ) && ( c != null) && ( !c.getVisibility().equals("private")) ){
				c = constructors.get(j);
				j++;
			}
			j--; //undo the addition
			
			// check if there is a non-private constructor
			if( ( j > constructors.size() ) || ( c == null ) ){
				System.err.println("Can't find a non-private constructor");
				List<ClassMetaInfo> cmiList = imi.getImplementedBy();
				if( ( cmiList != null ) && ( !cmiList.isEmpty() ) ){
					ClassMetaInfo cmi = cmiList.get(0);
					methodBody += imi.getInterfaceName() +" implementor = new " + cmi.getName() + "(";
					methodBody += this.createParametersWithoutType(method);
					methodBody += ");\n";
				}
				
			}
			else{
				//initialize the variables with null or false, create the object and call the method
				methodBody += this.initializeConstructorParameters( c );
				methodBody += imi.getInterfaceName() + " implementor = new "+c.getName()+"(";
				//methodBody += this.getConstructorParameters( c.getParameters() );
				methodBody += this.createParametersWithoutType(method);
				methodBody += ");\n";
			}
		}
		else{
			methodBody += imi.getImplementedBy().get(0)+" implementor = new " + imi.getImplementedBy().get(0) + "();\n";
		}
		
		//if the return type is void, we can't call and return the method in the same line
		if ( returnType ){
			methodBody += "implementor."+method.getName() + "(";
			methodBody += this.createParametersWithoutType(method);
			methodBody += ");\nreturn;"; 
		}
		else{
			methodBody += "return implementor."+method.getName() + "(";
			methodBody += this.createParametersWithoutType(method);
			methodBody += ");";
		}
		//System.out.println("method body =\n"+methodBody);
		return methodBody;
		
	}
		
			
	
	
	private String createMethods(InterfaceMetaInfo imi){
		List<MyMethod> methods = imi.getMethods();
		String methodStr = new String("");

		for(int i = 0 ; i < methods.size() ; i++){
			MyMethod method = methods.get(i);
			boolean returnTypeVoid = false;
			if(method.getReturnType().equals("void"))
				returnTypeVoid = true;
			methodStr += method.getVisibility() + " " + method.getReturnType() + " " + method.getName() + "(" +
			this.createParameters( method ) + "){\n" + this.createMethodBody( method , imi ,returnTypeVoid) + "\n}\n";
		}
		return methodStr;
	}
	
	protected String getAttribute(Node n,String attName){
		NamedNodeMap nnm = n.getAttributes();
		if(nnm!=null)
			for(int i=0;i<nnm.getLength();i++){
				Node aux = nnm.item(i);
				
				String auxName = aux.getNodeName();
				if(auxName.equalsIgnoreCase(attName)){
					return aux.getNodeValue();
				}
			}
		return null;
	}
	
	private String initializeConstructorParameters(String qualifiedName, String metadataPath){
		List<Parameter> paramList = this.getConstructorInformation(qualifiedName, metadataPath);
		if( ( paramList != null) && ( !paramList.isEmpty()) ){
			String initialization = new String("");
			int secondLast = paramList.size() - 1;
			int i = 0 ;
			for(i = 0 ; i < secondLast ; i++){
				Parameter param = paramList.get( i );
				String paramType = param.getType();
				if( paramType.equals("boolean"))
					initialization += param.getType() + " " + param.getName() + " = false;\n";
				else
					initialization += param.getType() + " " + param.getName() + " = null;\n";
			
			}
			Parameter param = paramList.get( i );
			String paramType = param.getType();
			if( paramType.equals("boolean"))
				initialization += param.getType() + " " + param.getName() + " = false;\n";
			else
				initialization += param.getType() + " " + param.getName() + " = null;\n";
			
			
			return initialization;
		}
		else
			return "";
	}
	
	private String initializeConstructorParameters(MyConstructor constructor){
		List<Parameter> paramList = constructor.getParameters();
		if( ( paramList != null) && ( !paramList.isEmpty()) ){
			String initialization = new String("");
			int secondLast = paramList.size() -1;
			int i = 0 ;
			for(i = 0 ; i < secondLast ; i++){
				Parameter param = paramList.get( i );
				String paramType = param.getType();
				if( paramType.equals("boolean"))
					initialization += param.getType() + " " + param.getName() + " = false;\n";
				else
					initialization += param.getType() + " " + param.getName() + " = null;\n";
			
			}
			Parameter param = paramList.get( i );
			String paramType = param.getType();
			if( paramType.equals("boolean"))
				initialization += param.getType() + " " + param.getName() + " = false;\n";
			else
				initialization += param.getType() + " " + param.getName() + " = null;\n";
			
			
			return initialization;
		}
		else
			return "";
	}
	
	private String getConstructorParameters(String qualifiedName, String metadataPath){
		List<Parameter> paramList = this.getConstructorInformation(qualifiedName, metadataPath);
		if( ( paramList != null) && ( !paramList.isEmpty()) ){
			String parameters = new String("");
			int secondLast = paramList.size() -1;
			int i = 0 ;
			for(i = 0 ; i < secondLast ; i++){
				Parameter param = paramList.get( i );
				parameters += param.getName() + ",";
			}
			Parameter param = paramList.get( i );
			parameters += param.getName();
			return parameters;
		}
		else
			return "";
	}
	
	private String getConstructorParameters(List<Parameter> paramList){
		if( ( paramList != null) && ( !paramList.isEmpty()) ){
			String parameters = new String("");
			int secondLast = paramList.size() -1;
			int i = 0 ;
			for(i = 0 ; i < secondLast ; i++){
				Parameter param = paramList.get( i );
				parameters += param.getName() + ",";
			}
			Parameter param = paramList.get( i );
			parameters += param.getName();
			return parameters;
		}
		else
			return "";
	}
	
	
	private List<Parameter> getConstructorInformation(String qualifiedName, String metadataPath){
		if( qualifiedName == null )
			return null;
		else{
			File metafile = new File(metadataPath);
			XPathHelper xhelper = new XPathHelper(metafile);
			
			
			String visibilityExpr = "/java-source-program/java-class-file[@name='"+qualifiedName+"']" +
			"/descendant::constructor/@visibility";
			System.out.println(visibilityExpr);
			String visibility = "empty";
			int i = 0;
			//get a visible constructor
			while( ( visibility != null) && ( !visibility.equals("") ) && 
					( !visibility.equals("public") ) && ( !visibility.equals("protected") ) ){
					visibility = xhelper.evaluate(visibilityExpr);
					System.out.println("visibility = "+visibility);
					i++;
			}
			
			if( ( visibility == null ) || ( visibility.equals("") ) )
				return null;
			else{
				// get the parameters node
				String expression = "/java-source-program/java-class-file[@name='"+qualifiedName+"']" +
				"/descendant::constructor["+i+"]/descendant::formal-argument";
				System.out.println(expression);
				NodeList nodelist = xhelper.getNodeList(expression);
				
				if ( ( nodelist != null ) && ( nodelist.getLength() == 0 )){
					System.err.println("Nodelist is either empty or null");
					return null;
				}
					
				
				//for each formal-argument(or parameter), store its name, type and position
				List<Parameter> paramList = new ArrayList<Parameter>();
				for(int j=0;j<nodelist.getLength();j++){
					Parameter p = new Parameter();
					Node aux = nodelist.item(j);
					//get parameter's name
					String paramName = this.getAttribute( aux , "name" );
					if( paramName != null){
						p.setName( paramName );
						//get the type of the parameter
						String typeExpr = "/java-source-program/java-class-file[@name='"+qualifiedName+"']" +
						"/descendant::constructor/descendant::formal-argument[@name='"+paramName+"']/type/@name";
						
						//teste
						System.out.println("typeExpr = "+typeExpr);
						
						String type = xhelper.evaluate(typeExpr);
						System.out.println("type = "+type);
						p.setType( type );
						// I am assuming that the program JAVA2XML put the parameters in a correct
						// position while extracting metadata from the source code. The samples I saw
						// has shown it is correct, but...
						p.setPosition( j );
					}
					paramList.add(p);
				}
				return paramList;
			}

		}
	}
}
