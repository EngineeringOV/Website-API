/*package ventures.of.api;

import org.dfdeshom.math.GMP;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ventures.of.api.utils.ByteUtils.asSha1;
import static ventures.of.api.utils.ByteUtils.asBase64;

@SpringBootTest
@ContextConfiguration("/applicationContext.xml")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void libraryPathCorrectTest() throws IllegalAccessException, NoSuchFieldException {

		System.setProperty("java.library.path", "./lib/GMP-java/");
//set sys_paths to null
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);

		Properties p = System.getProperties();
/*
		Enumeration keys = p.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String value = (String)p.get(key);
			System.out.println(key + ": " + value);
		}

		//Call native lib
		GMP gGmp = new GMP(7);
		GMP nGmp = new GMP();
		int n = nGmp.fromString("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);

		assertEquals("./lib/GMP-java/", (String)p.get("java.library.path"));
	}

	@Test
	public void accurateToPhp() throws IllegalAccessException, NoSuchFieldException, UnsupportedEncodingException, NoSuchAlgorithmException {
		assertEquals("qUqP5cyxm6YcTAhz05Hph5gvu9M=", phpShaAndBase64("test"));
		assertEquals("qvTGHdzF6KLavt4PO0gs2a6pQ00=", phpShaAndBase64("hello"));
		assertEquals("Lza8cQgTqgtg2y2oVycUqgQ3sRc=", phpShaAndBase64("freiheit"));
		assertEquals("QDkmAz0AG1J53zfLvlKHt8fCZ/o=", phpShaAndBase64("lol"));
		assertEquals("URbCjmUaGQE4IsCeXHDJ/EJaZtw=", phpShaAndBase64("kek"));
	}

	private String phpShaAndBase64(String textToEncode) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return asBase64(asSha1(textToEncode.getBytes("UTF-8")));
	}

}

*/