package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyComponentFactory extends CreateFile {

	private final String COMPONENTFACTORYPATH = "./src/java/adapter/impl/templates/ComponentFactory.java";
	
	public static void main(String args[]){
		File f = new File("/home/lsd/ra001973/workspace2/adapter/bin/newComponent");
		System.out.println(f.getAbsolutePath());
		File f2 = new File("./src/java/adapter/impl/ComponentFactory.java");
		System.out.println(f2.getAbsolutePath());
		if(f2.exists()){
			System.out.println("existe "+f2.exists());
		}
		CopyComponentFactory ccf = new CopyComponentFactory();
		ccf.createComponentFactory(f, "newComponent.impl");
	}
	
	private boolean isThereAnyCompFactory(File newComponentFolder,String name){
		File[] list = newComponentFolder.listFiles();
		for(int i=0;i<list.length;i++){
			String fileName = list[i].getAbsolutePath();
			if(fileName.endsWith(name))
				return true;
		}
		return false;
	}
	
	public boolean createComponentFactoryUsingJAR(File newComponentFolder,String newPackage, String pack){
		if( ( newComponentFolder != null ) && ( newComponentFolder.exists() ) ){
			/*the package is not correct. It actually corresponds to the absolute path name of the 
			 * new component directory*/
			/*int lastslash = -1;
			String pack;
			if( newPackage.endsWith( File.separator ) ){
				int aux = newPackage.length() - 1 ;
				if( aux < 0 )
					aux = newPackage.length();
				lastslash = newPackage.lastIndexOf(File.separator, aux );
				lastslash++;
				pack = newPackage.substring(lastslash, aux);
			}
			else{
				lastslash = newPackage.lastIndexOf( File.separator, newPackage.length() );
				lastslash++;
				pack = newPackage.substring( lastslash, newPackage.length() );
			}*/
			return this.createComponentFactory(newComponentFolder, pack);
		}
		else
			return false;
	}
	
	public boolean createComponentFactory(File newComponentFolder,String newPackage){
		try {
			String canonicalPath = newComponentFolder.getCanonicalPath();
			
			canonicalPath += "/impl/ComponentFactory.java";
			File implPath = new File(canonicalPath);
			
			//delete the old ComponentFactory, in case it exists
			boolean deleted = implPath.delete();
			
			//create ComponentFactory file
			boolean compFactoryIsCreated = implPath.createNewFile();
			
			//if ComponentFactory was created
			if( ( implPath.exists() ) && ( compFactoryIsCreated ) ){
								
				//read the template ComponentFactory
				File compFactory = new File( COMPONENTFACTORYPATH ) ;
				if( compFactory.exists() ) {
					//copy the content of one file to another, making minor changes
					boolean res = this.copy( compFactory , implPath , newPackage ) ;
					return res;
				}
				else{
					System.err.println("The program can't find the ComponentFactory class");
				}
					
			}
			else{
				System.err.println("The class 'ComponentFactory' was not created");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	private String getOldPackage(String file){
		//read from the template Facade
		String pattern = new String("package ");
		int packagePos = file.indexOf(pattern);
		if( packagePos >= 0){
			int semicolon = file.indexOf(';', packagePos);
			semicolon++;
			int beginString = packagePos;
			int endString = semicolon;
			packagePos += pattern.length();
			
			//remove empty spaces
			while(file.charAt(packagePos) == ' ')
				packagePos++;
			
			if( packagePos < semicolon ){
				String result = file.substring(beginString, endString);
				return result;
			}
		}
		return null;
	
	}
	
	protected boolean copy(File source, File destination, String newPackage){
		//if both source and destination file exists
		if( source.exists() && destination.exists() ){
			System.out.println("copying the ComponentFactory file...");
			try {
				
				
				//infra-structure classes
				Util util = new Util();
				BufferedReader in = new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				//String file = new String();
				
				long fileSizeLong = source.length();
				int fileSize = Integer.parseInt(String.valueOf(fileSizeLong));
				char[] cbuf = new char[fileSize];
				in.read(cbuf, 0, fileSize);
				String file = String.valueOf(cbuf);
	
				//change the old package for a new one
				StringPosition sp = util.readPackage(file);
				String replacePackage = "package "+newPackage+".impl;";
				if(sp != null)
					file = util.replace(replacePackage, file, sp.beginString, sp.endString);
				
				sp = this.readImport(file);
				String newImport = "import "+newPackage+".spec.prov.*;\nimport "+newPackage+".impl.*;";
				if(sp != null)
					file = util.replace(newImport, file, sp.beginString, sp.endString);
				
							
				file = file + "\n";				
				out.write(file);
				
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
	
	protected StringPosition readImport(String file){
		
		//read ComponentFactory template 
		String pattern = new String("import ");
		int importPos = file.indexOf(pattern);
		int semicolon = file.indexOf(';', importPos);
		semicolon++;
		if(importPos>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = importPos;
			sp.endString = semicolon;
			return sp;		}
		else
			return null;
	}
	
}
