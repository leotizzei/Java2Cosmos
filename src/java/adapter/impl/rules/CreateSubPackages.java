package adapter.impl.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CreateSubPackages {

	
	public boolean createSubPackages(File metadata,File componentFolder){
		try {
			System.out.println("Creating subpackages... ");
			if(metadata.exists()&&componentFolder.exists()){
				String implPath;
			
				implPath = componentFolder.getCanonicalPath();
				//create the impl file
				implPath = implPath.concat("/impl");
				File implFolder = new File(implPath);
				
				DOMReader dr = new DOMReader();
				Node root = dr.readFiles(metadata);
				List<JavaInfo> classes = new ArrayList<JavaInfo>();
				this.getElement(root, "package-decl", "name", classes);
				boolean canCreate = true;
				if(!classes.isEmpty()){
					//get the name of the packages
					List<String> packList = getPackagesName(classes);
			
					int i = 0;
					while((canCreate)&&(i<packList.size())){
						//for each name, create a new package
						canCreate = createPackage(packList.get(i),implFolder);
						i++;
					}
					return canCreate;
				
				}
				else{
					System.err.println("There are no packages");
					return false;
				}
			}
			else{
				System.err.println("Either metadata or component folder doesn't exists");
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
			
	}
	
	/**
	 * 
	 * @param name the name of the package
	 * @param implFolder the directory where the new package is going to be placed
	 * @return
	 */
	private boolean createPackage(String name,File implFolder){
		try {
			String canonicalPath = implFolder.getCanonicalPath();
			canonicalPath = canonicalPath.concat("/"+name);
			File newSubPackage = new File(canonicalPath);
			boolean created = false;
			if(!newSubPackage.exists())
				created = newSubPackage.mkdirs();
			else
				created = true;
			
			return created;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	/**
	 * this method returns a list with the names of the packages and subpackages of the component
	 * @param classes all the packages and subpackages of the component
	 * @return
	 */
	private List<String> getPackagesName(List<JavaInfo> classes){
		List<String> packList = new ArrayList<String>();
		for(int i=0;i<classes.size();i++){
			
			JavaInfo javainfo = classes.get(i);
			String pack = javainfo.getPackage();
			pack = pack.replaceAll("\\.", "/");
			//System.out.println("pack = "+pack+" "+packList.contains(pack));
			if((pack!=null)&&(!packList.contains(pack)))
				packList.add(pack);
		}
		//System.out.println("packList size = "+packList.size());
		return packList;
	}
	
	/**
	 * 
	 * @param n the current node
	 * @param element the name of the element you are looking for
	 * @param attribute the attribute you are looking for
	 * @param files a list to place the result
	 */
	protected void getElement(Node n,String element,String attribute,List<JavaInfo> files){
		if(n==null)
			return;
		else{
			if(n.getNodeName().equalsIgnoreCase(element)){
				NamedNodeMap nnm = n.getAttributes();
				for(int i=0;i<nnm.getLength();i++){
					Node att = nnm.item(i);
					String nodeName = att.getNodeName();
					if(nodeName.equals(attribute)){
						String nodeValue = att.getNodeValue();
						JavaInfo classInfo = new JavaInfo();
						classInfo.setPackage(nodeValue);
						files.add(classInfo);
					}
				}
			}
			getElement(n.getFirstChild(),element,attribute,files);
			getElement(n.getNextSibling(),element,attribute,files);
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
	
}
