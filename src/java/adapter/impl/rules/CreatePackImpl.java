package adapter.impl.rules;

import java.io.File;
import java.io.IOException;

public class CreatePackImpl {

	
	public boolean createFolderImpl(File f){
		String componentFolder;
	
		try {
			componentFolder = f.getCanonicalPath();
			componentFolder = componentFolder + "/impl";
			
			File specFolder = new File(componentFolder);
			boolean res = specFolder.mkdir();
			System.out.println("Was the directory "+componentFolder+" created? Answer: "+res);
			return res;
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		return false;
	}
	
}
