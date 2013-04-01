package adapter.impl.rules;

import java.io.File;
import java.io.IOException;

public class CreatePackSpec {

	
	public boolean createFolderSpec(File f){
		String componentFolder;
	
		try {
			componentFolder = f.getCanonicalPath();
			componentFolder = componentFolder + "/spec";
			
			File specFolder = new File(componentFolder);
			boolean res = specFolder.mkdir();
			System.out.println("criou "+componentFolder+" ?"+res);
			return res;
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		return false;
	}
	
}
