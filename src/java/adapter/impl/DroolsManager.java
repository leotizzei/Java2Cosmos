package adapter.impl;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.lang.descr.PackageDescr;
import org.drools.rule.Package;

/**
 * This is a sample file to launch a rule package from a rule source file.
 */
public class DroolsManager {
	private final File rules;
	
	public DroolsManager(File r){
		if(r.exists() && r.isFile())
			this.rules = r;
		else{
			System.err.println("Incorrect rules files. Can't instantiate DroolsManager");
			this.rules = null;
		}
	}
	
	
	/**
	 * this method assert the objects and fire the rules
	 * @param fileJar the jar that will be wrapped 
	 * @param newComponent the directory where the new component will be placed
	 * @param classpathstr an array with all the files and directories that the jar depends
	 */
	protected void transformationUsingJar(File fileJar, File newComponent,String[] classpathstr, String pack) {
        try {
        	List<String> classpath = new ArrayList();
        	for(int k=0;k<classpathstr.length;k++){
        		if( classpathstr[k] != null) 
        			classpath.add(classpathstr[k]);
        	}
        	
        	//load up the rulebase
            RuleBase ruleBase = readRule(this.rules.getAbsolutePath());
            WorkingMemory workingMemory = ruleBase.newWorkingMemory();
            
            // check if the file exists
            if( ( fileJar.exists() ) && ( fileJar.isFile() ))
            	workingMemory.assertObject( fileJar);
            else
            	System.err.println("jar file is not a file");
            if(newComponent.isDirectory())
            	workingMemory.assertObject( newComponent);
            else
            	System.err.println(newComponent+" is not a directory");


            
            /*String absolutePath = newComponent.getCanonicalPath();
            int slash;
            String pack = new String();
            //get the last directory name
            if( absolutePath.endsWith("/") ){
            	slash = absolutePath.lastIndexOf("/", (absolutePath.length() - 1));
            	slash++;
            	pack = absolutePath.substring(slash, (absolutePath.length() - 1) );
            }
            else{
            	slash = absolutePath.lastIndexOf("/", absolutePath.length() );
            	slash++;
            	pack = absolutePath.substring(slash, absolutePath.length() );
            }*/
            	
            //assert all objects and fire the rules
            workingMemory.assertObject( pack );
            workingMemory.assertObject( classpath );
            workingMemory.fireAllRules();
          
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
	
    protected void transformationUsingMetadata(File metadata, File newComponent,String pack) {
        try {
        	
        	//load up the rulebase
            RuleBase ruleBase = readRule(this.rules.getAbsolutePath());
            WorkingMemory workingMemory = ruleBase.newWorkingMemory();
            
            if(metadata.isFile())
            	workingMemory.assertObject( metadata);
            else
            	System.err.println("metadata is not a file");
            if(newComponent.isDirectory())
            	workingMemory.assertObject( newComponent);
            else
            	System.err.println(newComponent+" is not a directory");
            workingMemory.assertObject(pack);
            workingMemory.fireAllRules();
          
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Please note that this is the "low level" rule assembly API.
     */
	private static RuleBase readRule(String rules) throws Exception {
		//read in the source
		
		//ERRADO!!! substituir o cosmos.drl
		Reader source = new InputStreamReader( DroolsManager.class.getResourceAsStream( "/cosmos.drl" ) );
		
		//optionally read in the DSL (if you are using it).
		//Reader dsl = new InputStreamReader( DroolsTest.class.getResourceAsStream( "/mylang.dsl" ) );

		//Use package builder to build up a rule package.
		//An alternative lower level class called "DrlParser" can also be used...
		
		PackageBuilder builder = new PackageBuilder();

		//this wil parse and compile in one step
		//NOTE: There are 2 methods here, the one argument one is for normal DRL.
		builder.addPackageFromDrl( source );

		//Use the following instead of above if you are using a DSL:
		//builder.addPackageFromDrl( source, dsl );
		
		//get the compiled package (which is serializable)
		Package pkg = builder.getPackage();
		
		//add the package to a rulebase (deploy the rule package).
		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage( pkg );
		return ruleBase;
	}
	
	public static class Message {
		public static final int HELLO = 0;
		public static final int GOODBYE = 1;
		public static final int TESTE = 2;
		
		private String message;
		
		private int status;
		
		public String getMessage() {
			return this.message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
		public int getStatus() {
			return this.status;
		}
		
		public void setStatus( int status ) {
			this.status = status;
			
			
		}
	}
	
	
    
}
