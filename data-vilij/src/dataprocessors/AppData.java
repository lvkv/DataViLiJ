package dataprocessors;

import data.DataSet;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import java.nio.file.Path;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import static settings.AppPropertyTypes.CONSISTSOF;
import static settings.AppPropertyTypes.INVALID_DATA_TITLE;
import static settings.AppPropertyTypes.IWANTTOSLEEP;
import static settings.AppPropertyTypes.LOADEDDATAINFO;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    public AppData(){this.processor = new TSDProcessor();}
    
    @Override
    public void loadData(Path dataFilePath){
        try{
            String dataString = new String(Files.readAllBytes(dataFilePath));
            loadData(dataString);
        }
        catch(Exception e){}
    }
    
    public void clearProcessor(){
        processor.clear();
    }
    
    public String getMetadata(){
        return processor.getMetadata();
    }
    
    public Map<String, String> getDataLabels(){
        return processor.getDataLabels();
    }
    
    public Map<String, Point2D> getDataPoints(){
        return processor.getDataPoints();
    }
    
    public boolean isValid(){
        return processor.isValid();
    }

    public boolean loadData(String dataString){
        clearProcessor();
        PropertyManager manager = applicationTemplate.manager;
        try{ 
            processor.processString(dataString);
            return true;
        }
        catch(Exception e){
            System.out.println(e);
            ErrorDialog eDialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            eDialog.show(manager.getPropertyValue(INVALID_DATA_TITLE.name()), e.getMessage());
            ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
            return false;
        }
    }
    
    public void popupLoadMetadata() {
        PropertyManager manager = applicationTemplate.manager;
        int numLines = processor.getNumLines();
        if (numLines > 10) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(manager.getPropertyValue(LOADEDDATAINFO.name()));
            alert.setHeaderText(null);
            alert.setContentText(String.format(manager.getPropertyValue(CONSISTSOF.name()) + processor.getNumLines() + manager.getPropertyValue(IWANTTOSLEEP.name())));
            alert.showAndWait();
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try {
            FileWriter fw = new FileWriter(dataFilePath.toString());
            PrintWriter pw = new PrintWriter(fw);
            pw.print(((AppUI) applicationTemplate.getUIComponent()).getTSD());
            pw.close();
        }
        catch(Exception e){}
    }
    
    public boolean validClassic(){
        System.out.println(processor.numLabels()==2);
        return processor.numLabels() == 2;
    }

    @Override
    public void clear() {
        processor.clear();
    }
    
    public void loadTSDwDataSet(DataSet ds){
        processor.setDataLabels(ds.getLabels());
        processor.setDataPoints(ds.getLocations());
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        //processor.clear();
    }
}
