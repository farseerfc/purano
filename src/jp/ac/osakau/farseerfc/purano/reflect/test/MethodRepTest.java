package jp.ac.osakau.farseerfc.purano.reflect.test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;

import org.junit.Test;
import org.objectweb.asm.tree.MethodInsnNode;

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
		MethodRep rep = new MethodRep(node);
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
}
