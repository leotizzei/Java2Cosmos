package adapter.impl;

import java.io.File;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import adapter.spec.prov.IExportToCosmos;

public class Facade implements IExportToCosmos{
	//private final String COSMOSRULES = "/home/lsd/ra001973/workspace2/adapter/bin/cosmos.drl";
	
	
	public static void main(String[] args){
		long time = System.currentTimeMillis();
		Facade f = new Facade();
		System.out.println("Exporting to COSMOS... ");
		String dir = "/home/lsd/ra001973/workspace2/wmswm2007/cosmospetstore201";
		String jarFile = "/home/lsd/ra001973/workspace2/wmswm2007/petstore201/captcha201.jar"; 
		String pack = "com.sun.javaee.blueprints.petstore.captcha";
		String classpathFile = "/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/ajax-wrapper-comp.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/bp-dynamic-text-comp.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/bp-ui-14.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/bp-ui-5.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/commons-fileupload-1.1.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/commons-io-1.1.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/commons-logging.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/lucene-1.4.3.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/lib/shale-remoting.jar:/home/lsd/ra001973/programs/PetStore/petstore2_0_ea1/javapetstore-2.0-ea1/src/java/captcha201.jar:/home/lsd/ra001973/eclipse3_2_1/eclipse/plugins/org.eclipse.tomcat_4.1.130.v20060601/servlet.jar:.";
		String rules = "/home/lsd/ra001973/workspace2/Java2Cosmos/src/rules/cosmos.drl";
		//String componentFolder = "/home/lsd/ra001973/workspace2/wmswm2007/petstore201/com/sun/javaee/blueprints/petstore/captcha";
		//String newComponentFolder = "/home/lsd/ra001973/workspace2/wmswm2007/cosmospetstore201";
		/*String[] cp = f.getClasspath( classpathFile );
		if( ( cp != null ) && ( cp.length == 0 ) )
			System.err.println("Check the classpath!");
		else		
			for(int i = 0 ; i < cp.length; i++)
				System.out.println("cp[ " + i + " ] = " + cp[i]);
		*/
		
		f.changeJarToCosmos(dir, jarFile, pack , classpathFile, rules);
		//f.changeJavaSourceToCosmos( componentFolder, newComponentFolder, pack, rules);
		System.out.println("tempo:"+( ( double ) ( System.currentTimeMillis() - time ) / 1000 ) + " segundos");
	}
	
	
	/**
	 *  This method creates a wrapper to a jar file, so this jar file become compliant with COSMOS model 
	 * @param newComponentFolder the directory where the new java source code will be placed
	 * @param jarPath the canonical path of the jar file
	 * @param classpath the canonical path of all files that the jar depends
	 */
	public void changeJarToCosmos(String baseDir, String jarPath, String pack, String classpathFile, String rules) {
		//check if the component folder is not null
		if( baseDir == null){
			System.err.println("The new component directory can not be null");
			return;
		}
		else{
			if( !baseDir.endsWith( File.separator ) )
				baseDir += File.separator;
		}
		
		//check if  jar file exists
		if( jarPath != null){
			File jarPathFile = new File( jarPath );
			if( !jarPathFile.exists() ){
				System.err.println("The path to the jar file is not correct");
				return;
			}
		}
		else{
			System.err.println("The path to the jar file is null");
			return;
		}
		
		String newPack = this.removeWhiteSpaces( pack );
		if( !this.isValidPackage( newPack ) ){
			System.err.println("The package is not valid");
			return;
		}
			
		System.out.println("Starting conversion to COSMOS...");
		
		Control c = new Control();
		//String[] classpath = this.getClasspath( classpathFile );
		String[] classpath = classpathFile.split( File.pathSeparator );
		for(int i = 0 ; i < classpath.length; i++){
			File f = new File( classpath[ i ] );
			if( f.exists() )
				System.out.println("[ "+i+"] = "+classpath[ i ] + " exist");
			else
				System.out.println("[ "+i+"] = "+classpath[ i ] + "doesn't exist");
		}
		
		//String[] classpath = new String[1];
		//classpath[ 0 ] = "/home/lsd/ra001973/eclipse/eclipse/plugins/org.eclipse.tomcat_4.1.30.1/servlet.jar";
		c.changeJarToCosmos(jarPath, rules, baseDir, classpath , newPack);
		
	}

	/**
	 * This method transforms a java source code into another java source code compliant with COSMOS model
	 * @param componentFolder the directory where the old java source code is.  
	 * @param newComponentFolder the directory where the new java source code will be placed
	 * @param pack the package of the old java source code 
	 */
	public void changeJavaSourceToCosmos(String componentFolder, String newComponentFolder, String pack, String rules) {
		System.out.println("Starting transformation...");
		File f = new File(".");
		
		System.out.println(f.getAbsolutePath());
		Control c = new Control();
		c.changeJavaSourceToCosmos(componentFolder, rules , newComponentFolder, pack);
		
	}

	/**
	 * create a list with all parameters in the classpath
	 * @param filePath
	 * @return
	 */
	private String[] getClasspath(String filePath){
		if( filePath != null ){
			File f = new File( filePath );
			if( f.exists() ){
				XPathHelper xpath = new XPathHelper( f );
				String expression = "//java2cosmos/classpath";

				//get a list of all parameters in classpath
				NodeList nodelist = xpath.getNodeList(expression);

				//for each parameter
				if( nodelist != null){
					String[] classpath = new String[ nodelist.getLength() ]; 
					for(int i = 0 ; i < nodelist.getLength() ; i++){
						Node n = nodelist.item( i );

						// add the parameter in classpath
						String aux = n.getTextContent();
						classpath[ i ] = removeWhiteSpaces( aux );   
					}
					return classpath;
				}
			}
			
		}
		System.err.println("Can't read the classpath file");
		return null;
	}
	
	private String removeWhiteSpaces(String s){
		if( s != null){
			char[] c = s.toCharArray();
			
			//read until find the beginning of the package
			int i = 0 ;
			while( ( i < c.length ) && ( !Character.isLetterOrDigit( c[i] ) ) 
					&& ( c[ i ] != File.separatorChar ) && ( c[ i ] != '.') )
				i++;
			
			//save the package in the variable
			String res = "";
			while( ( i < c.length ) && ( !Character.isWhitespace( c[i] ) ) ){
				res += c[ i ];
				System.out.print(c[i]);
				i++;
			}
			System.out.println();	
			return res;
			
		}
		return null;
	}
	
	private boolean isValidPackage(String pack){
		if( pack == null)
			return false;
		else{
			char[] charPack = pack.toCharArray();
			int i = 0;
			while( i < charPack.length ){
				if(  ( Character.isLetterOrDigit( charPack[ i ] ) ) || ( charPack[ i ] == '.' ) );
				else{
					System.err.println("Invalid character = " + charPack[ i ]);
					return false;
				}
				i++;
			}
			return true;
				
		}
	}
	
}
