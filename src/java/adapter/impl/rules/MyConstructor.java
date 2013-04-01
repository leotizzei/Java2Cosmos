package adapter.impl.rules;

import java.util.List;

public class MyConstructor {

	private String fullClassName;
	private String name;
	private String visibility;
	private List<Parameter> parameters;
	private Class[] exceptions;
	
	public Class[] getExceptions() {
		return exceptions;
	}
	public void setExceptions(Class[] exceptions) {
		this.exceptions = exceptions;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility.toLowerCase();
	}
	public String getFullClassName() {
		return fullClassName;
	}
	public void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}
	
	
}
