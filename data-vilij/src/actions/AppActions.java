package actions;

import dataprocessors.AppData;
import java.io.File;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import vilij.propertymanager.PropertyManager;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import static settings.AppPropertyTypes.DATA_FILE_EXT;
import static settings.AppPropertyTypes.DATA_FILE_EXT_DESC;
import static settings.AppPropertyTypes.SAVE_UNSAVED_WORK;
import static settings.AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE;
import ui.AppUI;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }
    
    public String getFilePath(){
        return dataFilePath.toString();
    }

    @Override
    public void handleNewRequest() {
        try{
            if(promptToSave())
                ((AppUI)applicationTemplate.getUIComponent()).clear();
            dataFilePath = null;
        }catch(Exception e){}
    }

    @Override
    public void handleSaveRequest() {
        if (dataFilePath == null){
            ((AppData) applicationTemplate.getDataComponent()).loadData(((AppUI)applicationTemplate.getUIComponent()).getTSD());
            promptFileChooser();
        }else{
            ((AppData)applicationTemplate.getDataComponent()).loadData(dataFilePath);
            ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
        }
        String metadata = ((AppData)applicationTemplate.getDataComponent()).getMetadata()+ this.getFilePath();
        ((AppUI)applicationTemplate.getUIComponent()).setMetadata(metadata);
        ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
    }
    
    @Override
    public void handleLoadRequest() {
        PropertyManager manager = applicationTemplate.manager;
        ((AppUI)applicationTemplate.getUIComponent()).resetEditing();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
        File file = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (file == null) 
            return;
        ((AppUI)applicationTemplate.getUIComponent()).clear();
        dataFilePath = file.toPath();
        ((AppData)applicationTemplate.getDataComponent()).loadData(dataFilePath);
        ((AppUI)applicationTemplate.getUIComponent()).setTextArea(getStringFromFile(dataFilePath));
        String metadata = ((AppData)applicationTemplate.getDataComponent()).getMetadata()+ this.getFilePath();
        ((AppUI)applicationTemplate.getUIComponent()).setMetadata(metadata);
        ((AppData)applicationTemplate.getDataComponent()).popupLoadMetadata();
        //((AppUI)applicationTemplate.getUIComponent()).displayAlgTypeSelection();
        ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
        //return true;
    }

    @Override
    public void handleExitRequest() {
        boolean algRunning = ((AppUI)applicationTemplate.getUIComponent()).algRunning();
        if (algRunning) {
            PropertyManager manager = applicationTemplate.manager;
            ConfirmationDialog cDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            cDialog.show("Algorithm is Running", "An algorithm is currently running. Still exit?");
            switch (cDialog.getSelectedOption()) {
                case CANCEL:
                    return;
                case YES:
                    break;
                case NO:
                    return;
                default:
                    return;
            }
            try {
                if (dataFilePath==null && promptToSave()) {
                    ((AppUI) applicationTemplate.getUIComponent()).clear();
                }
                dataFilePath = null;
            } catch (Exception e) {
            }
            System.exit(0);
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        ((AppUI)applicationTemplate.getUIComponent()).takeChartScreenshot();
    }
    
    private String getStringFromFile(Path path){
        try {
            String dataString = new String(Files.readAllBytes(path));
            return dataString;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        ConfirmationDialog cDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        cDialog.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        switch(cDialog.getSelectedOption()){
            case CANCEL:
                return false;
            case YES:
                if(dataFilePath != null){
                  ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);  
                  return true;
                }
                return promptFileChooser();
            default:
                return true; // user chooses 'no'
        }
    }
    
    private boolean promptFileChooser(){
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name())));
        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (file == null) 
            return false;
        dataFilePath = file.toPath();
        ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
        return true;
    }
}
