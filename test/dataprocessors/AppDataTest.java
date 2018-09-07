/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import data.DataSet;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import javafx.geometry.Point2D;
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
public class AppDataTest {
    
    public AppDataTest() {
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
     * Test of saveData method, of class AppData.
     */
    @Test
    public void testSaveData() {
        System.out.println("saveData");
        File f = new File("test");
        Path dataFilePath = f.toPath();
        AppData instance = new AppData();
        instance.saveData(dataFilePath);
    }
}
