package ie.nuigalway.topology.api.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ie.nuigalway.topology.util.export.ExportInputUtil;

public class ReportRequestModel {
    
    public static ReportRequestModel valueOf(String s) {
        ObjectReader reader = new ObjectMapper().reader(ReportRequestModel.class);
        
        try {
            return reader.readValue(s);
        } catch (IOException e) {
            e.printStackTrace();
            return new ReportRequestModel();
        }
    }
    //this constructor is called when the user send request for export
    public ReportRequestModel(List<LsaModel> lsaModel) {
        
        ExportInputUtil exportUtil = new ExportInputUtil();
        List<InputCell> listInputCellHeaders = new ArrayList<InputCell>();
        
        List<InputCell> headers = exportUtil.generateLsaHeaders(listInputCellHeaders);
        this.getHeaders().addAll(headers);
        
        for(LsaModel  lsa : lsaModel) {
            List<InputCell> listInputCell = new ArrayList<InputCell>();
            List<InputCell> row = exportUtil.generateLsaRow(listInputCell, lsa);
            this.getRows().add(row);
        }
    }
    
    public ReportRequestModel(List<NetworkLsaModel> netLsaModel, boolean bool) {
        
        ExportInputUtil exportUtil = new ExportInputUtil();
        List<InputCell> listInputCellHeaders = new ArrayList<InputCell>();
        
        List<InputCell> headers = exportUtil.generateNetHeaders(listInputCellHeaders);
        this.getHeaders().addAll(headers);
        
        for(NetworkLsaModel  lsa : netLsaModel) {
            List<InputCell> listInputCell = new ArrayList<InputCell>();
            List<InputCell> row = exportUtil.generateNetRow(listInputCell, lsa);
            this.getRows().add(row);
        }
    }
    
    public ReportRequestModel(List<RouterLsaModel> routLsaModel, String a) {
        
        ExportInputUtil exportUtil = new ExportInputUtil();
        List<InputCell> listInputCellHeaders = new ArrayList<InputCell>();
        
        List<InputCell> headers = exportUtil.generateRouterHeaders(listInputCellHeaders);
        this.getHeaders().addAll(headers);
        
        for(RouterLsaModel  lsa : routLsaModel) {
            List<InputCell> listInputCell = new ArrayList<InputCell>();
            List<InputCell> row = exportUtil.generateRouterRow(listInputCell, lsa);
            this.getRows().add(row);
        }
    }
    
    public ReportRequestModel() {}
    
    @JsonProperty
    private List<InputCell> headers = new ArrayList<>();
    
    @JsonProperty
    private List<List<InputCell>> rows = new ArrayList<>();
    
    public List<InputCell> getHeaders() {
        return headers;
    }

    public List<List<InputCell>> getRows() {
        return rows;
    }

    public static class InputCell {
        
        @JsonProperty
        private String attribute;
        
        @JsonProperty
        private String value;

        public String getAttribute() {
            return attribute;
        }

        public String getValue() {
            return value;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
    
}

