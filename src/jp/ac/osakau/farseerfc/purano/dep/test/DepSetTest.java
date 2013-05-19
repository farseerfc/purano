package jp.ac.osakau.farseerfc.purano.dep.test;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.dep.FieldDep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DepSetTest {

	@Test
	public void test() {
		DepSet ds1=new DepSet();
		ds1.getFields().add(new FieldDep("A","B","C"));
		ds1.getLocals().add(1);
		
		DepSet ds2=new DepSet();
		ds2.getFields().add(new FieldDep("A","B","C"));
		ds2.getLocals().add(1);
		assertEquals(ds1,ds2);
		ds2.getLocals().add(1);
		assertEquals(ds1,ds2);
		ds2.getFields().add(new FieldDep("A","B","C"));
		assertEquals(ds1,ds2);
		
		ds2.getLocals().add(2);
		assertTrue(!ds1.equals(ds2));
	}

}
