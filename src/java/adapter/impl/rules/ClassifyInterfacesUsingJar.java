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

public class ClassifyInterfacesUsingJar {

	/**
	 * 
	 * @param newComponentFolder the folder where the new component will be placed
	 * @param jarFile a file represent
	 * @param pack the name of the package
	 * @param classpath a list of dependencies of the jar
	 * @return
	 */
	public boolean classifyInterfaces(File newComponentFolder,File jarFile,String pack,List classpath){
		boolean result = false;
		boolean result2 = false;
		//create a classloader 
		DeadEndClassLoader endClassLoader = new DeadEndClassLoader();
		URL[] locations = null;
		
		//put all the dependencies in the locations[] 
		try {
			locations = new URL[ ( classpath.size() + 1 ) ];
			int k = 0 ;
			for( k = 0; k < classpath.size() ; k++ ){
				File file = new File((String) classpath.get(k));
				if( file.exists() ){
					URL location = null;
					location = file.toURL();
					locations[k] = location;
				}
				else
					System.err.println("Error on classpath:"+file.getAbsolutePath()+" does not exist");
			}
			locations[k] = jarFile.toURL();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		URLClassLoader urlClassLoader = new URLClassLoader(locations, endClassLoader);
		JarClassLoader jarClassLoader;
		try {
			jarClassLoader = new JarClassLoader(jarFile.toURL());
			//get all the provided interfaces and put in a list
			List<InterfaceMetaInfo> provInterfaces = new ArrayList<InterfaceMetaInfo>();
			List<InterfaceMetaInfo> reqInterfaces = new ArrayList<InterfaceMetaInfo>();
			this.divideInterfaces(jarFile,urlClassLoader,reqInterfaces,provInterfaces);
			Collections.sort(provInterfaces);
			Collections.sort(reqInterfaces);
			// for each interface create a new file
			for(int i=0;i<provInterfaces.size();i++){
				InterfaceMetaInfo imi = provInterfaces.get(i);
				String interfaceName = getOnlyLastName( imi.getInterfaceName() );
				//StringBuilder sb = this.interfaceCreator(imi, pack+".spec.prov", "IProvidedInterface"+i,newComponentFolder);
				StringBuilder sb = this.interfaceCreator(imi, pack+".spec.prov", interfaceName,newComponentFolder);
				//result = this.createInterfaceFile(sb, newComponentFolder.getAbsolutePath()+"/spec/prov/","IProvidedInterface"+i);
				result = this.createInterfaceFile(sb, newComponentFolder.getAbsolutePath() + "/spec/prov/" , interfaceName);
			}
			for(int j=0;j<reqInterfaces.size();j++){
				InterfaceMetaInfo imi2 = reqInterfaces.get(j);
				StringBuilder sb2 = this.interfaceCreator(imi2, pack+".spec.req", "IRequiredInterface"+j,newComponentFolder);
				result2 = this.createInterfaceFile(sb2, newComponentFolder.getAbsolutePath()+"/spec/req/","IRequiredInterface"+j);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return result && result2;
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
	
	private boolean createInterfaceFile(StringBuilder sb,String newComponentFolder,String interfaceName){
		
		try {
			
			File destination = new File(newComponentFolder+interfaceName+".java");
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
	
	private String createParametersList(List<Parameter> parameters){
		String res = new String("");
		if ( !parameters.isEmpty() ){
			int secLast = (parameters.size() - 1);
			int i=0;
			for(i=0;i < secLast;i++){
				Parameter param = parameters.get(i);
				String aux = param.getType()+" ";
				aux = aux.concat(param.getName()+", ");
				res = res.concat(aux);
			}
			Parameter param = parameters.get(i);
			String aux = param.getType()+" ";
			aux = aux.concat(param.getName());
			res = res.concat(aux);
		}
		return res;
	}
	
	private StringBuilder interfaceCreator(InterfaceMetaInfo imi, String pack,String interfaceName,
			File newComponentFolder){
		StringBuilder sb = new StringBuilder("");
		
		//create the package
		sb = sb.append("package "+pack+";");
		
		//create the import list
		List<String> importList = imi.getImportList();
		if( importList != null)
			for(int i=0;i<importList.size();i++){
				sb = sb.append("import "+ importList.get(i)+ ";\n");
			}
		
		//create the interface name
		sb = sb.append("public interface "+interfaceName+" {\n");
		
		//create the methods
		List<MyMethod> methodsList = imi.getMethods();
		if (methodsList != null)
			for(int j=0;j<methodsList.size();j++){
				MyMethod method = methodsList.get(j);
				sb = sb.append(method.getVisibility()+" ");
				sb = sb.append(method.getReturnType()+" ");
				sb = sb.append(method.getName()+"(");
				String parametersList = createParametersList(method.getParameters());
				sb = sb.append(parametersList+");\n");
			}
		sb = sb.append("\n}\n");
		return sb;
	}
	
	
	private void divideInterfaces(File jarFile, URLClassLoader urlClassLoader,
			List<InterfaceMetaInfo> reqInterfaces, List<InterfaceMetaInfo> provInterfaces){
		Extractor extractor = new Extractor();
		List<InterfaceMetaInfo> interfaces = extractor.getInterfacesMetaInfo(jarFile,urlClassLoader);
		for(int i=0;i<interfaces.size();i++){
			InterfaceMetaInfo imi = interfaces.get(i);
			List<ClassMetaInfo> classes = extractor.getClassesMetaInfo(jarFile,urlClassLoader);
			//maybe it is not necessary to get the classmetainfo...
			List<ClassMetaInfo> implementors  = this.isImplemented(imi, classes);
			//System.out.println("class "+implementor.getName()+" implements "+imi.getInterfaceName());
	
			if( implementors.isEmpty() ){
				imi.setImplementedBy(implementors);
				reqInterfaces.add( imi );
			}
			else{
				imi.setImplementedBy(implementors);
				provInterfaces.add( imi );
			}
		
		}
		return;
	}
	
	private List<ClassMetaInfo> isImplemented(InterfaceMetaInfo imi, List<ClassMetaInfo> classes ){
		
		List<ClassMetaInfo> implementors = new ArrayList<ClassMetaInfo>();
		for(int i = 0; i < classes.size() ; i++){
			ClassMetaInfo cmi = classes.get(i);
			List<String> implementedInterfaces = cmi.getImplementedInterfaces();
			for(int j=0;j<implementedInterfaces.size();j++){
				String interfacestr = implementedInterfaces.get(j);
				if( interfacestr.equals(imi.getInterfaceName()) )
					implementors.add( cmi );
			}
		}
		return implementors;
	}
	
}
