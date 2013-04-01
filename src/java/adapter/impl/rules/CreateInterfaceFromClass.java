package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import adapter.impl.jarFiles.ClassMetaInfo;

public class CreateInterfaceFromClass {

	//private final String FACADEPATH = "./src/java/adapter/impl/templates/Facade.java";
	private String metadataPath = "output.xml";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting the creation of interfaces...");
		CreateInterfaceFromClass c= new CreateInterfaceFromClass();
		String compFolder = "/home/lsd/ra001973/workspace2/adapterCaseStudy/newComponent/newComponentSrc/";
		String pack = "newComponentSrc";
		File f = new File("output.xml");
		File compfile = new File (compFolder);
		boolean res = c.createNewInterfaces(f, compfile, pack);
		if(!res)
			System.err.println("The system couldn't create the new interfaces");
		else
			System.out.println("the new interfaces were created");
	}
	
	/**
	 * this method creates all interfaces and facades 
	 * @param metadata the file that represents the metadata from all source codes
	 * @param newComponentFolder the directory of the new component folder
	 * @param newPackage the new component package. Only the first package is specified
	 * @return return true if all the interfaces and its respectives facades were created. Otherwise,
	 * return false.
	 */
	public boolean createNewInterfaces(File metadata,File newComponentFolder,String newPackage){
		List<InterfaceMetaInfo> interfaceList = this.manager();
		if( ( interfaceList == null ) || ( interfaceList.isEmpty() ) ){
			System.err.println("The interface list is either null or empty");
			return false;
		}
		else{
			boolean anyInterfaceWerentCreated = false;
			boolean anyFacadeWerentCreated = false;
			boolean aux = false;
			//for each interface, create a new Facade
			for(int i=0;i<interfaceList.size();i++){
				InterfaceMetaInfo imi = interfaceList.get(i);
				imi.setPackag(newPackage);
				System.out.println("creating the files...");
				
				//create the interface
				aux = this.createInterfaceFile(newComponentFolder,imi);
				
				boolean facadeCreated = false;
				//if any Interface weren't created, return false
				if( !aux )
					anyInterfaceWerentCreated = true;
				else{
					FacadeHelper fh = new FacadeHelper();
					facadeCreated = fh.createFacade(newComponentFolder,newPackage,imi,i);
					//if any Facade weren't created, return false
					if( !facadeCreated )
						anyFacadeWerentCreated = true;
				}
				

			}
			//change Manager
			ChangeManager cm = new ChangeManager();
			String managerPath;
			try {
				managerPath = newComponentFolder.getCanonicalPath() + "/impl/Manager.java";
				boolean res = cm.changeManager(managerPath, interfaceList);
				if( res )
					System.out.println("Manager was changed");
				else
					System.err.println("Can't update Manager");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return ( (!anyInterfaceWerentCreated) && (!anyFacadeWerentCreated) );
		}
		
	}
	
	private List<InterfaceMetaInfo> manager(){
		System.out.println("starting manager...");
		
		//get list of classes
		//String[] classList = this.getListOfClasses();
		String[] classList = this.getListOfClasses();
		/*DEBUG*/
		/*classList[0] = "local.rami.Server.serverUpdateProcessor";
		classList[1] = "local.rami.Aux.jarHeader";
		classList[2] = "local.rami.Client.clientUpdateGenerator";*/
		//read the metadata
		DOMReader dr = new DOMReader();
				
		File metadata = new File(metadataPath);
		if( !metadata.exists() ){
			System.out.println("Metadata file doesn't exist");
			return null;
		}
		
		//get the root node
		Node root = dr.readFiles(metadata);
		
		
		List<InterfaceMetaInfo> newInterfaces = new ArrayList<InterfaceMetaInfo>();
		
		System.out.println("number of new interfaces = "+classList.length);
		//get information from each class
		for(int i=0;i<classList.length;i++){
			//get only the last name of each class
			String fullName = classList[i];
			//String lastName = this.getClassLastName( fullName );
			
			//extract class information from metadata
			InterfaceMetaInfo imi = new InterfaceMetaInfo();
			this.getClassInformation( root, fullName, imi );
			System.out.println(imi.getInterfaceName()+":"+imi.getVisibility());
			
			
			//add all information to the list of interfaces 
			newInterfaces.add(imi);
		}
		return newInterfaces;
		
	}
	

	private String getClassLastName(String fullName){
		int lastPoint = fullName.lastIndexOf('.');
		lastPoint++;
		String lastName = fullName.substring(lastPoint, fullName.length());
		return lastName;
	}
	
	/**
	 * get the return type of one specific method. 
	 * @param methodNode the method whose return type will be returned
	 * @return the name of the return type
	 */
	private String getReturnType(Node methodNode){
		if( methodNode == null )
			return null;
		else{
			File metafile = new File(this.metadataPath);
			XPathHelper xhelper = new XPathHelper(metafile);
		
			//get all parameters tag and put it all in a nodelist
			String methodName = this.getAttribute( methodNode, "name" );
			Node parent = methodNode.getParentNode();
			String classFullName = null;
			if( parent != null ){
				classFullName = this.getAttribute( parent , "name");
			}
			//get the return type whose method is called 'methodName' and whose class is called 'classFullName'
			String expression = "/java-source-program/descendant::class[@name='"+classFullName+"']/method[@name='"+methodName+"']/type/@name";
			String type = xhelper.evaluate(expression);
			//System.out.println("return type = "+type);
			return type;
		}
	}
	
	
	/**
	 * 
	 * @param n
	 * @return
	 */
	private List<Parameter> getParametersFromMetadata(Node methodNode){
		File metafile = new File(this.metadataPath);
		XPathHelper xhelper = new XPathHelper(metafile);
		
		String methodName = this.getAttribute( methodNode, "name" );
		Node parent = methodNode.getParentNode();
		String classFullName = null;
		if( parent != null ){
			Node grandParent = parent.getParentNode();
			if( grandParent != null)
				classFullName = this.getAttribute( grandParent , "name");
		}
		
		// get all parameters tag and put it all in a nodelist
		String expression = "/java-source-program/descendant::java-class-file" +
		"[@name='"+classFullName+"']/class/method[@name='"+methodName+"']/formal-arguments/formal-argument";
		NodeList nodelist = xhelper.getNodeList(expression);
		
		List<Parameter> paramList = new ArrayList<Parameter>();
		for(int i=0;i<nodelist.getLength();i++){
			Parameter p = new Parameter();
			Node aux = nodelist.item(i);
			String paramName = this.getAttribute( aux , "name" );
			if( paramName != null){
				p.setName( paramName );
				//preciso especificar a classe e o método
				String typeExpr = "/java-source-program/descendant::java-class-file" +
				"[@name='"+classFullName+"']/class/method[@name='"+methodName+"']/formal-arguments/" +
						"formal-argument[@name='"+paramName+"']/type/@name";
				String type = xhelper.evaluate(typeExpr);
				p.setType( type );
				// I am assuming that the program JAVA2XML put the parameters in a correct
				// position while extracting metadata from the source code. The samples I saw
				// has shown it is correct, but...
				p.setPosition( i );
			}
			paramList.add(p);
		}
		return paramList;
	}
	
	/*metodo teste*/
	private void getParametersFromMetadata(Node n, List<Parameter> paramList){
		if( ( n == null ) || ( n.getNodeName().equals("method") ) )
			return;
		else{
			//this is a parameter
			if( n.getNodeName().equals("formal-argument") ){
				Parameter p = new Parameter();
				p.setName( this.getAttribute(n, "name"));
				if( n.hasChildNodes() ){
					Node child = n.getFirstChild();
					while( (child != null) && (!child.getNodeName().equals("type")) )
						child = child.getNextSibling();
					if( ( child != null ) && ( child.getNodeName().equals("type") ) )
						p.setType(this.getAttribute(child, "name"));
				}
				paramList.add( p );
			}
			this.getParametersFromMetadata( n.getFirstChild(), paramList );
			this.getParametersFromMetadata( n.getNextSibling(), paramList );
		}
	}
		
		
	
	private void getMethods(Node n, List<MyMethod> methods){
		
	
		if( ( n == null) || ( n.getNodeName().equals("class") ) )
			return;
		else{
			if( n.getNodeName().equals( "method" ) ){
				MyMethod method = new MyMethod();
				method.setName(this.getAttribute( n, "name" ) );
				method.setVisibility(this.getAttribute( n, "visibility") );
				
				//get the return type
				method.setReturnType( this.getReturnType( n ) );
				
				//get parameters
				// acho q este metodo pode estar errado...
				//List<Parameter> paramList = this.getParametersFromMetadata( n );
				List<Parameter> paramList  = new ArrayList<Parameter>();
				this.getParametersFromMetadata( n.getFirstChild(), paramList );
				
				method.setParameters( paramList );
				methods.add(method);
			}
			getMethods( n.getFirstChild() , methods );
			getMethods( n.getNextSibling() , methods );
			return;
		}
	}
	
	
	
	
	private void getClassInformation(Node n, String classFullName, InterfaceMetaInfo imi){
		if( n == null){
			return;
		}
		else{
			if( n.getNodeName().equals("class") ){
				Node parent = n.getParentNode();
				String parentName = this.getAttribute( parent , "name");
				String aux = parentName;
				
				//remove '.java'
				int lastPoint = aux.lastIndexOf('.');
				aux = aux.substring(0, lastPoint);
				
				//replace path by qualified name
				aux = aux.replace('/', '.');
				if( aux.endsWith( classFullName ) ){
					//set class name and visibility
					String lastName = this.getClassLastName( classFullName );
					imi.setInterfaceName("I" + lastName );
					imi.setVisibility( "public" );
					
					//set import list
					imi.setImportList( this.getImportListFromMetadata( parentName ));
					
					//set that this interface is implemented by the 'classFullName' class
					/*List<String> implemented = new ArrayList<String>();
					implemented.add( classFullName );
					imi.setImplementedBy( implemented );*/
					
					ClassMetaInfo cmi = new ClassMetaInfo();
					cmi.setName( classFullName );
					imi.addImplementedBy( cmi );
					
					//set the constructors
					List<MyConstructor> constructors = new ArrayList<MyConstructor>();
					this.getConstructors(n, classFullName, constructors);
					imi.setConstructors( constructors );
					
					//set the methods
					List<MyMethod> methods = new ArrayList<MyMethod>();
					this.getMethods(n.getFirstChild(), methods);
					imi.setMethods( methods );
					
				}
				
			}
			getClassInformation( n.getFirstChild() , classFullName, imi);
			getClassInformation( n.getNextSibling() , classFullName, imi);
			
		}
		return;
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
	
	
	/*private StringBuilder createImportList(List<String> importList){
		return null;
	}*/
	
	private List<String> getImportListFromMetadata(String absolutePath){
		if( absolutePath == null)
			return null;
		else{
		
			File metafile = new File(this.metadataPath);
			XPathHelper xhelper = new XPathHelper(metafile);
		
			//get the import nodes
			String expression = "/java-source-program/descendant::java-class-file[@name='"+absolutePath+"']/import";
			NodeList nodelist = xhelper.getNodeList(expression);
			List<String> importList = new ArrayList<String>();
			for(int i=0;i<nodelist.getLength();i++){
				Node node = nodelist.item(i);
				String str = this.getAttribute(node, "module");
				if( str != null)
					importList.add(str);
			}
			return importList;
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
	
	
	private boolean createInterfaceFile(File newComponentFolder,InterfaceMetaInfo imi){
		if( ( newComponentFolder != null ) && ( newComponentFolder.exists() ) ){
			try {
				String path = newComponentFolder.getCanonicalPath();
				path += "/spec/prov/"+imi.getInterfaceName()+".java";
				File newInterface = new File(path);
				boolean created = newInterface.createNewFile();
				if( created )
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
		Util util = new Util();
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
				methods += mm.getVisibility()+" "+mm.getReturnType()+" "+mm.getName()+"(";
				List<Parameter> paramList = mm.getParameters();
				if ( (paramList != null ) && ( !paramList.isEmpty() ) ){
					int secLast =  paramList.size() - 1;
					int k = 0;
					for( k = 0 ; k < secLast ; k++){
						Parameter p = paramList.get(k);
						methods += p.getType()+" "+p.getName()+",";
					}
					Parameter p = paramList.get(k);
					methods += p.getType()+" "+p.getName();
				}
				methods += ");\n";
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
	
	private List<Parameter> getConstructorInformation(String qualifiedName){
		if( qualifiedName == null )
			return null;
		else{
			File metafile = new File(metadataPath);
			XPathHelper xhelper = new XPathHelper(metafile);
					
			String expression = "/java-source-program/java-class-file[@name='"+qualifiedName+"']" +
				"/descendant::constructor";
				//System.out.println(expression);
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
	
	/**
	 * return a list of constructors if the class has any declared constructor(s).
	 * @param n current node that traverse the xml tree 
	 * @param absoluteClassName the name of class whose constructors we would like to find
	 * @param constructors an empty list that we will fill in case we find the constructors
	 */
	private void getConstructors(Node n, String absoluteClassName, List<MyConstructor> constructors){
		if( n == null )
			return;
		else{
			if( n.getNodeName().equals("constructor") ){

				absoluteClassName = absoluteClassName.replace('.', '/');
				absoluteClassName += ".java";
				//check if this constructor belongs to the class we want
				Node parent = n.getParentNode();
				Node grandParent = parent.getParentNode();
				String absolutePath = this.getAttribute(grandParent, "name");
				if( absolutePath.endsWith( absoluteClassName ) ){

					//that's the class we want
					MyConstructor c = new MyConstructor();
					c.setFullClassName( absoluteClassName );
					c.setName( this.getAttribute(n, "name"));
					c.setVisibility( this.getAttribute(n, "visibility"));
					
					//get constructors parameters
					List<Parameter> constructorParameters = new ArrayList<Parameter>();
					this.getParametersFromMetadata(n, constructorParameters);
					c.setParameters( constructorParameters );
					constructors.add(c);
				}
				
			}
				
			this.getConstructors(n.getFirstChild(), absoluteClassName, constructors);
			this.getConstructors(n.getNextSibling(), absoluteClassName, constructors);
		}
	}
	
}
