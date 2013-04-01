package adapter.impl;

public class Hello {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Teste t = new Teste();
		t.hello();
		Teste.TesteB tb = t.new TesteB();
	}

}
