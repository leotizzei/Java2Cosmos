package adapter.impl.rules;

import java.io.File;

import org.w3c.dom.Node;

public class ConfigurationManager {

	private String configuration = "./src/conf/adapterConfiguration.xml";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigurationManager cm = new ConfigurationManager();
		cm.manager();

	}
	
	public void manager(){
		System.out.println("Loading the configuration file...");
		DOMReader dr = new DOMReader();
		File confFile = new File(this.configuration);
		Node root = dr.readFiles(confFile);
		this.traverse( root, 0 );
	}
	
	private void traverse(Node n,int deep){
		if( n == null ){
			return;
		}
		else{
			for(int i=0;i<deep;i++)
				System.out.print("    ");
			System.out.println(n.getNodeName());
			traverse(n.getFirstChild(), (deep+1) );
			traverse(n.getNextSibling(), deep);
		}
			
	}

}
