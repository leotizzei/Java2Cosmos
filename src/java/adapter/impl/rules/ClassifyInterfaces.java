package adapter.impl.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

public class ClassifyInterfaces {

	public static void main(String args[]){
		ClassifyInterfaces ci = new ClassifyInterfaces();
		File out = new File("output.xml");
		boolean res = false;
		File comp =new File("/home/lsd/ra001973/workspace2/adapter/bin/newComponent/");
		
		if(out.exists())
			 res = ci.classifyInterfaces(comp,out,args[2]);
			
	}
	
	
	
	public boolean classifyInterfaces(File newComponent,File file,String pack){
		
		try {
			DOMReader dr = new DOMReader();
			Node root = dr.readFiles(file);
			
			System.out.println("starting classifyInterfaces...");
			//save the list of interfaces
			List<JavaInfo> interfaceList = new ArrayList<JavaInfo>();
		
			//define some filters, for example: visibility public
			HashMap<String,String> filters = new HashMap<String,String>();
			filters.put("visibility", "public");
		
			//get the interface list
			dr.fillJavaInfo(root, "interface", interfaceList);
			
			
			List<JavaInfo> provInterfaces = new ArrayList<JavaInfo>();
			List<JavaInfo> reqInterfaces = new ArrayList<JavaInfo>();
			List<JavaInfo> nonPublicInterfaces = new ArrayList<JavaInfo>();
			
			//get provided interfaces
			dr.getProvidedInterfaces(root, "implement", provInterfaces);
			//System.out.println("antes do set");
			//this.printList(interfaceList);
			this.setProvidedInterfaces(interfaceList, provInterfaces);
			//System.out.println("depois do set");
			//this.printList(interfaceList);
			provInterfaces = new ArrayList<JavaInfo>();
					
			String specProvPath = newComponent.getCanonicalPath()+"/spec/prov/";
			String specReqPath = newComponent.getCanonicalPath()+"/spec/req/";
			String impl = newComponent.getCanonicalPath()+"/impl/";
			
			File newComponentSpecProvFolder = new File(specProvPath);
			File newComponentSpecReqFolder = new File(specReqPath);
		
			this.splitInterfaces(interfaceList, provInterfaces, reqInterfaces,nonPublicInterfaces);
			
		
			//move the interfaces on this list to spec directory
			boolean res1 = this.setupChange(provInterfaces,pack+".spec.prov",newComponentSpecProvFolder);
			boolean res2 = this.setupChange(reqInterfaces, pack+".spec.req", newComponentSpecReqFolder);
			boolean res3 = false;
			for(int t = 0; t<nonPublicInterfaces.size();t++){
				JavaInfo privInterface = nonPublicInterfaces.get(t);
				String packag = pack + ".impl";
				File newComponentImplFolder = new File(impl	);
				System.out.println("nonPublicInterfaces["+t+"]="+impl);
				if( newComponentImplFolder.exists() )
					res3 = this.moveInterfaces(newComponentImplFolder, privInterface,packag);
				else
					System.err.println("the package does not exist");
			}
			return (res1 && res2 && res3);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * this method set the implemented attribute of the interfaces, if the interface is implemented
	 * @param interfaces list of all interfaces
	 * @param prov list of the provided interfaces
	 */
	private void setProvidedInterfaces(List<JavaInfo> interfaces,List<JavaInfo> prov){
	
		for(int i=0;i<prov.size();i++){
			String provName = "/"+prov.get(i).getAbsolutePath()+".java";
			for(int j=0;j<interfaces.size();j++){
				JavaInfo javainfo = interfaces.get(j);
				String interfaceName = this.getInterfaceLastName(javainfo.getAbsolutePath());
				if( interfaceName.equals(provName) ){
					JavaInfo ji = new JavaInfo();
					ji.setAbsolutePath(interfaces.get(j).getAbsolutePath());
					ji.setPackage(interfaces.get(j).getPackage());
					ji.setVisibility(interfaces.get(j).getVisibility());
					ji.setImportList(interfaces.get(j).getImportList());
					ji.setImplemented(true);
					
					interfaces.remove(j);
					interfaces.add(ji);
				}
			}
		}
	
	}
	
	private boolean setupChange(List<JavaInfo> interfaces,String packag,File destinationFolder){
		for(int t = 0; t<interfaces.size();t++){
			JavaInfo ji = interfaces.get(t);
			boolean res = false;
			if( destinationFolder.exists() )
				res = this.moveInterfaces(destinationFolder, ji ,packag);
			if(!res)
				return false;
			
		}
		return true;
	}
	
	private String getOnlyInterfaceName(JavaInfo interfaceInfo){
		String path = interfaceInfo.getAbsolutePath();
		int posslash = path.lastIndexOf("/");
		if(posslash<0)
			posslash = 0;
		else
			posslash++;
		int point = path.lastIndexOf(".");
		//System.out.println(path+" "+posslash+" "+point);
		String res  = path.substring(posslash, point);
		return res;
	}
	
	private boolean isProvidedInterface(JavaInfo interfaces,List<JavaInfo> prov){
		boolean result = false;
		for(int j=0;j<prov.size();j++){
			String aux = this.getOnlyInterfaceName(interfaces);
			//if the provided interface name is the same of the interface name and the interface is public 
			if( (aux.equals(prov.get(j).getAbsolutePath()) && (interfaces.getVisibility().equals("public")))){
				//change the last name of the interface for the whole path  
				prov.remove(j);
				prov.add(interfaces);
				result = true;
			}
		}
		return result;
	}
	
	
	private void splitInterfaces(List<JavaInfo> interfaces,List<JavaInfo> prov,List<JavaInfo> req,
			List<JavaInfo> nonPublic){
		//prov = new ArrayList<JavaInfo>();
		//boolean isRequired = true;
		//for all interfaces
		//System.out.println("antes");
		//this.printList(prov);
		for(int i=0;i<interfaces.size();i++){
			//if(this.isProvidedInterface(interfaces.get(i), prov))
				//do nothing, because the interface was already added
			JavaInfo ji = interfaces.get(i);
	
			if((ji.getVisibility().equals("public")) && (ji.isImplemented()))
				prov.add(ji);
			else{
				String interfaceVisibility = ji.getVisibility();
				//add required interfaces
				if( interfaceVisibility.equals("public") )
					req.add(interfaces.get(i));
				else
					//add not-required and not-provided interfaces
					nonPublic.add(interfaces.get(i));
			}
		}
		//System.out.println("depois");
		//this.printList(prov);
		
	}
	
	
	private String getInterfaceLastName(String path){
		int pos = path.lastIndexOf("/");
		String result;
		if(pos<1){
			System.err.println("Something is wrong with the interface name");
			result = "ERROR";
		}
		else
			//get only the interface name and its extension
			result = path.substring(pos, path.length());
		return result;
	}
	
	/*
	 * private boolean moveInterfaces(File destinationFolder,List<JavaInfo> interfaceList,String pack){
		try {
			
		
			if(destinationFolder.exists()){
				boolean copied = false;
				for(int i=0;i<interfaceList.size();i++){
					copied = false;
					JavaInfo interfaceInfo = interfaceList.get(i);
					String path = interfaceInfo.getAbsolutePath();
					File source = new File(path);
					if(source.exists()){
						String lastName = this.getInterfaceLastName(path);
						String destinationPath = destinationFolder.getCanonicalPath();
						File dest = new File(destinationPath +lastName);
				
						if(dest.createNewFile()){
							//String pack = interfaceList.get(i).getPackage();
							copied = this.copy(source, dest,pack);
							if(copied)
								System.out.println("The file "+source.getAbsolutePath()+" was copied to "+dest.getAbsolutePath());
							else	
								System.out.println("The system can not copy "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
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
	 * */
	
	
	private boolean moveInterfaces(File destinationFolder,JavaInfo interfaceInfo,String pack){
		try {
			if(destinationFolder.exists()){
				boolean copied = false;
				copied = false;
				String path = interfaceInfo.getAbsolutePath();
				File source = new File(path);
				if(source.exists()){
					String lastName = this.getInterfaceLastName(path);
					String destinationPath = destinationFolder.getCanonicalPath();
					File dest = new File(destinationPath +lastName);
					if(dest.createNewFile()){
						copied = this.copy(source, dest,pack);
						if(copied)
							System.out.println("The file "+source.getAbsolutePath()+" was copied to "+dest.getAbsolutePath());
						else	
							System.err.println("The system can not copy "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
					}
					else
						System.err.println("It was not possible to create "+dest.getAbsolutePath());
				
				}
				else	
					System.err.println("The system can't copy "+source.getAbsolutePath());
			
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
	
	private boolean copy2(File source, File destination,String pack){
		
		if(source.exists()&&destination.exists()){
			try {
				Util util = new Util();
				BufferedReader in =     new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				
				String s = new String();
				while((s = in.readLine())!= null){
					
					int i = s.indexOf("package ");
					
					if(i>=0){
						s = util.changePackage(s, "package "+pack+";");
						//System.out.println("achou o pacote!! s = "+s);
					}
					s = s + "\n";				
					out.write(s);
				}
				in.close();
				out.close();
				//System.out.println("s ="+s2+":fim");
				
				      
				      
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
			//System.out.println("copying the files...");
			try {
				//infra-structure classes
				//Util util = new Util();
				BufferedReader in =     new BufferedReader(new FileReader(source.getCanonicalPath()));
				FileWriter fw = new FileWriter(destination.getCanonicalPath());
				BufferedWriter out = new BufferedWriter(fw);    
				Util util = new Util();
				
				long fileSizeLong = source.length();
				int fileSize = Integer.parseInt(String.valueOf(fileSizeLong));
				char[] cbuf = new char[fileSize];
				in.read(cbuf, 0, fileSize);
				String file = String.valueOf(cbuf);
				
				//change the package, StringPosition save the position of the string that will be replaced
				StringPosition sp = util.readPackage(file);
				String packReplacement = "package "+newPackage+";";
				if(sp != null)
					file = util.replace(packReplacement, file, sp.beginString, sp.endString);
				
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
	
	private void printList(List<JavaInfo> list){
		for(int i=0;i<list.size();i++){
			JavaInfo ji = list.get(i);
			System.out.println("absolutePath = "+ji.getAbsolutePath()+
					"\nPackage = "+ji.getPackage()+"\nVisibility = "+ji.getVisibility()+
					"\n isImplemented = "+ji.isImplemented());
		}
	}
	
}
