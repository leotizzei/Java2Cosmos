package adapter.impl;

import java.util.HashMap;

import adapter.spec.prov.IExportToCosmos;
import adapter.spec.prov.IManager;




class Manager implements IManager {

	private HashMap<String,Object> providedInterfaces;
	private HashMap<String,Object> requiredInterfaces;
	private String[] listReqInt;
	private String[] listProvInt;
	
	Manager(){
		this.providedInterfaces = new HashMap<String,Object>();
		this.requiredInterfaces = new HashMap<String,Object>();
		/*public interface is instanciated by the Facade*/
		IExportToCosmos icc =  new Facade();
		
		/*this interface is set as provided*/
		this.setProvidedInterface("IExportToCosmos",icc);
		
		/*create a list with interface names and set as provided interfaces */
		String[] listProv = new String[1];
		listProv[0] = "IExportToCosmos";
		this.setProvidedInterfaces(listProv);
		this.setRequiredInterfaces(null);
		
	}
	
	public String[] getProvidedInterfaces() {

		return this.listProvInt;
	}

	public void setProvidedInterface(String typeName, Object facade) {
		this.providedInterfaces.put(typeName,facade);

	}

	public void setProvidedInterfaces(String[] interfaces) {
		this.listProvInt = interfaces;

	}

	public String[] getRequiredInterfaces() {
		return this.listReqInt;
	}

	public Object getProvidedInterface(String name) {
		
		return this.providedInterfaces.get(name);
	}

	public void setRequiredInterface(String name, Object facade) {
		this.requiredInterfaces.put(name,facade);

	}

	public void setRequiredInterfaces(String[] interfaces) {
		this.listReqInt = interfaces;

	}

	public Object getRequiredInterface(String name) {
		
		return this.requiredInterfaces.get(name);
	}

	
	
}
