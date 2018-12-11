package deepbleu;

public class Main {
	
	static boolean AITest = true;

	public static void main(String[] args) {
		if(AITest) {
			AITester.test(10);
			return;
		} else {
			LoginListener.run(args);
		}
	}

}
