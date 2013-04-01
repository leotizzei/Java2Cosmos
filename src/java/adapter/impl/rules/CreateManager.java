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

import org.w3c.dom.Node;

public class CreateManager extends CreateFile {
	private final int REQUIRED = 0;
	private final int PROVIDED = 1;
	private final int CHAR_SIZE = 16;
	
	private String INSTANTIATIONPATTERN = "/*public interface is instanciated with the Facade*/";
	private String PROVIDEDINTERFACESPATTERN = "/*interfaces are set as provided*/";
	private String REQUIREDINTERFACESPATTERN = "/*interfaces are set as required*/";
	private String REGISTRATIONLISTPATTERN = "/*create a list with interface names and set as provided interfaces */";
	private final String MANAGERPATH = "./src/java/adapter/impl/templates/Manager.java";
	
	public static void main(String args[]){
		CreateManager cm = new CreateManager();
		String param1 = "/home/lsd/ra001973/workspace2/adapter/temp/java2xmlJar/";
		String param2 = "/home/lsd/ra001973/workspace2/java2xml/java2xml_v4.jar";
		
		File metadata = new File("/home/lsd/ra001973/workspace2/adapter/output.xml");
		File jarFile = new File(param2);
		File newComponentFolder = new File(param1);
		String newPackage = new String("java2xmlJar");
		//cm.createManager(jarFile, newComponentFolder, newPackage,args);
		
		
		
		List<JavaInfo> provInterfaces = new ArrayList<JavaInfo>();
		List<JavaInfo> reqInterfaces = new ArrayList<JavaInfo>();
		//cm.managerCreator(provInterfaces, reqInterfaces);
	}
	
	
	/**
	 *  this method forward the message to the appropriate method. If the file is a jar then call 
	 *  'createManagerUsingJar'. Otherwise call 'createManagerUsingMetadata'.
	 * @param file could be either a jar or a xml file
	 * @param newComponentFolder the folder where the new component will be placed
	 * @param newPackage the package of the new component
	 * @param classpath the canonical path of all files and directories that must be on class path 
	 * @return true if the manager was created or false on the other hand
	 */
	public boolean createManager(File file, File newComponentFolder,String newPackage,List classpath){
		boolean result;
		if( file.getAbsolutePath().endsWith(".jar") ){
			System.out.println("extracting jar file...");
			result = this.createManagerUsingJar(file, newComponentFolder, newPackage,classpath);
			return result;
		}
		else{
			if( file.getAbsolutePath().endsWith(".xml") ){
				System.out.println("parsing xml metadata...");
				result =  this.createManagerUsingMetadata(file, newComponentFolder, newPackage);
				return result;
			}
			else{
				System.err.println("The file "+file.getAbsolutePath()+" is neither a XML nor a JAR");
				return false;
			}
		}
		
	}
	
	
	
	private JavaInfo copyIMetaInfoToJavaInfo(InterfaceMetaInfo imi){
		JavaInfo ji = new JavaInfo();
		if( imi != null ){
			ji.setAbsolutePath(imi.getInterfaceName());
			ji.setPackage(imi.getPackag());
			ji.setVisibility(imi.getVisibility());
			return ji;
		}
		else
			return null;
	}
	
	private void printList(List<JavaInfo> list){
		for(int i=0;i<list.size();i++){
			JavaInfo ji = list.get(i);
			System.out.println("absolutepath = "+ji.getAbsolutePath());
		}
	}
	
	
	
	
	
	

	
	private boolean createManagerUsingJar(File jarFile, File newComponentFolder,String newPackage,List<String> classpath){
	
		CreateManagerUsingJar cmuj = new CreateManagerUsingJar();
		return cmuj.createManagerUsingJar(jarFile, newComponentFolder, newPackage, classpath);
	}

	
	
	private boolean createManagerUsingMetadata(File metadata, File newComponentFolder,String newPackage){
		try {
			List<JavaInfo> provInterfaces = new ArrayList<JavaInfo>();
			List<JavaInfo> reqInterfaces = new ArrayList<JavaInfo>();
			List<JavaInfo> interfaceList = new ArrayList<JavaInfo>();
			DOMReader dr = new DOMReader();
			Node root = dr.readFiles(metadata);
			
			//get the interface list
			dr.fillJavaInfo(root, "interface", interfaceList);
			
			//get provided interfaces
			dr.getProvidedInterfaces(root, "implement",provInterfaces);
			
			//divide provided and required interfaces
			this.splitInterfaces(interfaceList, provInterfaces, reqInterfaces);
			
			String canonicalPath = newComponentFolder.getCanonicalPath();
			//verificar se já existe outro manager
			canonicalPath += "/impl/Manager.java";
			File implPath = new File( canonicalPath );
			boolean managerIsCreated = implPath.createNewFile();
			if( ( implPath.exists() ) && ( managerIsCreated ) ){
				//System.out.println("existe "+implPath.getAbsolutePath());
				File manager = new File(MANAGERPATH);
				if( manager.exists() ){
					//System.out.println("existe "+manager.getAbsolutePath());
					boolean res = this.copy2(manager, implPath, newPackage ,provInterfaces,reqInterfaces);
					return res;
				}
				else{
					System.err.println("The program can't find the Manager class");
				}
					
			}
			else{
				System.err.println("The class 'Manager' was not created");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	private String changeManager(String file, String newPackage,List<JavaInfo> provInterfaces,
			List<JavaInfo> reqInterfaces){
		Util util = new Util();
		String variableProvidedInterface = new String("providedInterface");
		String variableRequiredInterface = new String("requiredInterface");
		
		//change the package, StringPosition save the position of the string that will be replaced
		StringPosition sp = util.readPackage(file);
		String packReplacement = "package " + newPackage + ".impl;";
		if(sp != null)
			file = util.replace(packReplacement, file, sp.beginString, sp.endString);
		
		//add line 'import spec.prov.*;'
		/*sp = util.readImport(file);*/
		sp = util.findPattern("import java.util.HashMap;", file);
		String newImport = "import " + newPackage +  ".spec.prov.*; import " + newPackage +  ".spec.req.*;" +
				"\nimport java.util.*;\n";
		if(sp != null){
			newImport += file.substring(sp.beginString, sp.endString);
			file = util.replace(newImport, file , sp.beginString, sp.endString);
		}
			
		
		//change the parameters in setProvidedInterface() call
		if( !provInterfaces.isEmpty() ){
			sp = this.getSetMethods(this.PROVIDED, file);
			String providedReplacement = "" ;
			for(int i = 0;i<provInterfaces.size();i++ ){
				JavaInfo provInterfacee = provInterfaces.get(i);
				String aux = this.getOnlyInterfaceName( provInterfacee );
				if( aux != null )
					providedReplacement += "setProvidedInterface(\""+aux+"\","+variableProvidedInterface+i+");";
			}	
			if(sp != null)
				file = util.replace(providedReplacement, file, sp.beginString, sp.endString);
		}
		
		//change the parameters in setRequiredInterface() call
		if( !reqInterfaces.isEmpty() ){ 
			sp = this.getSetMethods(this.REQUIRED, file);
			String requiredReplacement = "" ;
			for(int j = 0;j<reqInterfaces.size();j++ ){
				JavaInfo reqInterface = reqInterfaces.get(j);
				String aux2 = this.getOnlyInterfaceName( reqInterface );
				if( aux2 != null )
					requiredReplacement += "setRequiredInterface(\""+aux2+"\","+variableRequiredInterface+j+");";
			}	
			if(sp != null)
				file = util.replace(requiredReplacement, file, sp.beginString, sp.endString);
		}
		
		//change facade atribution
		if( !provInterfaces.isEmpty()){
			sp = this.getFacadeInstantiation(file,this.PROVIDED);
			String instantiation = "String[] listProv = new String["+provInterfaces.size()+"];\n";
			Collections.sort(provInterfaces);
			for(int h=0;h<provInterfaces.size();h++){
				String interfaceName = this.getOnlyInterfaceName(provInterfaces.get(h));
				if(interfaceName!=null){
					instantiation += interfaceName + " "+ variableProvidedInterface + h + " = new Facade_" + interfaceName + "();\n" +
							"listProv["+h+"] = "+interfaceName+";\n";
				}
			}
			if(sp != null)
				file = util.replace(instantiation, file, sp.beginString, sp.endString);
		}
		if( !reqInterfaces.isEmpty() ){
			sp = this.getFacadeInstantiation(file,this.REQUIRED);
			String instantiation = "String[] listReq = new String["+reqInterfaces.size()+"];\n";
			Collections.sort(reqInterfaces);
			for(int g=0;g<reqInterfaces.size();g++){
				String interfaceName = this.getOnlyInterfaceName(reqInterfaces.get(g));
				if(interfaceName!=null){
					instantiation += interfaceName + " "+ variableRequiredInterface + g + " = null ;\n" +
							"listReq["+g+"] = "+interfaceName+";\n";
				}
			}
			if(sp != null)
				file = util.replace(instantiation, file, sp.beginString, sp.endString);
		}
		
		
		return file;
	}
	
	private StringPosition getSetMethods(int interfaceType,String file){
		String pattern;
		if(interfaceType == this.PROVIDED)
			pattern = this.PROVIDEDINTERFACESPATTERN;
		else
			pattern = this.REQUIREDINTERFACESPATTERN;
		int methodPos = file.indexOf(pattern) + pattern.length() + 1;
		/*int semicolon = file.indexOf(';', methodPos);
		semicolon++;
		if( (methodPos > 0) && (semicolon > 0) ){
			StringPosition sp = new StringPosition();
			sp.beginString = methodPos;
			sp.endString = semicolon;
			return sp;
		}*/
		if( methodPos > 0){
			StringPosition sp = new StringPosition();
			sp.beginString = ++methodPos;
			sp.endString = sp.beginString + 1;
			return sp;
		}
		else
			return null;
			
	}
	
	private StringPosition getFacadeInstantiation(String file , int interfaceType){
		String pattern = new String();
		if(interfaceType == this.PROVIDED)
			pattern = this.PROVIDEDINTERFACESPATTERN;
		else
			pattern = this.REQUIREDINTERFACESPATTERN;
		
		int position = file.indexOf(pattern);
		if( position >= 0){
			StringPosition sp = new StringPosition();
			sp.beginString = position + pattern.length();
			sp.endString = sp.beginString + 1;
			return sp;
		}
		else
			return null;
	}
	
	private int searchListRegistration(String managerStr, String pattern){
		int i = managerStr.indexOf(pattern);
		i += pattern.length();
		return i;
	}
	
	protected boolean copy2(File source, File destination, String newPackage,List<JavaInfo> provInterfaces,
			List<JavaInfo> reqInterfaces){
		//if both source and destination file exists
		if(source.exists()&&destination.exists()){
			System.out.println("copying the files...");
			try {
				
				//infra-structure classes
				//Util util = new Util();
				BufferedReader in =     new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				
				Collections.sort(reqInterfaces);
				Collections.sort(provInterfaces);
				long fileSizeLong = source.length();
				int fileSize = Integer.parseInt(String.valueOf(fileSizeLong));
				char[] cbuf = new char[fileSize];
				in.read(cbuf, 0, fileSize);
				String file = String.valueOf(cbuf);
				
				String newManager = this.changeManager(file, newPackage, provInterfaces, reqInterfaces);
				
				out.write(newManager);
			
				//close both files
				in.close();
				out.close();
			    return true;
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
	
	
	private void splitInterfaces(List<JavaInfo> interfaces,List<JavaInfo> prov,List<JavaInfo> req){
		boolean isRequired = true;
		for(int i=0;i<interfaces.size();i++){
			isRequired = true;
			for(int j=0;j<prov.size();j++){
				String aux = this.getOnlyInterfaceName(interfaces.get(i));
				//System.out.println(aux+" == "+prov.get(j));
				if(aux.equals(prov.get(j).getAbsolutePath())){
					//System.out.println("[split]interfaces["+i+"]="+interfaces.get(i).getAbsolutePath());
					//System.out.println("[split]prov["+j+"]="+prov.get(j).getAbsolutePath());
					prov.remove(j);
					prov.add(interfaces.get(i));
					isRequired = false;
					
				}
			}
			if(isRequired)
				req.add(interfaces.get(i));
		}
	}
	
	/*private boolean checkFile(String str){
		File f = new File(str);
		return f.exists();
	}*/
	
	
	
	protected String changeParameter(String s,List<JavaInfo> interfaces,int pos,int TYPE){
		boolean isMethod = false;
		String newLine = new String();
		int semicolon = s.indexOf(";",pos);
		semicolon++;
		int string = s.indexOf("(String", pos);
		if(string>=0)
			isMethod = true;
		String result;
		if(!isMethod){
			Collections.sort(interfaces);
			for(int i=0;i<interfaces.size();i++){
				
				String aux = this.getOnlyInterfaceName(interfaces.get(i));
				if(aux!=null)
					if(TYPE==this.PROVIDED)
						newLine += "setProvidedInterface(\""+aux+"\",providedInterface"+i+");";
					else
						newLine += "setRequiredInterface(\""+aux+"\",requiredInterface"+i+");";
			}
			
		
			String replaced = s.substring(pos, semicolon);
			result = Util.replace(s, replaced, newLine);
		
		}
		else
			result = s;
		return result;
	}
	
	
	private String getOnlyInterfaceName(JavaInfo interfaceInfo){
		String path = interfaceInfo.getAbsolutePath();
		int posslash = path.lastIndexOf("/");
		if(posslash == path.length())
			posslash = path.lastIndexOf("/", (posslash-1));
		if(posslash<0)
			posslash = 0;
		else
			posslash++;
		int point = path.lastIndexOf(".");
		
		System.out.println(path+" "+posslash+" "+point);
		String res;
		if(point>posslash)
			res = path.substring(posslash, point);
		else
			res = null;
		return res;
	}
	
	
	private String changeInterface(String s,List<JavaInfo> interfaces){
		String result = new String();
		Collections.sort(interfaces);
		for(int i=0;i<interfaces.size();i++){
			String interfaceName = this.getOnlyInterfaceName(interfaces.get(i));
			if(interfaceName!=null){
				interfaceName += "  providedInterface"+i;
				result += Util.replace(s, "IExportToCosmos icc ", interfaceName);
			}
		}
		return result;
	}
		
}
