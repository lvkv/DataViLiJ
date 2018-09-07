/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lukas
 */
public class RunConfigTest {
    
    public RunConfigTest() {
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
    
    @Test
    public void testRCValid() throws Exception {
        RunConfig instance = new RunConfig();
        String iterations = "1";
        String updInterval = "1";
        String numCluster = "1";
        instance.evaluateRunConfig(iterations, updInterval, numCluster);
    }

    @Test(expected = java.lang.NumberFormatException.class)
    public void testRCInvalidText() throws Exception {
        RunConfig instance = new RunConfig();
        String iterations = "a";
        String updInterval = "a";
        String numCluster = "a";
        instance.evaluateRunConfig(iterations, updInterval, numCluster);
    }
    
    @Test(expected = RunConfig.InvalidInputError.class)
    public void testRCInvalidNumbers() throws Exception {
        RunConfig instance = new RunConfig();
        String iterations = "-1";
        String updInterval = "-1";
        String numCluster = "-1";
        instance.evaluateRunConfig(iterations, updInterval, numCluster);
    }
}
