package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import adapter.impl.jarFiles.ClassMetaInfo;

public class CreateFacade extends CreateFile {

	private final String FACADEPATH = "./src/java/adapter/impl/templates/Facade.java";
	
	public boolean createFacade(File file,File newComponentFolder,String newPackage,List<String> classpath){
		if(  ( file.getAbsolutePath().endsWith(".jar") ) && ( classpath != null ) )
			return this.createFacadeUsingJar(file, newComponentFolder, newPackage, classpath);
		else
			if( file.getAbsolutePath().endsWith(".xml")  )
				return this.createFacadeUsingMetadata(file, newComponentFolder, newPackage);
		return false;
	}
	
	
	
	
	private boolean createFacadeUsingJar(File jarFile,File newComponentFolder,String newPackage,List<String> classpath){
		CreateFacadeUsingJar cfuj = new CreateFacadeUsingJar();
		boolean result = cfuj.createFacadeUsingJar(jarFile, newComponentFolder, newPackage,classpath);
		return result;
	}
	
	private boolean createFacadeUsingMetadata(File metadata,File newComponentFolder,String newPackage){
		List<InterfaceMetaInfo> implementedInterfaces = new ArrayList<InterfaceMetaInfo>();
		try {
			DOMReader dr = new DOMReader();
			Node root = dr.readFiles(metadata);
			//this.searchImplement(root, "implement", implementedInterfaces);
			
			// look for implemented interfaces
			this.getInterfaces(root, "implement", implementedInterfaces);
			
			//teste
			/*System.out.println("number of implemented interfaces:"+implementedInterfaces.size());
			for(int k=0;k<implementedInterfaces.size();k++){
				InterfaceMetaInfo imi = implementedInterfaces.get(k);
				List<String> impls =  imi.getImplementedBy();
				for(int j=0;j<impls.size();j++)
					System.out.println(impls.get(j));
			}*/
			
			List<InterfaceMetaInfo> programmedInterfaces = new ArrayList<InterfaceMetaInfo>();
			this.searchInterfaces(root, "interface", programmedInterfaces);
			
			this.match(programmedInterfaces, implementedInterfaces);
			
			boolean res = true;
			Collections.sort(programmedInterfaces);
			for(int j=0;j<programmedInterfaces.size();j++){
				//create the Facade path
				InterfaceMetaInfo imi = programmedInterfaces.get(j);
				String canonicalPath = newComponentFolder.getCanonicalPath();
				canonicalPath += "/impl/Facade_"+imi.getInterfaceName()+".java";
				String facadeName = "Facade_"+imi.getInterfaceName();
				//System.out.println("Facade["+j+"]="+canonicalPath);
				File implPath = new File(canonicalPath);
				
				//delete in case it already exists
				implPath.delete();
				
				//create a brand new one
				boolean facadeIsCreated = implPath.createNewFile();
				if( ( implPath.exists() ) && ( facadeIsCreated ) ){
					//System.out.println("existe "+implPath.getAbsolutePath());
					File facade = new File(FACADEPATH);
					//String subPack = this.extractPackage(fullClassName)
					
					if( ( facade.exists() ) && ( imi.isImplemented() ) ) {
						//System.out.println("existe "+facade.getAbsolutePath());
						res = this.copy2(facadeName,facade, implPath, newPackage, imi);
					}
				}
			}
			return res;
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}
	
	private String extractPackage(String fullClassName){
		int lastSlash =  fullClassName.lastIndexOf(".");
		String pack = fullClassName.substring(0, lastSlash);
		return pack;
	}
	
	
	/**
	 * this method stores all interfaces that were implemented
	 * @param n current node
	 * @param element the tag element, in this case, always "implements"
	 * @param interfacees list of implemented interfaces
	 */
	private void getInterfaces(Node n,String element,List<InterfaceMetaInfo> interfacees){
		if(n == null)
			return;
		else{
			if(n.getNodeName().equalsIgnoreCase("implement")){
				InterfaceMetaInfo imi = new InterfaceMetaInfo();
				
				// provided interface name
				String name = this.getAttribute(n, "interface");
				if(name == null)
					System.err.println("Interface name is null");
				imi.setInterfaceName(name);
				
				//get the class that implements the interface
				Node parent = n.getParentNode();
				while( ( parent!=null ) && ( !parent.getNodeName().equalsIgnoreCase("class") ) )
					parent = parent.getPreviousSibling();
				
				// get the class name
				String implementorName;
				if( parent != null ){
					 implementorName= this.getAttribute(parent, "name");
				}
				else
					 implementorName = null;
				
				//add the class' package to the class name, so we have the full name
				while( ( parent != null ) && ( !parent.getNodeName().equalsIgnoreCase("package-decl")) )
					parent = parent.getPreviousSibling();
				if(parent != null){
					String packageName = this.getAttribute(parent, "name"); 
					String classs = packageName + "." + implementorName;
					ClassMetaInfo cmi = new ClassMetaInfo();
					cmi.setName( classs );
					imi.addImplementedBy( cmi );
				}
				interfacees.add(imi);
			}
			getInterfaces(n.getFirstChild(),element,interfacees);
			getInterfaces(n.getNextSibling(),element,interfacees);
		}
	}
	
	
	/**
	 * search for a interface that is implemented and get its informations
	 * @param n
	 * @param element
	 * @param interfaces
	 */
	private void searchInterfaces(Node n,String element,List<InterfaceMetaInfo> interfaces){
		if(n==null)
			return;
		else{
			//teste
			//System.out.println("[searchInterfaces]node = "+n.getNodeName());
			
			//check if the node is an interface
			if( ( n.getNodeName().equalsIgnoreCase(element) ) && ( n.hasChildNodes() ) ){

				
				//find if this interface is on the list
				String interfaceName = this.getAttribute(n, "name");
				//InterfaceMetaInfo imi = this.getInterfaceInfo(interfaces, interfaceName);
				InterfaceMetaInfo imi = new InterfaceMetaInfo();
				if(imi != null){
					imi.setInterfaceName(interfaceName);
					
					String visilibility = this.getAttribute(n, "visibility");
					imi.setVisibility( visilibility );
					
					Node child = n.getFirstChild();
					while( (child != null ) && ( !child.getNodeName().equalsIgnoreCase("method") ) ){
						//System.out.println("[searchInterfaces] chid name = "+child.getNodeName());
						child = child.getNextSibling();
					}
					List<MyMethod> methods = new ArrayList<MyMethod>();
					while(child!=null){
						//System.out.println("[searchInterfaces] 2while:chid name = "+child.getNodeName());
						if(child.getNodeName().equalsIgnoreCase("method")){
							MyMethod method = this.getMethod(child, interfaces);
							System.out.println("method "+method.getName());
							methods.add(method);
						}
						child = child.getNextSibling();
					}
					imi.setMethods(methods);
				}
				//add new interface
				interfaces.add(imi);
			}
			//printInterface(interfaces);
			this.searchInterfaces(n.getFirstChild(), element, interfaces);
			this.searchInterfaces(n.getNextSibling(), element, interfaces);
		}
	}
	
	
	
	private MyMethod getMethod(Node n,List<InterfaceMetaInfo> interfaces){
		MyMethod method = new MyMethod();
		method.setName(this.getAttribute(n, "name"));
		method.setVisibility(this.getAttribute(n, "visibility"));
		Node child = n.getFirstChild();
		while((child!=null)&&(!child.getNodeName().equalsIgnoreCase("type"))){
			//System.out.println("[getmethod] chid name = "+child.getNodeName());
			child = child.getNextSibling();
		}
		if(child!=null){
			method.setReturnType(this.getAttribute(child, "name"));
		}
		
		//find the tag <formal-arguments>
		while((child!=null)&&(!child.getNodeName().equalsIgnoreCase("formal-arguments"))){
			//System.out.println("[getmethod] chid name = "+child.getNodeName());
			child = child.getNextSibling();
		}
		
		//if <formal-arguments> was found
		if((child!=null)&&(child.hasChildNodes())){
			Node grandchild = child.getFirstChild();
			//find the tag <formal-argument>
			while((grandchild!=null)&&(!grandchild.getNodeName().equalsIgnoreCase("formal-argument")))
				grandchild = grandchild.getNextSibling();
			int i=0;
			while(grandchild!=null){
				if(grandchild.getNodeName().equalsIgnoreCase("formal-argument")){
					Parameter parameter = this.getParameter(grandchild,i);
					method.addParameter(parameter);
					i++;
				}
				//System.out.println("[getmethod] grandchid name = "+grandchild.getNodeName());
				grandchild = grandchild.getNextSibling();
			}
		}
			
		return method;
	}
	
	
	private Parameter getParameter(Node n,int pos){
		if(n==null)
			return null;
		else{
			System.out.println("[getparameter] n name = "+n.getNodeName());
			Parameter parameter = new Parameter();
			parameter.setName(this.getAttribute(n, "name"));
			parameter.setPosition(pos);
			Node child = n.getFirstChild();
			System.out.println("node = "+child.getNodeName());
			while(!child.getNodeName().equalsIgnoreCase("type")){
				System.out.println("node = "+child.getNodeName());
				child = child.getNextSibling();
			}
			parameter.setType(this.getAttribute(child, "name"));
			return parameter;
		}
	}
	
	private InterfaceMetaInfo getInterfaceInfo(List<InterfaceMetaInfo> list,String name){
		for(int i=0;i<list.size();i++){
			InterfaceMetaInfo imi = list.get(i);
			String aux = imi.getInterfaceName();
			if(name.equals(aux))
				return imi;
		}
		return null;
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
	
	
	protected String changeFacade(String facadeName,String file, String newPackage,InterfaceMetaInfo interfacee){
		Util util = new Util();
		
		//change the package, StringPosition save the position of the string that will be replaced
		StringPosition sp = util.readPackage(file);
		String packReplacement = "package "+newPackage+".impl;";
		if(sp != null)
			file = util.replace(packReplacement, file, sp.beginString, sp.endString);
		
		//change visibility
		sp = util.readClassVisibility(file);
		String visiReplacement = "";
		if(sp != null)
			file = util.replace(visiReplacement, file, sp.beginString, sp.endString);

		//change import
		ClassMetaInfo cmi = interfacee.getImplementedBy().get(0);
		String clazz = cmi.getName();
		if(clazz==null)
			System.err.println("Class musn't be null!");
		int point = clazz.lastIndexOf('.');
		point++;
		clazz = clazz.substring(point, clazz.length());
		String newImportClassLine = "import "+newPackage+".impl."+clazz+";\nimport "+newPackage+".spec.prov.*;\n";
		sp = util.readImport(file);
		if(sp != null)
			file = util.replace(newImportClassLine, file, sp.beginString, sp.endString);
		
		//change interface name from import
		String interf = interfacee.getInterfaceName();
		String newImportInterfaceLine ;
		if(interfacee.getVisibility().equals("public"))
			newImportInterfaceLine = "import "+newPackage+".spec.prov."+interf+";";
		else
			newImportInterfaceLine = "import "+newPackage+".impl."+interf+";";
		sp = util.readInterfaceNameFromImport(file);
		if(sp != null)
			file = util.replace(newImportInterfaceLine, file, sp.beginString, sp.endString);
		
		//change interface name on implements and change the Facade name
		String interfImpl = interfacee.getInterfaceName();
		String newImplementsInterfaceLine = facadeName + " implements "+interfImpl;
		sp = this.readInterfaceNameImplements(file);
		if(sp != null)
			file = util.replace(newImplementsInterfaceLine, file, sp.beginString, sp.endString);
		
		//change method
		sp = util.readMethod(file);
		if(sp != null){
			List<MyMethod> methods = interfacee.getMethods();
			String newS = new String();
			for(int k=0;k<methods.size();k++)
				newS += createMethod(interfacee,k);
			file = util.replace(newS, file, sp.beginString, sp.endString);
		}
		
		return file;
	}
	
	protected boolean copy2(String facadeName,File source, File destination, String newPackage,InterfaceMetaInfo interfacee){
		//if both source and destination file exists
		if(source.exists()&&destination.exists()){
			//System.out.println("copying the files...");
			try {
				
				
				//infra-structure classes
				//Util util = new Util();
				BufferedReader in =     new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				
				
				long fileSizeLong = source.length();
				int fileSize = Integer.parseInt(String.valueOf(fileSizeLong));
				char[] cbuf = new char[fileSize];
				in.read(cbuf, 0, fileSize);
				String file = String.valueOf(cbuf);
				
				String facadeStr = this.changeFacade(facadeName,file, newPackage, interfacee);
				
			
				
				out.write(facadeStr);
				//close both files
				in.close();
				out.close();
			      
				      
				return true;
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return false;
		}
		else{
			if(!source.exists())
				System.out.println("Source file does not exists");
			if(!destination.exists())
				System.out.println("destination file does not exists");
			return false;
		}
	}
	
	
	/**
	 * this method check if some templates appears, and when this happens the method replace the 
	 * pattern with a informations of new component that is being built
	 * @param s the line read from the template
	 * @param interfacee informations about the interface that is being implemented
	 * @param newPackage the new package
	 * @param eof it only change its value when the methods will be created. This variable defines the
	 * end of the file, in this case, Facade*.java. So, everything above the 's' line will be deleted.
	 * @return
	 */
	
	
	/**
	 * 
	 * @param parameters parameters list of a method 
	 * @return a string composed by the name of the parameters name. The string should be equal as the list 
	 * of parameters when a method is declared
	 */
	private String getParameterNameAndType(List<Parameter> parameters){
		String paramstr = new String();
		Parameter param;
		int tamParam = parameters.size();
		if(tamParam > 0){
			tamParam--;
			int i;
			for(i=0;i<tamParam;i++){
				param = parameters.get(i);
				paramstr+= param.getType()+" "+param.getName()+",";
			}
			param = parameters.get(i);
			paramstr+= param.getType()+" "+param.getName();
			return paramstr;
		}
		else
			return "";
	}
	/**
	 * 
	 * @param parameters parameters list of a method 
	 * @return a string composed by the name of the parameters name. The string should be equal as the list 
	 * of parameters when a method is called
	 */
	private String getParameterName(List<Parameter> parameters){
		
		String paramstr = new String();
		Parameter param;
		int tamParam = parameters.size();
		if(tamParam > 0){
			tamParam--;
			int i;
			for(i=0;i<tamParam;i++){
				param = parameters.get(i);
				paramstr+= param.getName()+",";
			}
			param = parameters.get(i);
			paramstr+= param.getName();
			return paramstr;
		}
		else
			return "";
	}
	
	/**
	 * 
	 * create a method using the informations from 'interinfo'
	 * 
	 * @param interinfo information about the interface and its methods and fields
	 * @param index the interface index on the interface list that was created reading the metadata file
	 * @return the string that will replace the template string 
	 */
	private String createMethod(InterfaceMetaInfo interinfo,int index){
		System.out.println("Creating the facade that implements "+interinfo.getInterfaceName());
		List<MyMethod> methods = interinfo.getMethods();
		MyMethod method = methods.get(index);
		System.out.println(method.getName()+" is being created");
		List<Parameter> parameters = method.getParameters();
		String paramstr = this.getParameterNameAndType(parameters);
		String paramNames = this.getParameterName(parameters);
		List<ClassMetaInfo> implementorsList = interinfo.getImplementedBy();
		String classImplements = null;
		if( ( implementorsList != null ) && (!implementorsList.isEmpty() ) ){
			ClassMetaInfo cmi = implementorsList.get(0);
			classImplements = cmi.getName();
			int point = classImplements.lastIndexOf('.');
			point++;
			classImplements = classImplements.substring(point, classImplements.length());
		}
		if(classImplements == null)
			System.err.println("There is no class that implements "+interinfo.getInterfaceName());
		String result =  "\n\npublic "+method.getReturnType()+" "+method.getName()+" " +
		"("+paramstr+") {\n	"+interinfo.getInterfaceName()+" c = new "+classImplements+"();\n";
		
		
		//add the return part
		if(method.getReturnType().equals("void"))
			result += "\nreturn;\n}\n";
		else
			result += "\nreturn c."+method.getName()+"("+paramNames+");\n}\n"; 
				
		
		/*String result =  method.getVisibility()+" "+method.getReturnType()+" "+method.getName()+" " +
				"("+paramstr+") {\n	"+classImplements+" c = new "+classImplements+"();\n" +
				" c."+method.getName()+"("+paramNames+");\n}";*/
		return result;	
		
		
	}
	
	protected StringPosition readInterfaceNameImplements(String file){
		String pattern = new String("Facade implements IExportToCosmos");
		int implementPos = file.indexOf(pattern);
		if(implementPos>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = implementPos;
			sp.endString = implementPos + pattern.length();
			return sp;		
		}
		else
			return null;
	}

	/**
	 * Check if all implemented interfaces were programmed or were a dependency from other
	 * java packages
	 * @param nonNative list of programmed interfaces
	 * @param implemented
	 * @return
	 */
	private void match(List<InterfaceMetaInfo> programmedInterfaces,List<InterfaceMetaInfo> implemented){
		
		boolean res[] = new boolean[implemented.size()];
		for(int i=0;i<implemented.size();i++){
			InterfaceMetaInfo implInterface = implemented.get(i);
			int j = 0;
			boolean found = false;
			while( (!found) && ( j < programmedInterfaces.size()) ){
				InterfaceMetaInfo progInterface = programmedInterfaces.get(j);
				//compare the name of both interfaces
				if( implInterface.getInterfaceName().equals(progInterface.getInterfaceName())){
					progInterface.setImplementedBy(implInterface.getImplementedBy());
					//progInterface.setMethods(implInterface.getMethods());
					progInterface.setFields(implInterface.getFields());
					found = true;
				}
				else
					j++;
			}
			res[i] = found;
		}
	
		
	}
	
}
