package adapter.impl;

import adapter.spec.prov.IManager;



public class ComponentFactory {
	public static IManager createInstance(){
		
		IManager im = new Manager();
		return im;
	}
	
	
	
}
