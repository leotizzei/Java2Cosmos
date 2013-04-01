package adapter.impl.rules;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.Attributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A class loader for loading jar files, both local and remote.
 */
public class JarClassLoader extends URLClassLoader {
    private URL url;
    private String temp = "/home/lsd/ra001973/workspace2/adapter/temp/";

    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
     */
    public JarClassLoader(URL url) {
        super(new URL[] { url });
        this.url = url;
    }

    public JarClassLoader(URL jar, ClassLoader parent) {
        super(new URL[] { jar },parent);
        
    }
  
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
			fileStub = this.temp + fileStub;
		
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
		/*if ( resolve && clas != null)
			resolveClass( clas );*/
		
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
	
    
    
    /**
     * Returns the name of the jar file main class, or null if
     * no "Main-Class" manifest attributes was defined.
     */
    public String getMainClassName() throws IOException {
        URL u = new URL("jar", "", url + "!/");
        JarURLConnection uc = (JarURLConnection)u.openConnection();
        Attributes attr = uc.getMainAttributes();
        return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
    }

    /**
     * Invokes the application in this jar file given the name of the
     * main class and an array of arguments. The class must define a
     * static method "main" which takes an array of String arguemtns
     * and is of return type "void".
     *
     * @param name the name of the main class
     * @param args the arguments for the application
     * @exception ClassNotFoundException if the specified class could not
     *            be found
     * @exception NoSuchMethodException if the specified class does not
     *            contain a "main" method
     * @exception InvocationTargetException if the application raised an
     *            exception
     */
    public void invokeClass(String name, String[] args)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InvocationTargetException
    {
        Class c = loadClass(name);
        Method m = c.getMethod("main", new Class[] { args.getClass() });
        m.setAccessible(true);
        int mods = m.getModifiers();
        if (m.getReturnType() != void.class || !Modifier.isStatic(mods) ||
            !Modifier.isPublic(mods)) {
            throw new NoSuchMethodException("main");
        }
        try {
            m.invoke(null, new Object[] { args });
        } catch (IllegalAccessException e) {
            // This should not happen, as we have disabled access checks
        }
    }

}



