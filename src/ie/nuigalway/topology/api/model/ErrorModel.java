package ie.nuigalway.topology.api.model;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="error")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class ErrorModel {
    
    private String shortError;
    private String fullError;
    private int errorCode;
    
    public ErrorModel() { }
    
    public ErrorModel(int errorCode, String shortError, String fullError) {
        this.shortError = shortError;
        this.fullError = fullError;
        this.errorCode = errorCode;
    }
    
    public String getShortError() {
        return shortError;
    }
    public void setShortError(String shortError) {
        this.shortError = shortError;
    }
    public String getFullError() {
        return fullError;
    }
    public void setFullError(String fullError) {
        this.fullError = fullError;
    }
    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}