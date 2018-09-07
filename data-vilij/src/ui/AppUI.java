package ui;

import actions.AppActions;
import algorithms.RunConfig;
import classification.RandomClassifier;
import clustering.Clusterer;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import data.DataSet;
import dataprocessors.AppData;
import java.io.File;
import static java.io.File.separator;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.EXIT_TOOLTIP;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.LOAD_TOOLTIP;
import static vilij.settings.PropertyTypes.NEW_TOOLTIP;
import static vilij.settings.PropertyTypes.PRINT_TOOLTIP;
import static vilij.settings.PropertyTypes.SAVE_TOOLTIP;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private String scButtonPath;  // resource path for screenshot icon
    //private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private LineChart<Number, Number> chart;
    private NumberAxis xAxis, yAxis;  // required axes for scatter chart
    private Label chartLabel;    // chart label
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;      // text area for new data input
    private Label textAreaLabel; // label for text area
    private String oldText = "", currentText = ""; //
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox readOnlyCBox;
    private Button editingButton;
    private Button toggleAxesButton;
    private boolean editing;
    private String dataCSSPath;
    private Text metadataDisplay;
    private VBox algorithmVBox;
    private RadioButton clusterRadio;
    private RadioButton classicRadio;
    private RadioButton randomCluster;
    private RadioButton kmeansCluster;
    private RadioButton randomClassic;
    private ToggleGroup algorithmTypeSelection;
    private ToggleGroup algSelection;
    private VBox algBox;
    private Button runButton;
    private Button settingsButton;
    private RunConfig classicRC;
    private RunConfig clusterRC;
    private RunConfig.AlgType currentAlgType;
    private RunConfig currentRunConfig;
    private Thread algorithmRunThread;
    private HBox setRunHBox;
    private StepThroughButton stepperButton;
    private double minx, miny, maxy, maxx;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public String getTSD() {
        return textArea.getText();
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }
    

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        dataCSSPath = manager.getPropertyValue(DATA_CSS_PATH.name());
        scButtonPath = String.join(separator, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), true);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        printButton = setToolbarButton(printiconPath, manager.getPropertyValue(PRINT_TOOLTIP.name()), true);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        scrnshotButton = setToolbarButton(scButtonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar = new ToolBar(newButton, saveButton, loadButton, printButton, exitButton, scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize() {
        initNodes();
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
        disableScreenshot();
    }

    public void disableSaveButton() {
        saveButton.disableProperty().set(true);
    }

    public void setTextArea(String dataString) {
        textArea.textProperty().set(dataString);
    }

    private void initNodes() {
        this.getPrimaryScene().getStylesheets().add(dataCSSPath);
        PropertyManager manager = applicationTemplate.manager;
        textAreaLabel = new Label(manager.getPropertyValue(TEXT_AREA.name()));
        textAreaLabel.setFont(Font.font(cssPath, FontPosture.REGULAR, 17));
        chartLabel = new Label(manager.getPropertyValue(CHART_LABEL.name()));
        chartLabel.setFont(Font.font(cssPath, FontPosture.REGULAR, 17));
        textArea = new TextArea();
        readOnlyCBox = new CheckBox(manager.getPropertyValue(READONLY.name()));
        toggleAxesButton = new Button("Toggle Auto Axes");
        editingButton = new Button("Finish Editing");
        editing = true;
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        chart = new LineChart<Number, Number>(xAxis, yAxis);
        chart.getStyleClass().add(manager.getPropertyValue(CHART.name()));
        metadataDisplay = new Text();
        algSelection = new ToggleGroup();
        runButton = new Button("Run");
        settingsButton = new Button("Settings");
        classicRC = new RunConfig(RunConfig.AlgType.CLASSIFICATION);
        clusterRC = new RunConfig(RunConfig.AlgType.CLUSTERING);
        stepperButton = new StepThroughButton("Step", 5);
    }

    private void layout() {
        VBox dataEntry = new VBox(10);
        HBox textAreaLabelContainer = new HBox();
        textAreaLabelContainer.setAlignment(Pos.CENTER);
        textAreaLabelContainer.getChildren().add(textAreaLabel);
        HBox belowTextArea = new HBox();
        editingButton.setPadding(new Insets(5, 5, 5, 5));
        toggleAxesButton.setPadding(new Insets(5,5,5,5));
        toggleAxesButton.setVisible(false);
        belowTextArea.getChildren().addAll(editingButton, toggleAxesButton);
        algorithmVBox = new VBox(5);
        HBox algTypeHBox = new HBox();
        algorithmTypeSelection = new ToggleGroup();
        clusterRadio = new RadioButton("Clustering");
        clusterRadio.setToggleGroup(algorithmTypeSelection);
        clusterRadio.setPadding(new Insets(0, 5, 0, 5));
        classicRadio = new RadioButton("Classification");
        classicRadio.setToggleGroup(algorithmTypeSelection);
        classicRadio.setPadding(new Insets(0, 5, 0, 5));
        algTypeHBox.getChildren().addAll(clusterRadio, classicRadio);
        algorithmVBox.getChildren().addAll(algTypeHBox);
        algSelection = new ToggleGroup();
        algBox = new VBox();
        algorithmVBox.getChildren().add(algBox);
        algBox.setVisible(false);
        randomClassic = new RadioButton("Random Classifciation");
        randomClassic.setToggleGroup(algSelection);
        randomClassic.setPadding(new Insets(5, 5, 5, 5));
        randomCluster = new RadioButton("Random Cluster");
        randomCluster.setToggleGroup(algSelection);
        kmeansCluster = new RadioButton("KMeans Cluster");
        kmeansCluster.setToggleGroup(algSelection);
        randomCluster.setPadding(new Insets(5, 5, 5, 5));
        kmeansCluster.setPadding(new Insets(5, 5, 5, 5));
        settingsButton.setPadding(new Insets(5, 5, 5, 5));
        runButton.setPadding(new Insets(5, 5, 5, 5));
        setRunHBox = new HBox(5);
        setRunHBox.getChildren().addAll(settingsButton, runButton);
        algorithmVBox.getChildren().addAll(setRunHBox);
        settingsButton.disableProperty().set(true);
        runButton.disableProperty().set(true);
        dataEntry.getChildren().addAll(textAreaLabelContainer, textArea, belowTextArea, metadataDisplay, algorithmVBox);
        algorithmVBox.setVisible(false);
        dataEntry.setPadding(new Insets(10, 10, 10, 10));
        VBox chartContainer = new VBox();
        HBox chartLabelContainer = new HBox();
        chartLabelContainer.setAlignment(Pos.CENTER);
        chartLabelContainer.getChildren().add(chartLabel);
        chartContainer.getChildren().addAll(chartLabelContainer, chart);
        chartContainer.setPadding(new Insets(10, 10, 10, 10));
        HBox row = new HBox();
        row.getChildren().addAll(dataEntry, chartContainer);
        appPane.getChildren().addAll(row);
    }

    public void setMetadata(String metadata) {
        metadataDisplay.setText(metadata);
    }

    private void setWorkspaceActions() {
        textArea.textProperty().addListener(e -> readData());
        editingButton.setOnMouseClicked(e -> toggleEditing());
        toggleAxesButton.setOnMouseClicked(e -> toggleAutoAxes());
        clusterRadio.selectedProperty().addListener(e -> displayCluster());
        classicRadio.selectedProperty().addListener(e -> displayClassic());
        algSelection.selectedToggleProperty().addListener(e -> displayButts());
        settingsButton.setOnMouseClicked(e -> dispRunConfig());
        runButton.setOnMouseClicked(e -> runButtonHandler());
    }

    private void toggleAutoAxes(){
        xAxis.autoRangingProperty().set(!xAxis.autoRangingProperty().get());
        yAxis.autoRangingProperty().set(!yAxis.autoRangingProperty().get());
        xAxis.setLowerBound(minx-1);
        xAxis.setUpperBound(maxx+1);
        yAxis.setLowerBound(miny-1);
        yAxis.setUpperBound(maxy+1);
    }
    
    private void runAlgorithmStepped(Task t) {
        System.out.println("User selected stepped algorithm.");
        System.out.print("Determining algorithm type... ");
        switch (currentAlgType) {
            case CLASSIFICATION:
                System.out.print("Classification.");
                runClassifierStepped(t);
                break;
            case CLUSTERING:
                System.out.print("Clustering.");
                runClusterStepped(t);
                break;
        }
    }
    
    private void runClusterStepped(Task t) {
        disableScreenshot();
        stepperButton.setVisible(false);
        System.out.println("Cluster stepped");
        Clusterer cAlgorithm = getClusterAlgorithm();
        Platform.runLater(() -> {
            stepperButton = new StepThroughButton("Step", 5);
            stepperButton.setVisible(true);
            stepperButton.setOnMouseClicked(e -> {
                disableScreenshot();
                cAlgorithm.run();
                DataSet ds = cAlgorithm.getDataSet();
                displayClusterOutput(ds);
                enableScreenshot();
            });
            stepperButton.setPadding(new Insets(5, 5, 5, 5));
            if (setRunHBox.getChildren().size() == 3) {
                setRunHBox.getChildren().remove(2);
            }
            setRunHBox.getChildren().add(stepperButton);
        });
    }
    
    private void runClassifierStepped(Task t){
        System.out.println("classifier stepped");
        DataSet dataSet = new DataSet();
        HashMap<String, String> newLabels = (HashMap<String, String>) ((AppData) applicationTemplate.getDataComponent()).getDataLabels();
        HashMap<String, Point2D> newLocations = (HashMap<String, Point2D>) ((AppData) applicationTemplate.getDataComponent()).getDataPoints();
        dataSet.setLabels(newLabels);
        dataSet.setLocations(newLocations);
        int iterations = currentRunConfig.getIterations();
        int updateInterval = currentRunConfig.getUpdateInterval();
        boolean isContinuous = currentRunConfig.isContinuousRun();
        RandomClassifier rcAlgorithm = new RandomClassifier(dataSet, iterations, updateInterval, isContinuous, true);
        Platform.runLater(() -> {
            stepperButton = new StepThroughButton("Step", 5);
            stepperButton.setVisible(true);
            stepperButton.setOnMouseClicked(e -> {
                disableScreenshot();
                rcAlgorithm.run();
                List<Integer> output = rcAlgorithm.getOutput();
                displayAlgorithmOutput(output);
                enableScreenshot();
            });
            stepperButton.setPadding(new Insets(5, 5, 5, 5));
            if (setRunHBox.getChildren().size() == 3) {
                setRunHBox.getChildren().remove(2);
            }
            setRunHBox.getChildren().add(stepperButton);
        });
    }
    
    private void runClassifierContinuous(Task t) throws InterruptedException{
        disableScreenshot();
        stepperButton.setVisible(false);
        System.out.println("Continuous threaded task");
        DataSet dataSet = new DataSet();
        HashMap<String, String> newLabels = (HashMap<String, String>) ((AppData) applicationTemplate.getDataComponent()).getDataLabels();
        HashMap<String, Point2D> newLocations = (HashMap<String, Point2D>) ((AppData) applicationTemplate.getDataComponent()).getDataPoints();
        dataSet.setLabels(newLabels);
        dataSet.setLocations(newLocations);
        int iterations = currentRunConfig.getIterations();
        int updateInterval = currentRunConfig.getUpdateInterval();
        boolean isContinuous = currentRunConfig.isContinuousRun();
        RandomClassifier rcAlgorithm = new RandomClassifier(dataSet, iterations, updateInterval, isContinuous, true);
        for (int i = 0; i <= iterations; i += updateInterval) {
            rcAlgorithm.run();
            List<Integer> output = rcAlgorithm.getOutput();
            displayAlgorithmOutput(output);
            Thread.sleep(1000);
        }
        enableScreenshot();
    }
    
    private void runClusterContinuous(Task t) throws InterruptedException{
        disableScreenshot();
        stepperButton.setVisible(false);
        System.out.println("Cluster continuous");
        int iterations = currentRunConfig.getIterations();
        System.out.println(iterations);
        int updateInterval = currentRunConfig.getUpdateInterval();
        System.out.println(updateInterval);
        Clusterer cAlgorithm = getClusterAlgorithm();
        System.out.println("Entering continuous loop.");
        for (int i = 0; i <= iterations; i += updateInterval) {
            System.out.println("Iteration "+i);
            cAlgorithm.run();
            System.out.println("Dataset");
            DataSet ds = cAlgorithm.getDataSet();
            displayClusterOutput(ds);
            Thread.sleep(1000);
            System.out.println("Done");
        }
        enableScreenshot();
    }
    
    private void displayClusterOutput(DataSet ds) {
        Platform.runLater(() -> {
            chart.getData().clear();
            
            ((AppData) applicationTemplate.getDataComponent()).loadTSDwDataSet(ds);
            ((AppData) applicationTemplate.getDataComponent()).displayData();
            
        });
    }
    
    private Clusterer getClusterAlgorithm(){
        DataSet dataSet = new DataSet();
        HashMap<String, String> newLabels = (HashMap<String, String>) ((AppData) applicationTemplate.getDataComponent()).getDataLabels();
        HashMap<String, Point2D> newLocations = (HashMap<String, Point2D>) ((AppData) applicationTemplate.getDataComponent()).getDataPoints();
        dataSet.setLabels(newLabels);
        dataSet.setLocations(newLocations);
        int iterations = currentRunConfig.getIterations();
        int updateInterval = currentRunConfig.getUpdateInterval();
        boolean isContinuous = currentRunConfig.isContinuousRun();
        if(randomCluster.isSelected()){
            System.out.println("Returning random clusterer.");
            return new RandomClusterer(currentRunConfig.getNumClusters(), dataSet, iterations, updateInterval, isContinuous);
        }
        System.out.println("Returning kmeans.");
        return new KMeansClusterer(dataSet, iterations, updateInterval, currentRunConfig.getNumClusters());
    }

    private void runAlgorithmContinuous(Task t) throws InterruptedException {
        System.out.println("User selected continuous algorithm.");
        System.out.print("Determining algorithm type... ");
        switch (currentAlgType) {
            case CLASSIFICATION:
                System.out.print("Classification.");
                runClassifierContinuous(t);
                break;
            case CLUSTERING:
                System.out.print("Clustering.");
                runClusterContinuous(t);
                break;
        }
    }
    
    
    public boolean algRunning(){
        return algorithmRunThread.isAlive();
    }

    private void runButtonHandler() {
        System.out.println("Running");
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                    if (isContinuousRun()) {
                        try { runAlgorithmContinuous(this); } 
                        catch (InterruptedException ex) {}
                    } 
                    else {
                        runAlgorithmStepped(this);
                    }
                return null;
            }
        };
        algorithmRunThread = new Thread(t, "Algorithm Run Thread");
        algorithmRunThread.start();
    }

    private void displayAlgorithmOutput(List<Integer> coeffs) {
        double a = coeffs.get(0);
        double b = coeffs.get(1);
        double c = coeffs.get(2);
        double[] xValues = getMinMaxX();
        double[] yValues = {coeffFunction(a, b, c, xValues[0]),
            coeffFunction(a, b, c, xValues[1])};
                Platform.runLater(() -> {
            XYChart.Series<Number, Number> lineSeries = new XYChart.Series();
            XYChart.Data d1 = new XYChart.Data<>((double) xValues[0], yValues[0]);
            XYChart.Data d2 = new XYChart.Data<>((double) xValues[1], yValues[1]);
            lineSeries.getData().add(d1);
            lineSeries.getData().add(d2);
            lineSeries.nameProperty().set("Random Classifcation");
            chart.getData().remove(chart.getData().size() - 1);
            chart.getData().add(lineSeries);
            d1.getNode().setVisible(false);
            d2.getNode().setVisible(false);
        });

    }
    
    public void resetEditing(){
        if(!editing)
            toggleEditing();
    }

    private double coeffFunction(double a, double b, double c, double x) {
        return ((-1*c) - (a*x))/b;
    }

    private boolean isContinuousRun() {
        return currentRunConfig.isContinuousRun();
    }

    public void dispRunConfig() {
        switch (currentAlgType) {
            case CLASSIFICATION:
                classicRC.display();
                break;
            case CLUSTERING:
                clusterRC.display();
                break;
        }
        runButton.disableProperty().set(false);
    }

    public void displayCluster() {
        currentAlgType = RunConfig.AlgType.CLUSTERING;
        currentRunConfig = clusterRC;
        if(algBox.getChildren().size() == 2){
            algBox.getChildren().set(0, randomCluster);//, kmeansCluster);
            algBox.getChildren().set(1, kmeansCluster);
        }
        else if(algBox.getChildren().size() == 1){
            algBox.getChildren().set(0, randomCluster);//, kmeansCluster);
            algBox.getChildren().add(kmeansCluster);
        }
        else{
            algBox.getChildren().add(randomCluster);//, kmeansCluster);
            algBox.getChildren().add(kmeansCluster);
        }
        algBox.setVisible(true);
        hideButts();
        try {
            algSelection.getSelectedToggle().setSelected(false);
        } catch (Exception e) {
        }
    }

    public void displayButts() {
        runButton.disableProperty().set(true);
        settingsButton.disableProperty().set(false);
    }

    public void hideButts() {
        runButton.disableProperty().set(true);
        settingsButton.disableProperty().set(true);
    }

    public void displayClassic() {
        currentAlgType = RunConfig.AlgType.CLASSIFICATION;
        currentRunConfig = classicRC;
        algBox.setVisible(true);
        if (algBox.getChildren().size() == 1){
            algBox.getChildren().set(0, randomClassic);
        }
        else if(algBox.getChildren().size() == 2){
            algBox.getChildren().set(0, randomClassic);
            algBox.getChildren().remove(1);
        }
        else{
            algBox.getChildren().add(randomClassic);
        }
        hideButts();
        try {
            algSelection.getSelectedToggle().setSelected(false);
        } catch (Exception e) {
        }
    }

    private void toggleEditing() {
        if (editing) {
            ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
            if (!((AppData) applicationTemplate.getDataComponent()).isValid()) {
                return;
            }
            editingButton.setText("Edit");
            textArea.setEditable(false);
            textArea.disableProperty().set(true);
            algorithmVBox.setVisible(true);
            boolean isValidClassification = ((AppData) applicationTemplate.getDataComponent()).validClassic();
            classicRadio.setDisable(!isValidClassification);
            toggleAxesButton.setVisible(true);
            displayData();
        } else {
            toggleAxesButton.setVisible(false);
            editingButton.setText("Finish Editing");
            textArea.setEditable(true);
            textArea.disableProperty().set(false);
            algorithmVBox.setVisible(false);
            runButton.setDisable(true);
            settingsButton.setDisable(true);
            try {
                algorithmTypeSelection.getSelectedToggle().setSelected(false);
                algSelection.getSelectedToggle().setSelected(false);
            } catch (Exception e) {
            }
        }
        algBox.setVisible(false);
        editing = !editing;
    }

    public void takeChartScreenshot() {
        PropertyManager manager = applicationTemplate.manager;
        WritableImage image = new WritableImage((int) chart.getWidth(), (int) chart.getHeight());
        image = chart.snapshot(null, image);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(manager.getPropertyValue(IMGFILE.name()), manager.getPropertyValue(IMGEXT.name())));
        fileChooser.setTitle(manager.getPropertyValue(TAKESCREENSHOT.name()));
        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (Exception s) {
        }
    }

//    private void toggleReadOnlyStatus() {
//        boolean readOnlyChecked = readOnlyCBox.selectedProperty().get();
//        if (readOnlyChecked) {
//            textArea.setEditable(false);
//            textArea.disableProperty().set(true);
//        } else {
//            textArea.setEditable(true);
//            textArea.disableProperty().set(false);
//        }
//    }

    private void checkForNewText() {
        oldText = currentText;
        currentText = textArea.getText();
        hasNewText = !currentText.equals(oldText);
    }

    private void checkForEmpty() {
        boolean isEmpty = currentText.length() == 0 ? true : false;
        saveButton.disableProperty().set(isEmpty);
        newButton.disableProperty().set(isEmpty);
    }

    private void readData() {
        checkForNewText();
        checkForEmpty();
    }

    private void enableScreenshot() {
        scrnshotButton.disableProperty().set(false);
    }

    private void disableScreenshot() {
        scrnshotButton.disableProperty().set(true);
    }

    private double[] getMinMaxX() {
        double yavg = 0, cnt = 0, maxx = 0;
        double minx = (double) chart.getData().get(0).getData().get(0).getXValue();
        for (XYChart.Series<Number, Number> data : chart.getData()) {
            for (XYChart.Data<Number, Number> datum : data.getData()) {
                if ((double) datum.getXValue() > maxx) {
                    maxx = (double) datum.getXValue();
                }
                if ((double) datum.getXValue() < minx) {
                    minx = (double) datum.getXValue();
                }
                cnt += 1;
                yavg += (double) datum.getYValue();
            }
        }
        double[] ans = {minx, maxx};
        return ans;
    }
    
    public void populateTextArea(String s){
        textArea.setText(s);
    }

    private void displayData() {
        PropertyManager manager = applicationTemplate.manager;
        ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
        chart.getData().clear();
        ((AppData) applicationTemplate.getDataComponent()).displayData();
        double yavg = 0, cnt = 0;
        maxx = 0;
        maxy = 0;
        minx = (double) chart.getData().get(0).getData().get(0).getXValue();
        miny = (double) chart.getData().get(0).getData().get(0).getYValue();
        for (XYChart.Series<Number, Number> data : chart.getData()) {
            data.getNode().setId(manager.getPropertyValue(TRANS_CLASS.name()));
            for (XYChart.Data<Number, Number> datum : data.getData()) {
                if ((double) datum.getXValue() > maxx) {
                    maxx = (double) datum.getXValue();
                }
                if ((double) datum.getYValue() > maxy) {
                    maxy = (double) datum.getYValue();
                }
                if ((double) datum.getXValue() < minx) {
                    minx = (double) datum.getXValue();
                }
                if ((double) datum.getYValue() < miny) {
                    miny = (double) datum.getYValue();
                }
                cnt += 1;
                yavg += (double) datum.getYValue();
            }
        }
        yavg /= cnt;
        XYChart.Series<Number, Number> lineSeries = new XYChart.Series();
        XYChart.Data d1 = new XYChart.Data<>((double) minx, yavg);

        XYChart.Data d2 = new XYChart.Data<>((double) maxx, yavg);

        lineSeries.getData().add(d1);
        lineSeries.getData().add(d2);

        lineSeries.nameProperty().set(manager.getPropertyValue(AVGTXT.name()));
        chart.getData().add(lineSeries);
        d1.getNode().setVisible(false);
        d2.getNode().setVisible(false);
        enableScreenshot();
        xAxis.autoRangingProperty().set(false);
        xAxis.setLowerBound(minx-1);
        xAxis.setUpperBound(maxx+1);
        yAxis.autoRangingProperty().set(false);
        yAxis.setLowerBound(miny-1);
        yAxis.setUpperBound(maxy+1);
       // ((AppData) applicationTemplate.getDataComponent()).clearProcessor();
    }
}
