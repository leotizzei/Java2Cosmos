package adapter.impl.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
class FacadeHelperUsingJar {
	
	
	
	/*public static void main(String[] args){
		FacadeHelperUsingJar f = new FacadeHelperUsingJar();
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
	}*/
	
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
		if( imi != null ){
			String aux = imi.getInterfaceName();
			//the interface name corresponds to its whole name
			String interfaceName =  aux.replace('.', '_') ;
			StringBuilder sb = new StringBuilder("\npackage "+ pack +".impl;\n" +
					"import " + pack + ".spec.prov.*;\n" + 
					"public class " + facadeName + " implements " + interfaceName + "{\n"+ 
						this.createMethods( imi )+
					"}\n"
					
					);
			return sb;
		}
		else
			return null;
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
	
	
	
	/**
	 * this methods returns the whole method's body. In other words, it initializes variables,
	 *  create objects, makes method calls, etc... 
	 * @param method the method being created
	 * @param facadeName the name of the Facade on which this method will be created
	 * @param imi information about the interface that this Facade implements
	 * @return a string whose content is the method body
	 */
	private String createMethodBody(MyMethod method, String facadeName, InterfaceMetaInfo imi){
		
				
		/**********************************************************************************************/
		//Collections.sort(parameters);
		String methodBody = new String("");
		
		// set constructors
		List<ClassMetaInfo> implementors = imi.getImplementedBy();
		
		Class[] exceptions = null;
		if( ( implementors != null ) && ( !implementors.isEmpty() ) ){
			boolean returnTypeIsVoid = method.getReturnType().equals( "void" );
			ClassMetaInfo cmi = implementors.get(0);
			List<MyConstructor> constructors = cmi.getConstructors();
			Class[] constructorExceptions = null;
			MyConstructor myConstructor = null;
			if( (constructors != null) && (  !constructors.isEmpty() ) ) {
				myConstructor = constructors.get(0);
				constructorExceptions = myConstructor.getExceptions();
			}
			MyMethod equivalentMethod = this.getEquivalentMethod( method , cmi );
			if( equivalentMethod != null){
				exceptions = equivalentMethod.getExceptions();
			}
			Class[] allExceptions = mergeExceptions(exceptions, constructorExceptions);
					
			//initializing the variables 
			List<Parameter> paramList = null;
			if( (constructors != null) && ( !constructors.isEmpty() ) ){
				
				methodBody += this.initializeConstructorParameters( myConstructor );
				paramList = myConstructor.getParameters();
			}
			methodBody += cmi.getName() + " implementor = null;\n";
			
			// set return type
			if ( !returnTypeIsVoid ) 
				methodBody += method.getReturnType() + " output = " + this.defaultValue( method.getReturnType() ) +";\n";
			if( ( allExceptions != null ) && ( allExceptions.length > 0) )
				methodBody += "\n\ttry{\n";
			
			// method call
			String aux = "implementor." + method.getName() + "(";
			int i = 0 ;
			
			List<Parameter> parameters = method.getParameters();
			Collections.sort(parameters);
			if( !parameters.isEmpty() ){
				for(i = 0; i < ( parameters.size() - 1 ) ; i++){
					Parameter p = parameters.get( i );
					aux += p.getName() + ",";
				}
				Parameter p = parameters.get(i);
				aux += p.getName();
			}
			aux += ");";
			
			// create the object
			if( !method.isStatic() ){
				if( ( paramList != null ) && ( !paramList.isEmpty() ) )
					methodBody += "implementor =  new " + cmi.getName() + "("+this.getConstructorParameters( paramList )+");\n";
				else
					methodBody += "implementor =  new " + cmi.getName() + "();\n";
			}
	
			//if the method returns void, it's not possible to return and call the method on the same time 
			if( !method.isStatic() ){
				if( !returnTypeIsVoid ){
					methodBody += "\t\t\toutput = " + aux;
				}
				else
					methodBody += "\t\t\t" + aux;
			}
			else{
				String className = cmi.getName() ;
				String staticCall  = null;
				if( className != null ){
					String patt = new String("implementor");
					int posBegin = aux.indexOf( patt );
					if( posBegin >= 0 ){
						int posEnd = posBegin + patt.length();
						staticCall = replace( className, aux, posBegin, posEnd );
					}
					
				}
				if( staticCall != null ){
					if( ( !returnTypeIsVoid )  ){
						methodBody += "\t\t\toutput = " + staticCall + ";";
					}
					else
						methodBody += "\t\t\t" + staticCall + ";";
				}
			}
			if( ( allExceptions != null ) && ( allExceptions.length > 0) ){
				methodBody += "\n}\n"; //close try{
				//Class[] exceptions = method.getExceptions();
				for(int e = 0; e < allExceptions.length; e++){
					Class exception = allExceptions[e];
					methodBody += "\ncatch("+ exception.getName()+" exception){\nexception.printStackTrace();\n}\n";
				}
			}
			if( !returnTypeIsVoid )
				methodBody += "\nreturn output;\n";
			else
				methodBody += "\nreturn;\n";
			
			
			
			return methodBody;
		}
		return null;
	}
	
	protected String replace(String replacement,String oldString,int begin,int end){
		String newStr = oldString.substring(0, begin);
		newStr = newStr.concat(replacement);
		/*if(begin<end)
			end--;
			*/
		newStr = newStr.concat(oldString.substring(end, oldString.length()));
		return newStr;
	}
	
	/**
	 * this method returns a MyMethod when it finds a equivalent method in an class that implements
	 * interfaces methods
	 * @param interfaceMethod
	 * @param cmi
	 * @return
	 */
	private MyMethod getEquivalentMethod(MyMethod interfaceMethod, ClassMetaInfo cmi){
		List<MyMethod> classMethods = cmi.getMethods();
		int i = 0;
		while( i < classMethods.size() ){
			MyMethod classMethod = classMethods.get(i);
			if( classMethod.equals( interfaceMethod ) )
				return classMethod;
			i++;
		}
		return null;
	}
	
	private boolean isSubClass(Class child, Class parent){
		Class childParent = child.getSuperclass();
		if( childParent.getCanonicalName().equals( parent.getCanonicalName() ) )
			return true;
		else
			return false;
	}
	
	private Class[] mergeExceptions(Class[] exc1, Class[] exc2){
		if( (exc1 == null) && ( exc2 == null) )
			return null;
		else{
			if( exc1 == null )
				return exc2;
			else{
				if( exc2 == null )
					return exc1;
				else{
					List<Class> classes = new ArrayList<Class>();
					for(int i = 0 ; i < exc1.length ; i++){
					/*	allExceptions[ i ] = exc1[ i ];*/
						classes.add(exc1[i]);
					}
					for(int j = 0 ; j < exc2.length ; j++){
						classes.add(exc2[j]);
						//classes.put( exc2[ j ].getCanonicalName() , exc2[ j ] );
						
					}
					for(int k = 0 ; k < classes.size() ; k++){
						for(int l = 0; l < classes.size(); l++){
							if( k != l ){
								if( this.isSubClass(classes.get(k), classes.get(l)) )
									classes.remove( k );
								
								}
							}
					}
					Class[] allExceptions = new Class[ classes.size() ] ;
					allExceptions = classes.toArray(allExceptions);
							
					return allExceptions;		
				}
				
			
			}
		}
	}
	
	private String defaultValue(String returnType){
		
		if(returnType.equals("int") )
			return "-1";
		else{
			if(returnType.equals("boolean"))
				return "false";
			else{
				if(returnType.equals("char"))
					return "' '";
				else{
					if(returnType.equals("short"))
						return "-1";
					else{
						if(returnType.equals("byte"))
							return "0";
						else{
							if(returnType.equals("long"))
								return "-1";
							else{
								if(returnType.equals("float"))
									return "-1";
								else{
									if(returnType.equals("double"))
										return "-1";
									else{
										return "null";
									}	
								}	
							}	
						}	
					}
				}
			}
		}
	}
	
	
	/*private String createMethodBody(MyMethod method, InterfaceMetaInfo imi, boolean returnType){
		
		String methodBody = new String("");
		List<ClassMetaInfo> cmiList = imi.getImplementedBy();
		if( ( cmiList != null ) && ( !cmiList.isEmpty() ) ){
			ClassMetaInfo cmi = cmiList.get(0);
			List<MyConstructor> constructors = cmi.getConstructors();
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
					
					methodBody += imi.getInterfaceName() +" implementor = new " + cmi.getName() + "(";
					methodBody += this.createParametersWithoutType(method);
					methodBody += ");\n";
					
					
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
				
				methodBody += imi.getInterfaceName() +" implementor = new " + cmi.getName() + "();\n";
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
		else
			return null;
		
	}*/
		
			
	
	
	private String createMethods(InterfaceMetaInfo imi){
		List<MyMethod> methods = imi.getMethods();
		String methodStr = new String("");

		for(int i = 0 ; i < methods.size() ; i++){
			MyMethod method = methods.get(i);
			String interfaceName = getClassLastName(imi.getInterfaceName());
			
			//Facade doesn't have static methods
			//if( !method.isStatic() ){
				
			methodStr += "\n\n\t" + method.getVisibility() +" " + method.getReturnType() + " " + method.getName() + "(" +
			this.createParameters( method ) + "){\n\t" + this.createMethodBody( method , "Facade_"+interfaceName  , imi ) + "\n\t}\n";
			//}
		}
		return methodStr;
	}
	
	private String getClassLastName(String qualifiedName){
		if( qualifiedName == null)
			return null;
		else{
			int lastPoint = qualifiedName.lastIndexOf('.');
			if( lastPoint < ( qualifiedName.length() - 1 )){
				lastPoint++;
				String lastName = qualifiedName.substring(lastPoint, qualifiedName.length());
				return lastName;
			}
			else
				return qualifiedName;
		}
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
		if( ( paramList != null) && ( !paramList.isEmpty() ) ){
			String initialization = new String("");
			for(int i = 0 ; i < paramList.size() ; i++){
				Parameter param = paramList.get( i );
				String paramType = param.getType();
				initialization += param.getType() + " " + param.getName() + " = "+defaultValue(paramType)+";\n";
			}
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
