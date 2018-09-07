/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author lukas
 */
public class TSDProcessorTest {
    
    public TSDProcessorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of processString method, of class TSDProcessor.
     */
    @Test
    public void testValidProcessString() throws Exception {
        System.out.println("processString");
        String tsdStringValid = "@Instance1	label1	1.5,2.2";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdStringValid);
    }
    
     /**
     * Test of processString method, of class TSDProcessor.
     */
    @Test(expected = TSDProcessor.InvalidTSDError.class)
    public void testInvalidProcessString() throws Exception {
        System.out.println("processString");
        String tsdStringInvalid = "oops poopsie wooopsie";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdStringInvalid);
    }  
}
