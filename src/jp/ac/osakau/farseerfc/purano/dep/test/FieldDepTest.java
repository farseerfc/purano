package jp.ac.osakau.farseerfc.purano.dep.test;

import jp.ac.osakau.farseerfc.purano.dep.FieldDep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FieldDepTest {

	@Test
	public void test() {
		String s1="A";
		String s2=Character.toString((char)65);
		FieldDep fd1 = new FieldDep(s1,"B","C");
		FieldDep fd2 = new FieldDep(s2,"B","C");
		assertEquals(fd1, fd2);
        //noinspection StringEquality
        assertTrue(s1 != s2);
	}

}
