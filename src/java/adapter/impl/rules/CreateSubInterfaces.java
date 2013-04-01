package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CreateSubInterfaces {

	
	public boolean createSubInterfaces(File componentFolder,File metadata,String newPackage){
		try {
			System.out.println("creating subinterfaces...");
			String canonicalPath = componentFolder.getCanonicalPath();
			String implPath = canonicalPath+"/impl";
		
			boolean result = true;
			result = this.copyFiles(canonicalPath.concat("/spec/prov"), metadata,implPath,newPackage+".impl");
			if(result)
				result = this.copyFiles(canonicalPath.concat("/spec/req"), metadata,implPath,newPackage+".impl");
			if(result)
				return true;
			else
				return false;
			
			
				
		
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
			System.out.println("Something is wrong with the class name");
			result = "ERROR";
		}
		else
			//get only the interface name and its extension
			result = path.substring(pos, path.length());
		return result;
	}
	
	private boolean copyFiles(String path,File metadata,String implDirPath,String newPackage){
		File dir = new File(path);
		File[] list = dir.listFiles();
		boolean result;
		if(list.length == 0)
			result = true;
		else
			result = false;
		try {
			for(int i=0;i<list.length;i++){
				if(list[i].isFile()){
					/*String newPack = getPackage(metadata);
					String aux = newPack.replaceAll("\\.", "/");
					newPack = newPackage + "." + newPack;
					*/
					String destPath = implDirPath+"/";
					//System.out.println("implDirPath = ""aux = "+aux+" implDirPath = "+implDirPath);
					File destFile = new File(destPath);
					if((destFile.createNewFile())&&(destFile.exists()))
						result = this.copy2(list[i], destFile, newPackage);
				}
				if(!result)
					return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * get the package reading the metadata
	 * @param metadata
	 * @return
	 */
	private String getPackage(File metadata){
		DOMReader dr = new DOMReader();
		Node root = dr.readFiles(metadata);
		List<String> packs = new ArrayList<String>(); 
		this.getElement(root, "interface",packs);
		if(packs.isEmpty())
			return null;
		else
			return packs.get(0);
	}
	
	
	protected void getElement(Node n,String element,List<String> packs){
		if(n==null)
			return;
		else{
			if(n.getNodeName().equalsIgnoreCase(element)){
				Node sibling = n.getPreviousSibling();
				while((sibling!=null)&&(!sibling.getNodeName().equalsIgnoreCase("package-decl")))
					sibling = sibling.getPreviousSibling();
				if(sibling != null){
					String packName = this.getAttribute(sibling, "name");
					packs.add(packName);
				}
			}
			getElement(n.getFirstChild(),element,packs);
			getElement(n.getNextSibling(),element,packs);
			return;
		}
	}
	
	protected String getAttribute(Node n,String attName){
		NamedNodeMap nnm = n.getAttributes();
		for(int i=0;i<nnm.getLength();i++){
			Node aux = nnm.item(i);
			String auxName = aux.getNodeName();
			if(auxName.equalsIgnoreCase(attName)){
				return aux.getNodeValue();
			}
		}
		return null;
	}
	
	
	
	
	private String changeInterface(String file,String newPackage){
		Util util = new Util();
		String oldPack = this.getPackage(file);
		//change the package, StringPosition save the position of the string that will be replaced
		StringPosition sp = util.readPackage(file);
		String packReplacement = "package "+newPackage+";";
		if(sp != null)
			file = util.replace(packReplacement, file, sp.beginString, sp.endString);
		
		//add an implemented class
		sp = util.getImplementsPosition(file);
		int extend = file.indexOf(" extends ");
		String interfaceFullName = oldPack + util.getInterfaceName(file);
		String implementReplacement;
		if( extend >= 0){
			implementReplacement = ", "+interfaceFullName+" {"; 
		}
		else{
			implementReplacement = " extends "+interfaceFullName+" {";
		}
		 
		if(sp != null)
			file = util.replace(implementReplacement, file, sp.beginString, sp.endString);
		
		return file;
	}
	
	protected boolean copy2(File source, File destination, String newPackage){
//		if both source and destination file exists
		if(source.exists()&&destination.exists()){
			//System.out.println("copying the files...");
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
				
				String facadeStr = this.changeInterface(file, newPackage);
				
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
	
	protected boolean copy(File source, File destination, String newPackage){
		//if both source and destination file exists
		if(source.exists()&&destination.exists()){
			System.out.println("copying the files...");
			try {
						
				//infra-structure classes
				Util util = new Util();
				BufferedReader in =     new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				String s = new String();
				
					
				String oldPack = null; 
				while((s = in.readLine())!= null){
					//if a string 'package' was found, change the old package to the new one
					int i = s.indexOf("package ");
					
					if(i>=0){
						oldPack = this.getPackage(s);
						s = this.changePackage(s, "package "+newPackage+";");
					}
					
					//add extends
					int j = s.indexOf(" interface ");
					if(j>=0){
						s = this.changeInterfaceAttribute(s,oldPack);
					}
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
	/**
	 *  this method extends the interface that was placed in the specs folders.
	 * @param s the line read
	 * @param oldPack the package of the interface that will be extended
	 * @return
	 */
	private String changeInterfaceAttribute(String s, String oldPack){
		int i = s.indexOf("interface ");
		
		i += "interface".length();
		//read white spaces
		while(s.charAt(i)==' '){
			//System.out.print(s.charAt(i));
			i++;
		}
		
		int j = i;
		char c = s.charAt(i);
		
		//read the interface name
		while((c!=' ')&&(c!='{')){
			
			i++;
			c = s.charAt(i);
		}
		//get the interface name
		String interfName = s.substring(j, i);
		
		//read white space
		while(c==' '){
			i++;
			c = s.charAt(i);
		}
		//create the extends [package.interface] and put that in the line
		if(c=='{'){
			String newStr = "extends "+oldPack+interfName+" {";
			String result = this.insertString(s, newStr, i);
			return result;
		}
		return null;
		
		
	}
	
	
	private String insertString(String line,String newStr,int pos){
		String begin = line.substring(0, pos);
		String middle = begin.concat(newStr);
		pos++;
		String end = middle.concat(line.substring(pos, line.length()));
		return end;
	}
	
	/**
	 * get the package of the class that is beeing copied
	 * @param s
	 * @return
	 */
	private String getPackage(String s){
		int packPos = s.indexOf("package ");
		if(packPos>=0){
			int packageTam = "package ".length();
			String pack = s.substring(packageTam, s.length());
			int i = 0 ;
			while(s.charAt(i) == ' ')
				i++;
			int semicolon = pack.indexOf(";");
			if((semicolon>=0)&&(i<semicolon)){
				//semicolon--;
				pack = pack.substring(i,semicolon)+".";
			}
			else
				pack = pack.substring(i,pack.length())+".";
			return pack;
		}
		else
			return null;
	}
	
	
	private String changePackage(String packageLine,String newPackage){
		int i = packageLine.indexOf("package ");
		if(i>=0){
			
			int packend = packageLine.indexOf(";", i);
			packend++;
			String pack = packageLine.substring(i, packend);
			String res = packageLine.replaceFirst(pack, newPackage);
			return res;
		}
		else{
			System.out.println("there is no package");
			return packageLine;
		}
	
			
	}
	
}
