package adapter.impl.rules;

import java.util.ArrayList;
import java.util.List;

import adapter.impl.jarFiles.ClassMetaInfo;


/**
 * 
 * @author Leonardo Pondian Tizzei
 *
 * Although a interface doesn't have constructors, our interface metainfo store
 * information about the class that implements it. It seens weird but with reflection
 * we can't have all these information together. This is specially useful when we are
 * creating interfaces from the public classes.
 * 
 *
 */
public class InterfaceMetaInfo implements Comparable{

	private List<MyField> fields;
	private List<MyMethod> methods;
	private List<ClassMetaInfo> implementedBy;
	private String interfaceName;
	private String visibility;
	private String packag;
	private List<String> importList;
	private List<MyConstructor> constructors;
	
	
	public boolean isImplemented(){
		List<ClassMetaInfo> implementedBy = this.getImplementedBy();
		if( ( implementedBy == null ) || ( implementedBy.isEmpty() ) )
			return false;
		else
			return true;
	}
	
	public InterfaceMetaInfo() {
		//default visibility is public
		this.visibility = "public";
		this.fields = new ArrayList<MyField>();
		this.methods = new ArrayList<MyMethod>();
		this.implementedBy = new ArrayList<ClassMetaInfo>();
	}

	
	
	public List<String> getImportList() {
		return importList;
	}

	public void setImportList(List<String> importList) {
		this.importList = importList;
	}

	public String getPackag() {
		return packag;
	}

	public void setPackag(String packag) {
		this.packag = packag;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String vis) {
		
		this.visibility = vis.toLowerCase();
	}

	
	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		// when the name is a nested class, replace '$' by '.'
		int cifrao = interfaceName.indexOf("$");
		if( cifrao > 0 ){
			interfaceName = interfaceName.replace('$', '_');
		}
		this.interfaceName = interfaceName;
	}

	public List<ClassMetaInfo> getImplementedBy() {
		return implementedBy;
	}
	
	public void setImplementedBy(List<ClassMetaInfo> implementedBy) {
		this.implementedBy = implementedBy;
	}
	
	public void addImplementedBy(ClassMetaInfo classs){
		this.implementedBy.add(classs);
	}
	
	public List<MyField> getFields() {
		return fields;
	}
	public List<MyMethod> getMethods() {
		return methods;
	}
	
	public void setFields(List<MyField> fields) {
		this.fields = fields;
	}
	public void setMethods(List<MyMethod> methods) {
		this.methods = methods;
	}
	
	public void addField(MyField field){
		this.fields.add(field);
	}

	public int compareTo(Object o) {
		InterfaceMetaInfo imi = (InterfaceMetaInfo) o;
		int result = this.interfaceName.compareTo(imi.interfaceName);
		if( result < 0)
			return -1;
		else{
			if( result == 0 )
				return 0;
			else
				return 1;
		}
	}

	public List<MyConstructor> getConstructors() {
		return constructors;
	}

	public void setConstructors(List<MyConstructor> constructors) {
		this.constructors = constructors;
	}

	
	
	
	
}





