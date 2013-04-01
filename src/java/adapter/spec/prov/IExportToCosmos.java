package adapter.spec.prov;


public interface IExportToCosmos {
	
	/**
	 * This method transforms a java source code into another java source code compliant with COSMOS model
	 * @param componentFolder the directory where the old java source code is.  
	 * @param baseDir the directory where the new java source code will be placed
	 */
	public void changeJavaSourceToCosmos(String componentFolder,String baseDir, String pack, String rules);
	
	/**
	 *  This method creates a wrapper to a jar file, so this jar file become compliant with COSMOS model 
	 * @param baseDir the directory where the new java source code will be placed
	 * @param jarPath the canonical path of the jar file
	 * @param pack the package of the new COSMOS component. It has the same restrictions as a package name in Java language
	 * @param classpath the canonical path to a XML file that contains all classpaths necessary to load the component
	 */
	public void changeJarToCosmos(String baseDir, String jarPath, String pack, String classpathFile, String rules);
}
