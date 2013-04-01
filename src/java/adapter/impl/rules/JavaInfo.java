package adapter.impl.rules;

import java.util.ArrayList;
import java.util.List;

class JavaInfo implements Comparable{
	private String visibility;
	private String packagee;
	private String absolutePath;
	private List<String> importList;
	private boolean isImplemented;
	
	JavaInfo(){
		this.importList = new ArrayList<String>();
		this.isImplemented = false;
	}
	

	
	public boolean isImplemented() {
		return isImplemented;
	}



	public void setImplemented(boolean isImplemented) {
		this.isImplemented = isImplemented;
	}



	public void setPackage(String s){
		this.packagee = s;
	}
	
	public String getPackage(){
		return this.packagee;
	}
	
	public void setAbsolutePath(String s){
		this.absolutePath = s;
	}
	
	public String getAbsolutePath(){
		return this.absolutePath;
	}
	
	public void addImport(String i){
		this.importList.add(i);
	}
	
	public List<String> getImportList(){
		return this.importList;
	}

	


	public int compareTo(Object o) {
		JavaInfo imi = (JavaInfo) o;
		int result = this.absolutePath.compareTo(imi.absolutePath); 
		if( result < 0)
			return -1;
		else{
			if( result == 0 )
				return 0;
			else
				return 1;
		}
	}
	

	public String getVisibility() {
		return visibility;
	}



	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}



	public void setImportList(List<String> importList) {
		this.importList = importList;
	}

	public String getName(){
		String pack = this.packagee;
		String absolutePath = this.getAbsolutePath();
		
		if( ( pack == null ) || (absolutePath == null ) )
			return null;
		
		//remove the extension
		if( absolutePath.endsWith("java") || absolutePath.endsWith("class")){
			int point = absolutePath.lastIndexOf('.');
			absolutePath = absolutePath.substring(0, point);
		}
			
		
		absolutePath = absolutePath.replace('/', '.');
		int packagePosition = absolutePath.indexOf( pack );
		if ( packagePosition >= 0 ){
			return absolutePath.substring(packagePosition, absolutePath.length());
		}
		else
			return null;
	}
	
}
