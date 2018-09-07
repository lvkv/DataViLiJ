package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    DATA_CSS_PATH,
    TRANS_CLASS,
    
    /* button text */
    DISPLAY_BUTTON_TEXT,
    READONLY,
    AVGTXT,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,
    TAKESCREENSHOT,
    CHART,
    LOADEDDATAINFO,
    CONSISTSOF,
    IWANTTOSLEEP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    INVALID_DATA_MESSAGE,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    INVALID_DATA_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    IMGFILE,
    IMGEXT,
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    CHART_LABEL
}
