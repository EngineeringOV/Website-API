/*
 * Example usage
 * To compile:
 * javac -cp target/*.jar example.java
 *
 * To run:
 * java  -Djava.library.path=lib   example
 * 
 */
import org.dfdeshom.math.*;

class example {
    public static void main(String[] args) {
        //Properties p = System.getProperties();
        //p.list(System.out);
	GMP a = new GMP("243454656");
	GMP b = new GMP("165758648758768");
	long startTime=System.currentTimeMillis();
	for (int i=0;i<20000;i++) {
		a.multiply(b,a);
	    }
	System.out.println("Mulitiplying  2 numbers 20000 times took " + ((System.currentTimeMillis()-startTime))+ "  milliseconds");

    }
}
