package jp.ac.osakau.farseerfc.purano.reflect.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.reflect.ClassFinderDumpper;
import jp.ac.osakau.farseerfc.purano.reflect.HtmlDumpper;

import org.junit.Test;

public class JoldenTest {
    @Test
    public void testJoldenBh() throws Exception {
        jolden("bh");
    }
    
    @Test
    public void testJoldenBisort() throws Exception {
        jolden("bisort");
    }
    
    @Test
    public void testJoldenEm3d() throws Exception {
        jolden("em3d");
    }
    
    @Test
    public void testJoldenHealth() throws Exception {
        jolden("health");
    }
    
    @Test
    public void testJoldenMst() throws Exception {
        jolden("mst");
    }
    
    @Test
    public void testJoldenPerimeter() throws Exception {
        jolden("perimeter");
    }
    
    @Test
    public void testJoldenPowner() throws Exception {
        jolden("power");
    }
    
    @Test
    public void testJoldenTreeAdd() throws Exception {
        jolden("treeadd");
    }
    
    @Test
    public void testJoldenTsp() throws Exception {
        jolden("tsp");
    }
    
    @Test
    public void testJoldenVoronoi() throws Exception {
        jolden("voronoi");
    }
    

    public void jolden(String suite) throws IOException{
		ClassFinder cf = new ClassFinder(Arrays.asList("mit.jolden."+suite));
		cf.resolveMethods();
	
	    File output = new File("/tmp/output-"+suite+".html");
	    PrintStream ps = new PrintStream(new FileOutputStream(output));
	    ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
	    dumpper.dump();
	}
}
