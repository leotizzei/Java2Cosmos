package adapter.impl.rules;

public class Parameter implements Comparable{
	private String name;
	private int position;
	private String type;
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setType(String type) {
		//when it is a nested class, don't use '$'
		int cifrao = type.indexOf("$");
		if( cifrao >= 0 ){
			type = type.replace('$', '.');
		}
		this.type = type;
	}

	public int compareTo(Object o) {
		Parameter p = (Parameter) o;
		if( this.position < p.position)
			return -1;
		else
			return 1;
		
	}
	
	public boolean equals(Parameter p){
		
		boolean[] res = new boolean[3];
		if( this.name.equals( p.getName() ))
			res[0] = true;
		if( this.position == p.getPosition())
			res[1] = true;
		if( this.type.equals(p.getType()))
			res[2] = true;
		boolean finalResult = res[0] && res[1] && res[2];
		return finalResult;
	}
}
