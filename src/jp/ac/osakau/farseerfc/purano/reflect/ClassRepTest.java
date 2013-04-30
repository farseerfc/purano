package jp.ac.osakau.farseerfc.purano.reflect;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassRepTest {

	@Test
	public void testNameToReflect() throws ClassNotFoundException {
		String className = "jp.ac.osakau.farseerfc.purano.reflect.ClassRepTest";
		ClassRep rep = new ClassRep(Class.forName(className));
		assertNotNull(rep.getReflect());
		ClassRep rep2 = new ClassRep(rep.getReflect());
		assertEquals(rep2.getName(),className);
	}

}
