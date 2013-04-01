package adapter.impl;

import harsh.javatoxml.Java2XML;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.impl.jarFiles.Unzip;



public class Control {
	
	//the directory where the files will be placed temporaly
	private final String TEMP = "/home/lsd/ra001973/workspace2/adapter/temp/";
	
	Control() {
		
	}
	
	/**
	 *  This method creates a wrapper to a jar file, so this jar file become compliant with COSMOS model 
	 * @param newComponentFolder the directory where the new java source code will be placed
	 * @param jarPath the canonical path of the jar file
	 * @param classpath the canonical path of all files that the jar depends
	 */
	public void changeJarToCosmos(String jarstr, String cosmosRules, String baseDirectory, String[] classpath, String pack){
		String aux = pack.replace('.', File.separatorChar);
		String newComponentFolder = baseDirectory + aux ;
		
		//check if the file exists and create new a folder
		File newFolder = new File( newComponentFolder );
		if( !newFolder.exists()){
			boolean created = newFolder.mkdirs();
			if( created )
				System.out.println("The new folder "+newComponentFolder+" was created");
			else{
				System.err.println("The new folder "+newComponentFolder+" could not be created");
				return;
			}
		}
				
		//instantiate the DroolsManager and set the cosmos rules
		File cosmosRulesFile = new File(cosmosRules);
		DroolsManager dm = new DroolsManager(cosmosRulesFile);
		
		//setup the transformation and fire the rules
		File jarfile = new File( jarstr );
		dm.transformationUsingJar( jarfile , newFolder, classpath, pack );
		
		
	}
	
	/**
	 * This method transforms a java source code into another java source code compliant with COSMOS model
	 * @param componentFolder the directory where the old java source code is.  
	 * @param newComponentFolder the directory where the new java source code will be placed
	 * @param pack the package of the old java source code 
	 */
	public void changeJavaSourceToCosmos(String componentFolder, String cosmosRules, 
			String newComponentFolder, String pack) {
		File folder = new File(componentFolder);
		File newFolder = new File(newComponentFolder);
		boolean created = newFolder.mkdir();
		System.out.println("conseguiu criar diretorio "+newComponentFolder+"?"+created);
		
		//if exists the component folder and the new folder could have been created
		if( ( folder.exists() ) && created ){
			//get all java files
			List javaFiles = new ArrayList();
			String filter = ".java";
			
			this.searchJavaFiles(folder,javaFiles,filter);
			
			if( javaFiles.isEmpty() ){
				System.err.println(" There is no java files in the "+folder.getAbsolutePath()+" directory.");
				return;
			}
			else{	
			
				//create component metadata
				String metadata = this.createMetadataFile(javaFiles);
				
				
				
				//instantiate the DroolsManager and set the cosmos rules
				File cosmosRulesFile = new File(cosmosRules);
				DroolsManager dm = new DroolsManager(cosmosRulesFile);
				
				//this.startTransformation(metadata,dm,newFolder,pack);
				// check if the metadata file exists
				File metafile = new File(metadata);
				if(metafile.exists()){
					//start the transformation from the file output.xml 
					dm.transformationUsingMetadata(metafile, newFolder, pack);
				}
				else
					System.err.println("output.xml does not exist");
			}
			
		}
		else{
			if(created)
				System.err.println("the component folder does not exist");
			else
				System.err.println("the chosen name already exists ");
			return;
		}
		
	}
	
	/**
	 * This method is only used when the transformation uses metadata file. This method only checks if the 
	 * metadata file exists and then call 
	 * @param metadata
	 * @param dm
	 * @param newComponent
	 * @param pack
	 */
	/*private void startTransformation(String metadata,DroolsManager dm,File newComponent,String pack){

		//check if the metadata file exists
		File metafile = new File(metadata);
		if(metafile.exists()){
			//start the transformation from the file output.xml 
			dm.transformationUsingMetadata(metafile, newComponent, pack);
		}
		else
			System.out.println("output.xml does not exist");
	}
*/	
	
	/*public static boolean checkFolder(File folder){
		//check if the component folder really exists
		
		if(folder.exists()){
			
			return true;
		}
		else
			return false;
	}*/
	
	
	/**
	 * this method search for all that ends with the 'filter' value. For example, if 'filter' is equal to 
	 * '.java', this method will search for all files in the directory and subdirectories of 'dir', and store
	 * the absolute path of all those files in the 'javaFiles' list.
	 */
	private void searchJavaFiles(File dir, List javaFiles,String filter){
		if((dir==null)||(!dir.exists()))
			return;
		else{
			File[] files = dir.listFiles();
			for(int i=0;i<files.length;i++){
				if(files[i].isDirectory())
					searchJavaFiles(files[i],javaFiles,filter);
				else{
					if(files[i].getAbsolutePath().endsWith(filter)){
						System.out.println(files[i].getAbsolutePath());
						javaFiles.add(new String(files[i].getAbsolutePath()));
					}
				}
			}
		
		}
	}
	
	/**
	 * this method pass all the files stored in the list as a input to the java2xml program.
	 * The Java2xml program will create the metadata file, with informations about all the java programs
	 * that it read.  
	 * @param javaFiles list of the java file and the input to the java2xml
	 * @return
	 */
	private String createMetadataFile(List javaFiles) {
		
		//for all java files
		System.out.println("This component has "+javaFiles.size()+" java source files.");
		String[] args = new String[javaFiles.size()];
		for(int i=0;i<javaFiles.size();i++){
			args[i] = (String) javaFiles.get(i);
			System.out.println(args[i]);
		}
		//extract metadata from all these java files and persist into output.xml
		Java2XML.main(args);
		
		System.out.println("Component metadata file created.");
		//the java2xml always create a metadata file with the same name: output.xml
		return "output.xml";	
	
		
	

	}

	
}
