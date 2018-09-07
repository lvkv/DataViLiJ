/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import algorithms.Algorithm;
import data.DataSet;

/**
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {
    public DataSet       dataset;
    protected final int numberOfClusters;

    public int getNumberOfClusters() { return numberOfClusters; }

    public Clusterer(int k, DataSet d) {
        if (k < 2)
            k = 2;
        else if (k > 4)
            k = 4;
        numberOfClusters = k;
        dataset = d;
    }
    
    public DataSet getDataSet(){
        return dataset;
    }
}