package jp.ac.osakau.farseerfc.purano.reflect.test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.effect.ThisFieldEffect;
import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.reflect.ClassRep;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.test.TargetA;
import jp.ac.osakau.farseerfc.purano.test.TargetC;
import jp.ac.osakau.farseerfc.purano.util.Types;

import org.junit.Test;
import org.objectweb.asm.tree.MethodInsnNode;

import com.google.common.base.Joiner;

public class MethodRepTest {

	@Test
	public void testReflect2Node() throws NoSuchMethodException, SecurityException {
		Method method = this.getClass().getDeclaredMethod(
				"testReflect2Node", new Class<?>[0]);
		assertNotNull(method);
//		MethodRep rep = new MethodRep(method,this.getClass().getName());
//		assertEquals(rep.getReflect(),method);
//		assertEquals(rep.getInsnNode().name,"testReflect2Node");
//		
//		MethodRep rep2 = new MethodRep(rep.getInsnNode());
//		assertEquals(rep2.getReflect(),method);
	}

	@Test
	public void testNode2Reflect() throws NoSuchMethodException, SecurityException{
		MethodInsnNode node = new MethodInsnNode(0,
				"jp/ac/osakau/farseerfc/purano/reflect/test/MethodRepTest",
				"testNode2Reflect",
				"()V");
		MethodRep rep = new MethodRep(node, 0);
		assertEquals(rep.getInsnNode(),node);
//		assertEquals(rep.getReflect(), this.getClass().getDeclaredMethod(
//				"testNode2Reflect", new Class<?>[0]));
//		
//		MethodRep rep2= new MethodRep(rep.getReflect(),rep.getInsnNode().owner);
//		assertEquals(rep2.getInsnNode().name, node.name);
//		assertEquals(rep2.getInsnNode().owner, node.owner);
//		assertEquals(rep2.getInsnNode().desc, node.desc);
		
	}
	
	
	@Test
	public void testPerformance() throws NoSuchMethodException, SecurityException{
		final int TIMES = 1000;
		for(int i=0;i<TIMES;++i){
			testReflect2Node();
			//testNode2Reflect();
		}
	}
	
	@Test
	public void testStatic(){
		ClassFinder cl = new ClassFinder("jp.ac.osakau.farseerfc.purano.reflect.test");
		ClassRep cr = cl.loadClass(TargetA.class.getName());
		MethodRep mr1= cr.getMethodVirtual("memberAdd(II)I");
		assertFalse(mr1.isStatic());
		MethodRep mr2 = cr.getMethodVirtual("staticAdd(II)I");
		assertTrue(mr2.isStatic());
	}
	
	@Test
	public void testArgAndThis(){
		ClassFinder cl = new ClassFinder("jp.ac.osakau.farseerfc.purano.reflect.test");
		ClassRep cr = cl.loadClass(TargetA.class.getName());
		MethodRep mr1= cr.getMethodVirtual("memberAdd(II)I");
		MethodRep mr2 = cr.getMethodVirtual("staticAdd(II)I");
		assertTrue(mr1.isThis(0));
		assertFalse(mr2.isThis(0));
		
		assertEquals(mr1.argCount(),3);
		assertEquals(mr2.argCount(),2);
		
		assertFalse(mr1.isArg(0));
		assertTrue(mr1.isArg(1));
		assertTrue(mr1.isArg(2));
		assertFalse(mr1.isArg(3));
		
		assertTrue(mr2.isArg(0));
		assertTrue(mr2.isArg(1));
		assertFalse(mr2.isArg(2));
		
	}
	
	@Test 
	public void testMerge(){
		ClassFinder cl = new ClassFinder("jp.ac.osakau.farseerfc.purano.reflect.test");
		ClassRep ca = cl.loadClass(TargetA.class.getName());
		ClassRep cc = cl.loadClass(TargetC.class.getName());
		
		MethodRep ma = ca.getMethodVirtual("equals(Ljava/lang/Object;)Z");
		MethodRep mc = cc.getMethodVirtual("equals(Ljava/lang/Object;)Z");
		ma.resolve(1, cl);
		mc.resolve(2, cl);
		ma.resolve(3, cl);
		mc.resolve(4, cl);
		
		assertTrue(mc.getOverrides().get(0) == ma);
		DepEffect dec = mc.getDynamicEffects();
		DepEffect dea = ma.getDynamicEffects();
		assertTrue(dea.isSubset(dec));
		
		System.out.println(dea.dump(ma, new Types(), ""));
		System.out.println(dec.dump(mc, new Types(), ""));
		
		ThisFieldEffect tfe1 = new ThisFieldEffect("A", "B", "C", new DepSet(), null);
		ThisFieldEffect tfe2 = new ThisFieldEffect("A", "B", "C", new DepSet(), null);
		ThisFieldEffect tfe3 = new ThisFieldEffect("A", "B", "D", new DepSet(), null);
		assertEquals(tfe1, tfe2);
		assertFalse(tfe1.equals(tfe3));
			
		System.out.println(Joiner.on("\n").join( ma.dump(cl, new Types())));
		System.out.println(Joiner.on("\n").join( mc.dump(cl, new Types())));
	}
	
	@Test
	public void testLocalVariable(){
		ClassFinder cl = new ClassFinder("jp.ac.osakau.farseerfc.purano.reflect.test");
		ClassRep ca = cl.loadClass(Class.class.getName());
		MethodRep ma = ca.getMethodVirtual("copyFields([Ljava/lang/reflect/Field;)[Ljava/lang/reflect/Field;");
		ma.resolve(1, cl);
		System.out.println(ma.getMethodNode().localVariables);
	}

	
	
}
