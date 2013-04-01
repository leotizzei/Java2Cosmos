/**
 * 
 */
package adapter.impl.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * @author ra001973
 *
 */
public final class AdapterClassLoader extends ClassLoader {

	public AdapterClassLoader(){
		super(AdapterClassLoader.class.getClassLoader());
	}
	
	public AdapterClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	private String componentPath;
	
	//	 $Id$
	
	/*
	A AdapterClassLoader compiles your Java source on-the-fly. It
	checks for nonexistent .class files, or .class files that are older
	than their corresponding source code.
	*/
	
	
	
	public String getComponentPath() {
		return componentPath;
	}

	public void setComponentPath(String componentPath) {
		this.componentPath = componentPath;
	}
	
	
	
	
	//	 Given a filename, read the entirety of that file from disk
	//	 and return it as a byte array.
	private byte[] getBytes( String filename ) throws IOException {
	
		//Find out the length of the file
		File file = new File( filename );
		long len = file.length();
		//Create an array that's just the right size for the file's
		//contents
		
		byte raw[] = new byte[(int)len];
		//	 Open the file
		FileInputStream fin = new FileInputStream( file );
		//	 Read all of it into the array; if we don't get all,
		//	 then it's an error.
		int r = fin.read( raw );
		if (r != len)
			throw new IOException( "Can't read all, "+r+" != "+len );
		//	 Don't forget to close the file!
		fin.close();
		//	 And finally return the file contents as an array
		return raw;
	}
	

	//	 The heart of the ClassLoader -- automatically compile
	//	 source as necessary when looking for class files
	protected Class findClass(String name) throws ClassNotFoundException {
		//	 Our goal is to get a Class object
		Class clas = null;
		//	 First, see if we've already dealt with this one
		clas = findLoadedClass( name );
		//	System.out.println( "findLoadedClass: "+clas );
		//	 Create a pathname from the class name
		//	 E.g. java.lang.Object => java/lang/Object
		
		
		String fileStub = name.replace( '.', '/' );
		
		
		
		//	 Build objects pointing to the source code (.java) and object
		//	 code (.class)
		if(!fileStub.startsWith("java"))
			fileStub = this.getComponentPath() + fileStub;
		
		String classFilename = fileStub+".class";
		
		File classFile = new File( classFilename );
		
		if( classFile.exists() )
			System.out.println(classFilename + " exists");
		//	System.out.println( "j "+javaFile.lastModified()+" c "+
		//	 classFile.lastModified() );
		//	 First, see if we want to try compiling. We do if (a) there
		//	 is source code, and either (b0) there is no object code,
		//	 or (b1) there is object code, but it's older than the source
		
	//	 Let's try to load up the raw bytes, assuming they were
	//	 properly compiled, or didn't need to be compiled
		try {
			//	 read the bytes
			byte raw[] = getBytes( classFilename );
			//	 try to turn them into a class
			clas = defineClass( name, raw, 0, raw.length );
		} catch( IOException ie ) {
			//	 This is not a failure! If we reach here, it might
			//	 mean that we are dealing with a class in a library,
			//	 such as java.lang.Object
			System.err.println(ie.getLocalizedMessage());
		} catch( NoClassDefFoundError err){
			System.err.println("NoClassDefFoundError "+err.getLocalizedMessage());
		}
		
	//	System.out.println( "defineClass: "+clas );
	//	 Maybe the class is in a library -- try loading
	//	 the normal way
		if (clas == null) {
			clas = findSystemClass( name );
		}
	//	System.out.println( "findSystemClass: "+clas );
	//	 Resolve the class, if any, but only if the "resolve"
	//	 flag is set to true
//		if ( resolve && clas != null)
//			resolveClass( clas );
		
		if (clas == null){
			ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
			clas = sysClassLoader.loadClass(name);
		}
		
		//	 If we still don't have a class, it's an error
		if (clas == null)
			throw new ClassNotFoundException( name );
	//	 Otherwise, return the class
		return clas;
	}
	
	public static void main(String[] args){
		AdapterClassLoader acl = new AdapterClassLoader();
		java.net.URL location;
		try {
			location = new URL("jar:file:///home/lsd/ra001973/javaLibraries/comons-beanutils-1.7.0/commons-beanutils-1.7.0.jar!/");
			URL[] locations = new URL[1];
			locations[0] = location;
			//java.lang.ClassLoader loader = URLClassLoader.newInstance(locations, acl.getClass().getClassLoader());
			//Class userList = loader.loadClass("org.eclipse.core.boot.IPlatformConfiguration$ISitePolicy");
			AdapterClassLoader loader = new AdapterClassLoader();
			Class clazz = loader.loadClass("de.gulden.framework.amoda.environment.ant.ANTTaskApplicationWrapper",true);
			System.out.println("[ACL] class name = "+clazz.getName()+ " ");
			Class[] classes = clazz.getClasses();
			for(int k=0;k<classes.length;k++)
				System.out.println("classes "+classes[k].getName());
			Type type = clazz.getGenericSuperclass();
			System.out.println(type.getClass().getName());
			System.out.println("canonical name = "+clazz.getCanonicalName());
			ClassLoader loader2 = (ClassLoader) clazz.getClassLoader();
			System.out.println("some classloader = "+loader2.getClass().getName());
			
			if (clazz.isInterface())
				System.out.println(clazz.getName()+" is interface");
			else
				System.out.println(clazz.getName()+" is not interface");
			Field[] fields;
			fields = clazz.getFields();
			if( fields != null){
				for(int k=0;k<fields.length;k++){
						System.out.println("field "+fields[k].getName());
				}
				
			}
			else
				System.out.println("fields is null");
			
			
			
				
			Method[] methods = clazz.getDeclaredMethods();
			if(methods == null){
				System.out.println("methods is null");
			}
			else{
				System.out.println("methods is not null");
				for(int i=0;i<methods.length;i++){
					System.out.println(methods[i].getName());
				}
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch(SecurityException e){
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		
		  
		
	}
	
}	


