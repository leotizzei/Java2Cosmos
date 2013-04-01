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
import adapter.impl.jarFiles.JarExtractor;

public class CreateManagerUsingJar {

	
	protected boolean createManagerUsingJar(File jarFile, File newComponentFolder,String newPackage,List<String> classpath){
		JarExtractor jarExtractor = new JarExtractor();
		
		
		URL[] locations = null;
		try {
			locations = new URL[ ( classpath.size() + 1 ) ];
			int k = 0;
			for( k = 0 ; k < classpath.size() ; k++ ){
				File file = new File( classpath.get( k ) );
				if( file.exists() ){
					URL location = null;
					location = file.toURL();
					locations[ k ] = location;
				}
				else
					System.err.println("Error on classpath:" + file.getAbsolutePath() + " does not exist");
			}
			locations[k] = jarFile.toURL();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		//urlClassLoader loads the files in classpath
		DeadEndClassLoader endClassLoader = new DeadEndClassLoader();
		URLClassLoader urlClassLoader = new URLClassLoader(locations, endClassLoader);
		
		
		
		//List<InterfaceMetaInfo> interfaces = jarExtractor.getInterfacesMetaInfo(jarFile,urlClassLoader);
		List<ClassMetaInfo> classes = jarExtractor.getClassesMetaInfo(jarFile, urlClassLoader);
		
		List<InterfaceMetaInfo> provInterfaces = new ArrayList<InterfaceMetaInfo>();
		List<InterfaceMetaInfo> reqInterfaces = new ArrayList<InterfaceMetaInfo>();
		for(int i=0 ; i <classes.size() ; i++ ){
			
			//maybe it is not necessary to get the classmetainfo...
			ClassMetaInfo cmi = classes.get( i );
			//get only public classes
			if( ( cmi.getVisibility() != null ) && ( cmi.getVisibility().equals( "public" ) ) && ( !cmi.isAbstract() ) ) {
				InterfaceMetaInfo imi = new InterfaceMetaInfo();
				
				String aux = cmi.getName();
				//the interface name corresponds to its whole name
				String interfaceName =  aux.replace('.', '_') ;
				imi.setInterfaceName( "I" + interfaceName );
				imi.setConstructors( cmi.getConstructors() );
				imi.setMethods( cmi.getMethods() );
				List<ClassMetaInfo> implementor = new ArrayList<ClassMetaInfo>();
				implementor.add( cmi );
				imi.setImplementedBy( implementor );
				imi.setVisibility( "public" );
				provInterfaces.add( imi );
			}
			
		}
	
		StringBuilder sb = this.managerCreator( newPackage, provInterfaces, reqInterfaces);
		return this.createManagerFile(sb, newComponentFolder);
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
	
	/**
	 * this method get a template of a class Manager and fill the blanks with information
	 * of the new component
	 * @param newPackage represent the package of the new component (only the initial package)
	 * @param provInterfaces a list of provided interfaces
	 * @param reqInterfaces a list of required interfaces
	 * @return
	 */
	private StringBuilder managerCreator(String newPackage, List<InterfaceMetaInfo> provInterfaces,
			List<InterfaceMetaInfo> reqInterfaces){
		
		StringBuilder managerFile = new StringBuilder("package "+newPackage+".impl;\n" +
				"import "+newPackage+".spec.prov.*;\nimport java.util.*;\n " +
						"\nclass Manager implements IManager {\n" +
				"\tprivate HashMap<String,Object> providedInterfaces;\n"+
				"\tprivate HashMap<String,Object> requiredInterfaces;\n"+
				"\tprivate String[] listReqInt;\n"+
				"\tprivate String[] listProvInt;\n"+
				"\tManager(){	\n\t\tthis.providedInterfaces = new HashMap<String,Object>();\n"+
				"\t\tthis.requiredInterfaces = new HashMap<String,Object>();\n"+
				this.interfaceInstantiation( provInterfaces, reqInterfaces ) + " " +
				this.settingInterfaces( provInterfaces , reqInterfaces ) +
				"\t\tString[] listProv = new String[" + provInterfaces.size() + "];\n" +
				this.settingListOfProvidedInterfaces( provInterfaces ) +
				"\t\tthis.setProvidedInterfaces(listProv);\n"+
				"\t\tthis.setRequiredInterfaces(null);}\n " +
				"\t\tpublic String[] getProvidedInterfaces() { \n" +
				"\t\treturn this.listProvInt;\n" +
				"}\n\t\tpublic void setProvidedInterface(String typeName, Object facade) {\n" +
				"\t\tthis.providedInterfaces.put(typeName,facade);\n" +
				"\t\t}\n\t\t public void setProvidedInterfaces(String[] interfaces) {\n" +
				"\t\tthis.listProvInt = interfaces; }\n\n" +
				"\t\tpublic String[] getRequiredInterfaces() {\n" +
				"\t\treturn this.listReqInt; }\n \n" +
				"\t\tpublic Object getProvidedInterface(String name) {\n" +
				"\t\treturn this.providedInterfaces.get(name);\n" +
				"}\t\t\n 	public void setRequiredInterface(String name, Object facade) {\n" +
				"\t\t	this.requiredInterfaces.put(name,facade);\n" +
				"\t\t	}\n	public void setRequiredInterfaces(String[] interfaces) {\n" +
				"\t\t	this.listReqInt = interfaces;\n" +
				"}\n\t\tpublic Object getRequiredInterface(String name) {\n" +
				"\t\t	return this.requiredInterfaces.get(name);\n }\n}"
		); 
		//System.out.println(managerFile);
		return managerFile;
	}
	
	private String interfaceInstantiation(List<InterfaceMetaInfo> provInterfaces, List<InterfaceMetaInfo> reqInterfaces){
		
		String interfaces = new String("");
		Collections.sort(provInterfaces);
		for(int i=0;i<provInterfaces.size();i++){
			InterfaceMetaInfo ji = provInterfaces.get(i);
			interfaces += ji.getInterfaceName()+ " providedInterface" + i + " = (" + ji.getInterfaceName() + ") new Facade" + i + "();\n";
		}
		for(int j=0;j<reqInterfaces.size();j++){
			InterfaceMetaInfo jir = reqInterfaces.get(j);
			interfaces += jir.getInterfaceName()+" requiredInterface"+j+" = null; \n";
		}
		return interfaces;
	}
	
	private String settingInterfaces(List<InterfaceMetaInfo> provInterfaces, List<InterfaceMetaInfo> reqInterfaces){
		String result = new String("");
		Collections.sort(provInterfaces);
		for(int i=0;i<provInterfaces.size();i++){
			InterfaceMetaInfo jip = provInterfaces.get(i);
			result += "this.setProvidedInterface(\""+jip.getInterfaceName()+"\",providedInterface"+i+");\n";
		}
		for(int j=0;j<reqInterfaces.size();j++){
			InterfaceMetaInfo jip = reqInterfaces.get(j);
			result += "this.setProvidedInterface(\""+jip.getInterfaceName()+"\",requiredInterface"+j+");\n";
		}
		return result;
	}
	
	private String settingListOfProvidedInterfaces(List<InterfaceMetaInfo> provInterfaces){
		String result = new String("");
		Collections.sort(provInterfaces);
		for(int i=0;i<provInterfaces.size();i++){
			InterfaceMetaInfo jip = provInterfaces.get(i);
			result += "listProv["+i+"] = \""+ jip.getInterfaceName()+"\";\n";
		}
		return result;
	}
	
	private boolean createManagerFile(StringBuilder sb,File newComponentFolder){
		//Util util = new Util();
		try {
			File destination = new File(newComponentFolder+"/impl/Manager.java");
			destination.createNewFile();
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
	
	private ClassMetaInfo isImplemented(InterfaceMetaInfo imi, List<ClassMetaInfo> classes ){
		
		for(int i = 0;i<classes.size();i++){
			ClassMetaInfo cmi = classes.get(i);
			List<String> implementedInterfaces = cmi.getImplementedInterfaces();
			for(int j=0;j<implementedInterfaces.size();j++){
				String interfacestr = implementedInterfaces.get(j);
				if( interfacestr.equals(imi.getInterfaceName()) )
					return cmi;
			}
		}
		return null;
	}
	
	
	
}
