package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

public class ClassManager {

	
	
	
	/**
	 * copy the classes from one folder to another
	 * @param newComponentFolder
	 * @param metadata
	 * @param pack
	 * @return
	 */
	public boolean copyClasses(File newComponentFolder,File metadata,String pack){
		DOMReader dr = new DOMReader();
		try {
			Node root = dr.readFiles(metadata);
			System.out.println("starting copyClasses...");
			//save the list of interfaces
			List<JavaInfo> classList = new ArrayList<JavaInfo>();
		
				
			dr.fillJavaInfo(root, "class", classList);
			
			
			String implPath = newComponentFolder.getCanonicalPath()+"/impl/";
			File newComponentImplFolder = new File(implPath);
			boolean moved = this.moveClasses(newComponentImplFolder, classList,pack+".impl");
			return moved;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	
	private String getClassLastName(String path){
		int pos = path.lastIndexOf("/");
		String result;
		if(pos<1){
			System.out.println("Something is wrong with the Class name");
			result = "ERROR";
		}
		else
			//get only the Class name and its extension
			result = path.substring(pos, path.length());
		return result;
	}
	/**
	 * copy the classes listed in classList to the destination folder, changing the package
	 * @param destinationFolder
	 * @param classList
	 * @param pack
	 * @return
	 */
	private boolean moveClasses(File destinationFolder,List<JavaInfo> classList,String newPackage){
		try {
			//if the destination folder exists
			if(destinationFolder.exists()){
				boolean copied = false;
				//for all classes
				for(int i=0;i<classList.size();i++){
					copied = false;
					//create a java.io.File to each class
					String path = classList.get(i).getAbsolutePath();
					File source = new File(path);
					
					//if the source file exists
					if( source.exists() ){
						String lastName = this.getClassLastName( path );
						String destinationPath = destinationFolder.getCanonicalPath();
						
						System.out.println("[moveclasses]package:"+newPackage+" destinationPath +lastName = "+destinationPath +lastName);
						File dest = new File( destinationPath + lastName );
						
						if( dest.createNewFile() ){
							copied = this.copy( source , dest , newPackage );
							if( copied )
								System.out.println("The file "+source.getAbsolutePath()+" was copied to "+dest.getAbsolutePath());
							else	
								System.out.println("The system can't copy "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
						}
						else
							System.out.println("It was not possible to create "+dest.getAbsolutePath());
					}
					else	
						System.out.println("The system can't copy "+source.getAbsolutePath());
				}
				return copied;
			}
			else{
				System.err.println("the destination folder "+destinationFolder.getAbsolutePath()+" does not exist");
				return false;
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
	
	
	private String changeClass(String file, String newPackage,BufferedWriter out){
		Util util = new Util();
		
		String oldPackage = this.getOldPackage(file);
		
		//change the package, StringPosition save the position of the string that will be replaced
		StringPosition sp = util.readPackage(file);
		String packReplacement = "package "+newPackage+";";
		if(sp != null)
			file = util.replace(packReplacement, file, sp.beginString, sp.endString);
		
		// change class visibility 
		sp = util.readClassVisibility(file);
		String newVisibility = "";
		if(sp != null)
			file = util.replace(newVisibility, file, sp.beginString, sp.endString);
		
		// change import 
		sp = this.readImport(file,oldPackage);
		if(sp != null)
			file = util.replace("import "+newPackage, file, sp.beginString, sp.endString);
		
		return file;
	}
	
	protected boolean copy(File source, File destination, String newPackage){
		//if both source and destination file exists
		if(source.exists()&&destination.exists()){
			System.out.println("copying "+destination.getAbsolutePath());
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
				
				String facadeStr = this.changeClass(file, newPackage, out);
				
				out.write(facadeStr);
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
	
	private StringPosition readImport(String file, String oldPackage){
		
		//read from the template Facade
		String pattern = new String("import "+oldPackage);
		int importPos = file.indexOf(pattern);
		int semicolon = file.indexOf(';', importPos);
		semicolon++;
		if(importPos>=0){
			StringPosition sp = new StringPosition();
			sp.beginString = importPos;
			sp.endString = semicolon;
			return sp;		
		}
		else
			return null;
	}
}
