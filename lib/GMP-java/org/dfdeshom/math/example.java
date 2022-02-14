import java.util.*;
import org.dfdeshom.math.*;

class example {
    public static void main(String[] args) {
        //String s = System.getproperty("java.classpath");
        //System.out.println(s);
        Properties p = System.getProperties();
        p.list(System.out);
	GMP a = new GMP("243454656");
	GMP b = new GMP("165758648758768");
	long startTime=0;
	for (int i=0;i<20000;i++)
	    {
		if(i==1)
		    {
			startTime=System.currentTimeMillis();
		    }
		a.multiply(b,a);
	    }
	System.out.println("Mulitiplying  2 numbers 20000 times took " + ((System.currentTimeMillis()-startTime))+ "  milliseconds");
	//System.out.println(a); // Display the string.
    }
}
