package jp.ac.osakau.farseerfc.purano.reflect.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.reflect.ClassFinderDumpper;
import jp.ac.osakau.farseerfc.purano.reflect.HtmlDumpper;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 5/31/13
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassFinderTest {
    
    @Test
    public void testPurano() throws IOException{
		ClassFinder cf = new ClassFinder(Arrays.asList("jp.ac.osakau.farseerfc.purano","org.objectweb.asm","java.lang.Object"));
		cf.resolveMethods();
	    File output = new File("/tmp/output-purano.html");
	    PrintStream ps = new PrintStream(new FileOutputStream(output));
	    ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
	    dumpper.dump();
	}
    
    @Test
    public void testTomcat() throws IOException{
		ClassFinder cf = new ClassFinder(Arrays.asList("org.apache.catalina"));
		cf.resolveMethods();
	    File output = new File("/tmp/output-tomcat.html");
	    PrintStream ps = new PrintStream(new FileOutputStream(output));
	    ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
	    dumpper.dump();
	}
    
    @Test
    public void testHtmlParser() throws IOException{
		ClassFinder cf = new ClassFinder(Arrays.asList("org.htmlparser"));
		cf.resolveMethods();
	    File output = new File("/tmp/output-htmlparser.html");
	    PrintStream ps = new PrintStream(new FileOutputStream(output));
	    ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
	    dumpper.dump();
	}
    
    @Test
    public void testArgoUML() throws IOException{
		ClassFinder cf = new ClassFinder(Arrays.asList("org.argouml"));
		cf.resolveMethods();
	    File output = new File("/tmp/output-argouml.html");
	    PrintStream ps = new PrintStream(new FileOutputStream(output));
	    ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
	    dumpper.dump();
	}
}
