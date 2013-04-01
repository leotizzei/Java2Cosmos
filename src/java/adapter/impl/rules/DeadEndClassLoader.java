package adapter.impl.rules;

public class DeadEndClassLoader extends ClassLoader {
	
	public DeadEndClassLoader() {
		super(null);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException("I refuse to load class " + name);
	}
}
