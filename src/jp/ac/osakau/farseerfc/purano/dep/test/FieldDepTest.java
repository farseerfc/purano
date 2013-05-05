package jp.ac.osakau.farseerfc.purano.dep.test;

import static org.junit.Assert.*;
import jp.ac.osakau.farseerfc.purano.dep.FieldDep;

import org.junit.Test;

public class FieldDepTest {

	@Test
	public void test() {
		String s1="A";
		String s2=Character.toString((char)65);
		FieldDep fd1 = new FieldDep(s1,"B","C");
		FieldDep fd2 = new FieldDep(s2,"B","C");
		assertEquals(fd1, fd2);
		assertTrue(s1 != s2);
	}

}
