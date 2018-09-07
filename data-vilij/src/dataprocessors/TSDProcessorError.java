package dataprocessors;

/**
 * @author Lukas Velikov
 */
public abstract class TSDProcessorError extends Exception{
    private Integer lineNumber; // line in tsd text box where error occurs
    
    public TSDProcessorError(String error_msg, Integer ln){
        super(error_msg + "\nError occurred at line " + ln + ".");      
        lineNumber = ln;
    }
    
//    public TSDProcessorError(String error_msg){
//        super(error_msg+ "\nError occurred at line " + ln + ".");
//        lineNumber = null;
//    }
    
    public int getLineNumber(){
        return lineNumber;
    }
}
