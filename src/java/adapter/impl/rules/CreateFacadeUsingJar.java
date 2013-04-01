
package adapter.impl.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adapter.impl.jarFiles.ClassMetaInfo;
import adapter.impl.jarFiles.Extractor;

public class CreateFacadeUsingJar {
	private String IDENTATION = "   ";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private List<ClassMetaInfo> isImplemented(InterfaceMetaInfo imi, List<ClassMetaInfo> classes ){

		List<ClassMetaInfo> implementors = new ArrayList<ClassMetaInfo>();
		for(int i = 0;i<classes.size();i++){
			ClassMetaInfo cmi = classes.get(i);
			List<String> implementedInterfaces = cmi.getImplementedInterfaces();
			for(int j=0;j<implementedInterfaces.size();j++){
				String interfacestr = implementedInterfaces.get(j);
				if( interfacestr.equals(imi.getInterfaceName()) ){
					implementors.add( cmi );
					imi.setConstructors( cmi.getConstructors() );
				}
			}
		}
		return implementors;
	}

	/**
	 * this method initializes the arguments of a specified constructor
	 * @param constructor 
	 * @return
	 */
	private String initializeConstructorParameters(MyConstructor constructor){
		List<Parameter> paramList = constructor.getParameters();
		if( ( paramList != null) && ( !paramList.isEmpty()) ){
			String initialization = new String("");
			int secondLast = paramList.size() -1;
			int i = 0 ;
			for(i = 0 ; i < secondLast ; i++){
				Parameter param = paramList.get( i );
				String paramType = param.getType();
				if( paramType.equals( "boolean" ) )
					initialization += param.getType() + " " + param.getName() + " = false;\n";
				else
					initialization += param.getType() + " " + param.getName() + " = null;\n";

			}
			Parameter param = paramList.get( i );
			String paramType = param.getType();
			if( paramType.equals( "boolean" ) )
				initialization += param.getType() + " " + param.getName() + " = false;\n";
			else
				initialization += param.getType() + " " + param.getName() + " = null;\n";


			return initialization;
		}
		else
			return "";
	}

	private List<InterfaceMetaInfo> getProvidedInterfaces(File jarFile, URLClassLoader urlClassLoader){
		Extractor extractor = new Extractor();
		List<InterfaceMetaInfo> provInterfaces = new ArrayList<InterfaceMetaInfo>();
		List<InterfaceMetaInfo> interfaces = extractor.getInterfacesMetaInfo(jarFile,urlClassLoader);
		for(int i=0;i<interfaces.size();i++){
			InterfaceMetaInfo imi = interfaces.get(i);
			List<ClassMetaInfo> classes = extractor.getClassesMetaInfo(jarFile,urlClassLoader);
			//maybe it is not necessary to get the classmetainfo...
			List<ClassMetaInfo> implementors  = this.isImplemented(imi, classes);
			//System.out.println("class "+implementor.getName()+" implements "+imi.getInterfaceName());

			if( !implementors.isEmpty() ){
				imi.setImplementedBy(implementors);
				provInterfaces.add( imi );
			}

		}
		return provInterfaces;
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
					int size = exc1.length + exc2.length;
					Class[] allExceptions = new Class[size];
					for(int i = 0 ; i < exc1.length ; i++)
						allExceptions[ i ] = exc1[ i ];
					for(int j = 0 ; j < exc2.length ; j++)
						allExceptions[ exc1.length + j ] = exc2[ j ];
					return allExceptions;
				}
			}
		}
	}

	private String defaultValue(String returnType){
		if(returnType.equals("int"))
			return "-1";
		else{
			if(returnType.equals("boolean"))
				return "false";
			else{
				if(returnType.equals("char"))
					return "";
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

	private String createMethodBody(MyMethod method, String facadeName, InterfaceMetaInfo imi){
		List<Parameter> parameters = method.getParameters();


		//Collections.sort(parameters);
		String methodBody = new String("");

		// set constructors
		List<ClassMetaInfo> implementors = imi.getImplementedBy();
		List<MyConstructor> constructors = null;
		Class[] exceptions = null;
		if( ( implementors != null ) && ( !implementors.isEmpty() ) ){
			boolean returnTypeIsVoid = method.getReturnType().equals( "void" );
			ClassMetaInfo cmi = implementors.get(0);
			constructors = cmi.getConstructors();
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

			methodBody += imi.getInterfaceName() + " implementor = null;\n";

			if ( !returnTypeIsVoid ) 
				methodBody += method.getReturnType() + " output = " + this.defaultValue( method.getReturnType() ) +";";
			if( ( allExceptions != null ) && ( allExceptions.length > 0) )
				methodBody += "\ntry{\n";

			// method call
			String aux = "implementor." + method.getName() + "(";
			int i = 0 ;
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
			if( ( paramList != null ) && ( !paramList.isEmpty() ) )
				methodBody += "implementor = new " + cmi.getName() + "("+this.getConstructorParameters(paramList)+");\n";
			else
				methodBody += "implementor = new " + cmi.getName() + "();\n";


			//if the method returns void, it's not possible to return and call the method on the same time 
			if( !returnTypeIsVoid ){
				methodBody += "output = " + aux;
			}


			if( ( allExceptions != null ) && ( allExceptions.length > 0) ){
				methodBody += "}"; //close try{
				//Class[] exceptions = method.getExceptions();
				for(int e = 0; e < allExceptions.length; e++){
					Class exception = allExceptions[e];
					methodBody += " catch("+ exception.getName()+" exception){\nexception.printStackTrace();\n}\n";
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

	private String createMethods(InterfaceMetaInfo imi, String facadeName,File newComponentFolder){
		List<MyMethod> methods = imi.getMethods();
		String methodStr = new String("");
		for(int i=0;i<methods.size();i++){
			MyMethod method = methods.get(i);
			methodStr += method.getVisibility()+" "+method.getReturnType()+" "+method.getName()+"("+
			this.createParameters(method)+"){\n"+this.createMethodBody(method, facadeName, imi)+"\n}\n";
		}
		return methodStr;
	}


	private String getOnlyLastName(String qualifiedName){
		if( qualifiedName == null)
			return null;
		else{
			int lastPoint = qualifiedName.lastIndexOf('.');
			if( lastPoint < ( qualifiedName.length() - 1 ))
				lastPoint++;
			else
				return null;
			String name = qualifiedName.substring(lastPoint, qualifiedName.length());
			return name;
		}
	}	

	/**
	 * this file get all the information from the interface, package and the facade name, 
	 * and put it all together in a stringbuilder. This stringbuilder is the new facade.
	 * @param imi meta-information about the interface
	 * @param pack the package name
	 * @param facadeName the name of the facade
	 * @param newComponentFolder
	 * @return
	 */
	private StringBuilder facadeCreator(InterfaceMetaInfo imi, String pack,String facadeName,
			File baseDir){
		String interfaceName = pack + ".spec.prov." + this.getOnlyLastName( imi.getInterfaceName() );
		StringBuilder sb = new StringBuilder("\npackage "+ pack +".impl;\n" +
				"import "+pack+".spec.prov.*;\n" + this.IDENTATION+
				"class "+facadeName+" implements "+ interfaceName +"{\n"+ this.IDENTATION+this.IDENTATION+
				this.createMethods(imi, facadeName, baseDir)+
				"}\n"

		);
		return sb;
	}


	private boolean createFacadeFile(StringBuilder sb,File newComponentFolder,String facadeName){

		try {
			File destination = new File(newComponentFolder + File.separator + "impl" + File.separator +facadeName+".java");
			destination.createNewFile();
			System.out.println(" destination exists "+ destination.exists() );
			if( destination.exists()){
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				String str = new String(sb);
				out.write(str);
				out.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * this method starts the creation of all Facades
	 * @param jarFile a java.io.File that represents the file that will be converted to COSMOS
	 * @param newComponentFolder the base directory where all the packages will be placed
	 * @param newPackage the package of the new component 
	 * @param classpath the classpath necessary to load the jar file
	 * @return true if the Facade was created and false otherwise
	 */
	protected boolean createFacadeUsingJar(File jarFile,File baseDir,String newPackage,List<String> classpath){
		boolean result = false;
		System.out.println("createFacadeUsingJar...");
		
		//the top classloader
		DeadEndClassLoader endClassLoader = new DeadEndClassLoader();

		//put all the files and directories into the array URL
		URL[] locations = null;
		try {
			locations = new URL[ ( classpath.size() + 1 ) ];
			int k = 0 ;
			for( k = 0; k < classpath.size() ; k++ ){
				File file = new File(classpath.get(k));
				if( file.exists() ){
					URL location = null;
					location = file.toURL();
					locations[k] = location;
				}
				else
					System.err.println("Error on classpath:"+file.getAbsolutePath()+" does not exist");
			}
			locations[k] = jarFile.toURL();

			// create a classloader to load the classpath
			URLClassLoader urlClassLoader = new URLClassLoader(locations, endClassLoader);

			//jar classloader will load the jar file
			//JarClassLoader jarClassLoader = new JarClassLoader( jarFile.toURL() );

			//get a list of all interfaces that were implemented
			List<InterfaceMetaInfo> provInterfaces = this.getProvidedInterfaces( jarFile , urlClassLoader);
			Collections.sort(provInterfaces);

			//for each interface, create a facade
			for(int i = 0 ; i < provInterfaces.size() ; i++){
				System.out.println(i+" result = "+ result);
				InterfaceMetaInfo imi = provInterfaces.get(i);
				StringBuilder sb = this.facadeCreator( imi, newPackage, "Facade"+i , baseDir );
				result = this.createFacadeFile(sb, baseDir , "Facade"+i );
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}


		return result;
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



}
