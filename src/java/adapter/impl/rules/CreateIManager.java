package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CreateIManager extends CreateFile {

	private final String IMANAGERPATH = "./src/java/adapter/impl/templates/IManager.java";
	
	public boolean createIManager(File newComponentFolder,String newPackage){
		try {
			String canonicalPath = newComponentFolder.getCanonicalPath();
			//verificar se já existe outro componentfactory
			canonicalPath += "/spec/prov/IManager.java";
			File implPath = new File(canonicalPath);
			boolean iManagerIsCreated = implPath.createNewFile();
			if((implPath.exists())&&(iManagerIsCreated)){
				//System.out.println("existe "+implPath.getAbsolutePath());
				File iManager = new File(IMANAGERPATH);
				if(iManager.exists()){
					//System.out.println("existe "+iManager.getAbsolutePath());
					boolean res = this.copy(iManager, implPath, newPackage+".spec.prov");
					return res;
				}
				else{
					System.err.println("The program can't find the IManager class");
				}
					
			}
			else{
				System.err.println("The class 'IManager' was not created");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * @param source the file to be copied
	 * @param destination the file where the source file will be copied
	 * @param newPackage the package of the new class/interface
	 * @return
	 */
	protected boolean copy(File source, File destination, String newPackage){
		//if both source and destination file exists
		if( source.exists() && destination.exists() ){
			System.out.println("copying the files...");
			try {
				
				
				//infra-structure classes
				Util util = new Util();
				BufferedReader in = new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				String s = new String();
				
				//change the visibility of the class/interface
				boolean firstPublicHasPassed = false;
				
				//read each line of the file
				while((s = in.readLine())!= null){
					//if a string 'package' was found, change the old package to the new one
					int i = s.indexOf("package ");
					if(i>=0){
						s = util.changePackage(s, "package "+newPackage+";");
					}
					
					/*//if a string 'public' was found, erase it
					int publik = s.indexOf("public ");
					if( ( publik >= 0 ) && ( !firstPublicHasPassed ) ){
						firstPublicHasPassed = true;
						//s = util.changeVisibility(s);
					}*/
					
					s = s + "\n";				
					out.write(s);
				}
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
	
}
