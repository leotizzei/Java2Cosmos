package adapter.impl.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyMethod{
	private String name;
	private List<Parameter> parameterList;
	private String returnType;
	private String visibility;
	private Class[] exceptions;
	private boolean isStatic;
	
	public boolean isStatic() {
		return isStatic;
	}


	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}


	public Class[] getExceptions() {
		return exceptions;
	}


	public void setExceptions(Class[] exceptions) {
		this.exceptions = exceptions;
	}


	public MyMethod() {
		this.parameterList = new ArrayList<Parameter>();
		
	}
	
	
	public String getVisibility() {
		return visibility;
	}


	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}


	public String getName() {
		return name;
	}
	public List<Parameter> getParameters() {
		return parameterList;
	}
	
	public void addParameter(Parameter p){
		this.parameterList.add(p);
	}
	
	public String getReturnType() {
		if( returnType.startsWith("[L")	){
			int pos = returnType.length();
			pos--;
			returnType = returnType.substring(2,pos);
			returnType = returnType.concat("[]");
		}
		return returnType;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setParameters(List<Parameter> parameter) {
		this.parameterList = parameter;
	}
	public void setReturnType(String returnType) {
		//when it is a nested class, don't use '$'
		int cifrao = returnType.indexOf("$");
		if( cifrao >= 0 ){
			returnType = returnType.replace('$', '.');
		}
		this.returnType = returnType;
	}
	
	public boolean equals(MyMethod mm){
		/*private String name;
		private List<Parameter> parameterList;
		private String returnType;
		private String visibility;*/
		
		boolean[] result = new boolean[4];
		for(int i=0;i<result.length;i++){
			result[i] = false;
		}
		
		if(this.name.equals(mm.getName()))
			result[0] = true;
		if(this.returnType.equals(mm.getReturnType()))
			result[1] = true;
		if(this.visibility.equals(mm.getVisibility()))
			result[2] = true;
		List<Parameter> paramList = mm.getParameters();
		List<Parameter> paramList2 = this.getParameters();
		Collections.sort( paramList );
		Collections.sort( paramList2 );
		if( paramList.size() == paramList2.size()){
			int j = 0;
			result[3] = true;
			while( (j < paramList.size()) && ( result[3] ) ) {
				Parameter p1 = paramList.get(j);
				Parameter p2 = paramList2.get(j);
				if( p1.equals( p2 ) )
					result[3] = true;
				else
					result[3] = false;
				j++;
			}
		}
 		boolean finalResult = result[0] && result[1] && result[2] && result[3]; 
		return finalResult;
		
	}
}
