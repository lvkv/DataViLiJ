package dataprocessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.scene.control.Tooltip;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends TSDProcessorError {
        private static final String NAME_ERROR_MSG = "\nAll data instance names must start with the @ character.";
        public InvalidDataNameException(String name, int ln) {
            super(String.format("Invalid name '%s'. " + NAME_ERROR_MSG, name), ln);
        }
    }
    
    public static class IdenticalNamesException extends TSDProcessorError{
        private static final String NAME_ERROR_MSG = "Duplicate instance names found: ";
        public IdenticalNamesException(String name, int ln) {
            super(NAME_ERROR_MSG + name + " ", ln);
        }
    }
    
    public static class InvalidTSDError extends Exception{
        public InvalidTSDError(String er){super(er);}
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private int lineNumber;
    private int numLines;
    private boolean isValid;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    public Map<String, String> getDataLabels(){
        return dataLabels;
    }
    
    public Map<String, Point2D> getDataPoints(){
        return dataPoints;
    }
    
    public void setDataLabels(Map<String, String> dL){
        dataLabels = dL;
    }
    
    public void setDataPoints(Map<String, Point2D> dP){
        dataPoints = dP;
    }
    
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        isValid = true;
        numLines = numberOfLinesIn(tsdString);
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        lineNumber = 0;
        ArrayList<String> labelz = new ArrayList<String>();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  lineNumber++; 
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      labelz.add(name);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });

        for(int i=0; i<labelz.size(); i++){
            for(int j=0; j<labelz.size(); j++){
                if(labelz.get(i).equals(labelz.get(j)) && i!= j){
                    System.out.println("testing "+labelz.get(i)+" vs "+labelz.get(j));
                    isValid = false;
                    throw new IdenticalNamesException(labelz.get(i), lineNumber);
                }
            }
        }
        if (errorMessage.length() > 0)
            throw new InvalidTSDError(errorMessage.toString());
    }
    
    private int numberOfLinesIn(String str){
        String[] split = str.split("\n");
        return split.length;
    }
    
    
    
    public String getMetadata(){
        Set<String> labels = new HashSet<String>();
        labels.addAll(dataLabels.values());
        String ans = "Instances:\t\t" + dataLabels.size();
        ans       += "\nLabels:\t\t" + labels.size();
        ans       += "\nLabel names:\t";
        for(String labelName : labels){
            ans += labelName + " ";
        }
        return ans+"\nFile Path:\t\t";
    }
    
    public int getNumLines(){
        return numLines;
    }
    
    public int numLabels(){
        Set<String> labels = new HashSet<String>();
        labels.addAll(dataLabels.values());
        return labels.size();
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        ArrayList<String> names = new ArrayList<>(dataLabels.keySet());
        for (String label : labels) {
            names.add(label);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number, Number> pointToAdd = new XYChart.Data<>(point.getX(), point.getY());
                
                series.getData().add(pointToAdd);
                //pointToAdd.getNode().setVisible(true);
                XYChart.Data<Number, Number> dpoint = series.getData().get(series.getData().size()-1);
            });
            chart.getData().add(series);
        }
       for (XYChart.Series<Number, Number> data : chart.getData()) {
           data.getNode().setVisible(false);
            //data.getNode().setId("serie-select");
            for (XYChart.Data<Number, Number> datum : data.getData()) {
                
                Tooltip.install(datum.getNode(), new Tooltip(names.remove(0)));
                datum.getNode().setId("serie-select");
            }
        }
    }
    
    public boolean isValid(){
        return isValid;
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
        lineNumber = 0;
        numLines = 0;
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@")){
            isValid = false;
            throw new InvalidDataNameException(name, lineNumber);
        }
        return name;
    }
}
