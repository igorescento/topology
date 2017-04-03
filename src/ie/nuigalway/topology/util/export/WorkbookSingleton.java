package ie.nuigalway.topology.util.export;

import java.util.Hashtable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class WorkbookSingleton {
    
    private static Hashtable<String, HSSFWorkbook> workbookMap;

    private static final WorkbookSingleton INSTANCE = new WorkbookSingleton(); 

    private WorkbookSingleton() {
        workbookMap = new Hashtable<String, HSSFWorkbook>();
    }

    public static WorkbookSingleton getInstance() {
        return INSTANCE;
    }
    
    public static synchronized Hashtable<String, HSSFWorkbook> getWorkbookMap() {
        return workbookMap;
    }

    public final HSSFWorkbook getWorkbook(String id) {
        return getWorkbookMap().get(id); 
    }
    
    public void addWorkbook(String id , HSSFWorkbook workbook) {
        getWorkbookMap().put(id, workbook);
    }

    public void removeWorkbook(String id) {
        getWorkbookMap().remove(id);
    }

}
