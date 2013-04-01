package adapter.impl.rules;

import java.io.File;
import java.io.IOException;

public class CreatePackSpecReqAndProv {

	
	public boolean createSpecSubPackages(File newComponent){
		if(newComponent.exists()){
			String canPath;
			try {
				canPath = newComponent.getCanonicalPath();
				if(canPath.endsWith("/"))
					canPath = canPath + "spec/";
				else
					canPath = canPath + "/spec/";
			
				File spec = new File(canPath);
				if(spec.exists()){
					boolean reqTester = false;
					boolean provTester = false;
					File prov = new File(canPath+"prov/");
					File req = new File(canPath+"req/");
					provTester = prov.mkdir();
					reqTester = req.mkdir();
					if(provTester&&reqTester)
						return true;
					else
						return false;
				}
				else{
					System.err.println("does not exist subpackage spec");
					return false;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
		
	}
	
}
