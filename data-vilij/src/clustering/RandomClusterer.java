package clustering;

import data.DataSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lukas
 */
public class RandomClusterer extends Clusterer{
    
    private int maxIterations;
    private int updateInterval;
    private boolean isContinuous;
    
    public RandomClusterer(int k, DataSet d, int maxI, int upI, boolean isC){
        super(k, d);
        maxIterations = maxI;
        updateInterval = upI;
        isContinuous = isC;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return isContinuous;
    }

    @Override
    public void run() {
        Map<String, String> labels = dataset.getLabels();
        HashSet<String>labelsSet = new HashSet<String>();
        for(String key: labels.keySet()){
            labelsSet.add(labels.get(key));
            System.out.println(key+": "+labels.get(key));
        }
        ArrayList<String> labelsList = new ArrayList<String>(labelsSet);
        for(String key: labels.keySet()){
            labels.put(key, labelsList.get((int)(Math.random()*(labelsList.size()))));
        }
        
        dataset.setLabels(labels);
    }
    
}
