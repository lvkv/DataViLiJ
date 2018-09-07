 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author lukas
 */

public class RunConfig {
    private Stage stage;
    private VBox parent;
    private Scene scene;
    private AlgType algType;
    
    private TextField iterationsInput;
    private TextField updateInput;
    private CheckBox continuousRunBox;
    private TextField numClusterInput;
    private Button okButton;
    
    private Integer iterations;
    private int updateInterval;
    private boolean continuousRun;
    private int numClusters;
    
    public enum AlgType{
        CLASSIFICATION,
        CLUSTERING
    }
    
    public static class InvalidInputError extends Exception{
        public InvalidInputError(String er){
            super(er);
        }
    }
    
    public RunConfig(AlgType algType){
        this.algType = algType;
        init();
    }
    
    public RunConfig(){
    }
    
    public TextField getIterInput(){
        return iterationsInput;
    }
    
    private void init(){
        stage = new Stage();
        stage.setTitle("Run Configuration");
        parent = new VBox(5);
        parent.setPadding(new Insets(10,10,10,10));
        scene = new Scene(parent);
        
        Label iterLabel = new Label("Iterations:\t\t\t\t");
        iterationsInput = new TextField();
        iterationsInput.setText("5");
        HBox iterationsHBox = new HBox();
        iterationsHBox.getChildren().addAll(iterLabel, iterationsInput);
        
        Label updateLabel = new Label("Update Interval:\t\t");
        updateInput = new TextField();
        updateInput.setText("1");
        HBox updateHBox = new HBox();
        updateHBox.getChildren().addAll(updateLabel, updateInput);
        
        continuousRunBox = new CheckBox("Continuous Run?");
        
        Label numClusterLabel = new Label("Number of Clusters:\t");
        numClusterInput = new TextField();
        numClusterInput.setText("3");
        HBox clusterHBox = new HBox();
        clusterHBox.getChildren().addAll(numClusterLabel, numClusterInput);
        
        parent.getChildren().addAll(iterationsHBox, updateHBox, continuousRunBox);
        if(algType == AlgType.CLUSTERING){
            parent.getChildren().addAll(clusterHBox);
        }
        
        okButton = new Button("OK");
        parent.getChildren().add(okButton);
        okButton.setOnMouseClicked(e -> readRC());
        
        stage.setScene(scene);
    }
    
    public int getIterations() {
        return iterations;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
    
    public int getNumClusters(){
        return numClusters;
    }

    public void readRC() {
        String iterText = iterationsInput.getText();
        String updateText = updateInput.getText();
        try {
            iterations = (int) Double.parseDouble(iterText);
            if (iterations < 0) {
                throw new InvalidInputError("Negative Input");
            }
        } catch (Exception e) {
            errorDialog();
            iterationsInput.setText("5");
            return;
        }

        try {
            updateInterval = (int) Double.parseDouble(updateText);
            if (updateInterval < 0) {
                throw new InvalidInputError("Negative Input");
            }
        } catch (Exception e) {
            errorDialog();
            updateInput.setText("1");
            return;
        }
        if (algType == AlgType.CLUSTERING) {
            String clusterText = numClusterInput.getText();
            try {
                numClusters = (int) Double.parseDouble(clusterText);
                if (numClusters < 0) {
                    throw new InvalidInputError("Negative Input");
                }
            } catch (Exception e) {
                errorDialog();
                numClusterInput.setText("3");
                return;
            }
        }
        stage.close();
    }
    
    public void evaluateRunConfig(String iterationInput, String updInterval, String numCluster) throws Exception{
        //throw new InvalidInputError("Negative Input");

            iterations = (int) Double.parseDouble((String)iterationInput);
            if (iterations < 0) {
                throw new InvalidInputError("Negative Input");
            }



            updateInterval = (int) Double.parseDouble((String)updInterval);
            if(updateInterval< 0){
                throw new InvalidInputError("Negative Input");
            }

        if (algType == AlgType.CLUSTERING) {
                numClusters = (int) Double.parseDouble((String)numCluster);
                if (numClusters < 0) {
                    throw new InvalidInputError("Negative Input");
                }
        }
        
    }
    
    
    public boolean isContinuousRun(){
        return continuousRunBox.isSelected();
    }
    
    private void errorDialog(){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Invalid Input");
        alert.setContentText("Please enter valid numbers!");
        alert.showAndWait();
    }
    
    public void display(){
        stage.show();
    }
}
