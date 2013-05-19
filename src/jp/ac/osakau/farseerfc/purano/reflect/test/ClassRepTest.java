package jp.ac.osakau.farseerfc.purano.reflect.test;

import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.reflect.ClassRep;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

public class ClassRepTest {

	@Test
	public void testGetMethodVirtual() throws ClassNotFoundException, IOException {
		ClassFinder cf = new ClassFinder(Arrays.asList("jp.ac.osakau.farseerfc.purano.reflect.test"));
		ClassRep cr = cf.loadClass("java.util.AbstractList");
		MethodRep mr = cr.getMethodVirtual("removeAll(Ljava/util/Collection;)Z");
		assertNotNull(mr);
	}

}
