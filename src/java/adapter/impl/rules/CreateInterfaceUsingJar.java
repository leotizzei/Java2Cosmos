package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import adapter.impl.jarFiles.ClassMetaInfo;
import adapter.impl.jarFiles.JarExtractor;

public class CreateInterfaceUsingJar {

//	private final String FACADEPATH = "./src/java/adapter/impl/templates/Facade.java";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting the creation of interfaces...");
		CreateInterfaceUsingJar c= new CreateInterfaceUsingJar();
		String compFolder = "/home/lsd/ra001973/workspace2/adapterCaseStudy/diamond/ds_eval/";
		String jarFile = "/home/lsd/ra001973/mestrado/transformador/caseStudy/transformador/componentsource/diamond/ds_eval.jar";
		String pack = new String("ds_eval");
		File f = new File(jarFile);
		List<String> classpath = new ArrayList<String>();
		File compfile = new File (compFolder);
		boolean res = c.createInterfaceFromPublicClasses(f, compfile, pack, classpath);
		if(!res)
			System.err.println("The system couldn't create the new interfaces");
		else
			System.out.println("the new interfaces were created");
	}
	
	
	
	
	
	public boolean createInterfaceFromPublicClasses(File jarFile,File newComponentFolder,String newPackage,List<String> classpath){
		return this.infrastructure(jarFile, newComponentFolder, newPackage, classpath);
	}
	
	/**
	 * this methods loads the jar file and its classpaths and calls methods that will 
	 * create the interfaces
	 * @param jarFile the jar file that we are tranforming
	 * @param newComponentFolder the component folder where the new files will be placed
	 * @param newPackage the package of the new component
	 * @param classpath the jar classpath
	 * @return 
	 */
	protected boolean infrastructure(File jarFile,File newComponentFolder,String newPackage,List<String> classpath){
		if(  ( classpath != null ) && ( jarFile != null ) && ( newComponentFolder != null ) && ( newPackage != null ) ){
			
			boolean result[] = new boolean[ classpath.size() ];
			
			DeadEndClassLoader endClassLoader = new DeadEndClassLoader();
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
						result[k] = true;
					}
					else{
						result[k] = false;
						System.err.println("Error on classpath:"+file.getAbsolutePath()+" does not exist");
					}
				}
				locations[k] = jarFile.toURL();
			} catch (MalformedURLException e1) {
				System.err.println("Some error on CreateInterfaceUsingJar:infrastructure");
				e1.printStackTrace();
			}
			
			URLClassLoader urlClassLoader = new URLClassLoader(locations, endClassLoader);
			//JarClassLoader jarClassLoader;
			
			//get a list of interface that will be created 
			List<InterfaceMetaInfo> listClasses = this.searchManager(jarFile, urlClassLoader);
			this.createNewInterfaces(listClasses, newPackage, newComponentFolder);
			
			for(int r = 0; r < result.length; r++){
				if( !result[r])
					return false;
			}
			return true;
		}
		else
			return false;
	}
	
	/**
	 * this method search for public class but stores its information in a list of interfaces 
	 * meta-information
	 * @param jarFile the jar file we are transforming
	 * @param urlClassLoader the jar classloader 
	 * @return
	 */
	private List<InterfaceMetaInfo> getPublicClasses(File jarFile, URLClassLoader urlClassLoader){
		JarExtractor extractor = new JarExtractor();
		//extract information about the public classes 
		List<ClassMetaInfo> classList = extractor.getClassesMetaInfo(jarFile, urlClassLoader);
		List<InterfaceMetaInfo> result = new ArrayList<InterfaceMetaInfo>();
		
		//for each public class, create correspondent interface meta-information
		for(int i=0;i<classList.size();i++){
			ClassMetaInfo cmi = classList.get(i);
			if( cmi != null ){
				String visibility = cmi.getVisibility();
				// get only public classes
				if( ( visibility != null ) && ( visibility.equals( "public" ) ) && ( !cmi.isAbstract() ) ){
					InterfaceMetaInfo imi = new InterfaceMetaInfo();
					String aux = cmi.getName();
					
					//the interface name corresponds to its whole name
					String interfaceName =  aux.replace('.', '_') ;
					imi.setInterfaceName( "I" + interfaceName );
					imi.setConstructors( cmi.getConstructors() );
					//WARNING! get all methods, including the private ones
					imi.setMethods( cmi.getMethods() );
					List<ClassMetaInfo> implementor = new ArrayList<ClassMetaInfo>();
					implementor.add( cmi );
					imi.setImplementedBy( implementor );
					imi.setVisibility( "public" );
					result.add( imi );
				}
			}
		}
		return result;
	}
	
	private List<InterfaceMetaInfo> searchManager(File jarFile, URLClassLoader urlClassLoader){
		
		//return a list of interfaces that will be created
		List<InterfaceMetaInfo> futureProvidedInterfaces = getPublicClasses( jarFile, urlClassLoader );
		if( ( futureProvidedInterfaces == null ) || ( futureProvidedInterfaces.isEmpty() ) ){
			System.err.println("The interface list is either null or empty");
			return null;
		}
		else{
			return futureProvidedInterfaces;
		}
	}
	
	private boolean createNewInterfaces(List<InterfaceMetaInfo> futureProvidedInterfaces, String newPackage,
			File newComponentFolder){
			
			boolean anyInterfaceWerentCreated = false;
			boolean anyFacadeWerentCreated = false;
			boolean aux = false;
			//for each interface, create a new Facade
			for(int i = 0 ; i < futureProvidedInterfaces.size() ; i++){
				InterfaceMetaInfo imi = futureProvidedInterfaces.get(i);
				imi.setPackag( newPackage );
				System.out.println("creating the files...");
				
				//create the interface
				aux = this.createInterfaceFile( newComponentFolder , imi );
				
				boolean facadeCreated = false;
				//if any Interface weren't created, return false
				if( !aux ){
					System.err.println("Some interfaces weren't created");
					anyInterfaceWerentCreated = true;
				}
				else{
					FacadeHelperUsingJar fh = new FacadeHelperUsingJar();
					facadeCreated = fh.createFacade(newComponentFolder,newPackage,imi,i);
					//if any Facade weren't created, return false
					if( !facadeCreated ){
						System.err.println("Some Facades weren't created");
						anyFacadeWerentCreated = true;
					}
				}
				

			}
			
			//change manager
			
			return ( (!anyInterfaceWerentCreated) && (!anyFacadeWerentCreated) );
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
	 * read the classes that implements the interfaces that will be created
	 * @return an array of strings with the name of these classes
	 */
	private String[] getListOfClasses(){
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try {
			System.out.println("Enter with the complete name of all classes you would like to create\n" +
					"an interface to it. Separate the name of each class by single space.\n For example: " +
					"java.util.List java.util.ArrayList");
			input = in.readLine();
			String[] classes = input.split(" ",-1);
			/*for(int i=0;i<classes.length;i++)
				System.out.println(classes[i]);*/
			System.out.println(input);
			
			
			return classes;
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	
	
	
	private boolean createInterfaceFile(File newComponentFolder,InterfaceMetaInfo imi){
		if( ( newComponentFolder != null ) && ( newComponentFolder.exists() ) ){
			try {
				String path = newComponentFolder.getCanonicalPath();
				path += "/spec/prov/"+imi.getInterfaceName()+".java";
				File newInterface = new File(path);
				boolean created = newInterface.createNewFile();
				if( created )
					//fill the content of interface
					return this.fillInterfaceFile(newInterface,imi);
				else
					return false;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			return false;
		return false;
	}
	
	private boolean fillInterfaceFile(File newInterface, InterfaceMetaInfo imi){
		// infra-structure classes
		FileWriter fw;
		try {
			
			fw = new FileWriter(newInterface.getCanonicalPath());
			BufferedWriter out = new BufferedWriter(fw);    
			String s = new String();
			
			//create package
			String packag = "package "+imi.getPackag()+".spec.prov;\n";
			
			//create import list
			List<String> importList = imi.getImportList();
			String imports = new String("");
			if( importList != null){
				for(int i=0;i<importList.size();i++){
					imports += "import "+importList.get(i)+"\n";
				}
			}
			
			//create interface modifiers
			String interfaceModifiers = imi.getVisibility()+" interface "+imi.getInterfaceName()+" {\n";
			
			//create interface methods
			String methods = new String("");
			List<MyMethod> methodsList = imi.getMethods();
			for( int j=0;j<methodsList.size();j++){
				MyMethod mm = methodsList.get(j);
				
				//interfaces don't have static methods 
				//if( !mm.isStatic() ){
				if( ( mm != null ) && (mm.getVisibility().equals( "public" ) ) ){	
					methods += "\t\t"+mm.getVisibility() + " " + mm.getReturnType() + " " + mm.getName() + "(";
					List<Parameter> paramList = mm.getParameters();
					if ( (paramList != null ) && ( !paramList.isEmpty() ) ){
						int secLast =  paramList.size() - 1;
						int k = 0;
						for( k = 0 ; k < secLast ; k++){
							Parameter p = paramList.get(k);
							methods += p.getType() + " " + p.getName() + ",";
						}
						Parameter p = paramList.get(k);
						methods += p.getType() + " " + p.getName();
					}
					methods += ");\n\n";
				}
				//}
			}
			methods +="\n}\n";
			String file = packag + imports + interfaceModifiers + methods;
			out.write(file);
			out.close();
			System.out.println("The file "+ newInterface.getAbsolutePath()+" was created");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	
	
	
	
	
}
