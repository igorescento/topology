package ie.nuigalway.topology.util.export;

import java.util.List;

import ie.nuigalway.topology.api.model.LsaModel;
import ie.nuigalway.topology.api.model.NetworkLsaModel;
import ie.nuigalway.topology.api.model.ReportRequestModel.InputCell;
import ie.nuigalway.topology.api.model.RouterLsaModel;
import ie.nuigalway.topology.api.resources.IPv4Converter;

public class ExportInputUtil {
	
	InputCell inputCell;

	public List<InputCell> generateLsaHeaders(List<InputCell> listInputCellHeaders) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue("ID");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("instance");
		inputCell.setValue("Instance");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("area");
		inputCell.setValue("Area");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue("Type");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("originator");
		inputCell.setValue("Originator");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("sequence");
		inputCell.setValue("Sequence");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("age");
		inputCell.setValue("Age");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("checksum");
		inputCell.setValue("Checksum");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("options");
		inputCell.setValue("Options");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("body");
		inputCell.setValue("Body");
		
		listInputCellHeaders.add(inputCell);

		return listInputCellHeaders;
	}

	public List<InputCell> generateLsaRow(List<InputCell> listInputCell, LsaModel lsaModel) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue(IPv4Converter.longToIpv4(lsaModel.getId()));

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("instance");
		inputCell.setValue(lsaModel.getInstance());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("area");
		inputCell.setValue(lsaModel.getArea());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue(lsaModel.getType());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("originator");
		inputCell.setValue(IPv4Converter.longToIpv4(lsaModel.getOriginator()));

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("sequence");
		inputCell.setValue(lsaModel.getSequence());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("age");
		inputCell.setValue(String.valueOf(lsaModel.getAge()));

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("checksum");
		inputCell.setValue(lsaModel.getChecksum());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("options");
		inputCell.setValue(lsaModel.getOptions());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("body");
		inputCell.setValue(lsaModel.getBody());

		listInputCell.add(inputCell);

		return listInputCell;
	}
	
	public List<InputCell> generateNetHeaders(List<InputCell> listInputCellHeaders) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue("ID");

		listInputCellHeaders.add(inputCell);
		
		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue("Type");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("netmask");
		inputCell.setValue("Netmask");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("routersid");
		inputCell.setValue("IDs of Routers");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("numrouters");
		inputCell.setValue("# of Routers");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("originator");
		inputCell.setValue("Originator");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("firstaddr");
		inputCell.setValue("First Address");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("lastaddr");
		inputCell.setValue("Last Address");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("networkaddr");
		inputCell.setValue("Network Address");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("broadcastaddr");
		inputCell.setValue("Broadcast Address");
		
		listInputCellHeaders.add(inputCell);
		
		inputCell = new InputCell();
		inputCell.setAttribute("ipavailable");
		inputCell.setValue("IPs Available");
		
		listInputCellHeaders.add(inputCell);

		return listInputCellHeaders;
	}

	public List<InputCell> generateNetRow(List<InputCell> listInputCell, NetworkLsaModel lsaModel) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue(lsaModel.getId());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue(lsaModel.getType());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("netmask");
		inputCell.setValue(lsaModel.getNetmask());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("routersid");
		inputCell.setValue(lsaModel.getRoutersid());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("numrouters");
		inputCell.setValue(String.valueOf(lsaModel.getNumrouters()));

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("originator");
		inputCell.setValue(lsaModel.getOriginator());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("firstaddr");
		inputCell.setValue(String.valueOf(lsaModel.getFirstaddr()));

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("lastaddr");
		inputCell.setValue(lsaModel.getLastaddr());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("networkaddr");
		inputCell.setValue(lsaModel.getNetworkaddr());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("broadcastaddr");
		inputCell.setValue(lsaModel.getBroadcastaddr());

		listInputCell.add(inputCell);
		

		inputCell = new InputCell();
		inputCell.setAttribute("ipavailable");
		inputCell.setValue(String.valueOf(lsaModel.getIpavailable()));

		listInputCell.add(inputCell);

		return listInputCell;
	}
	
	public List<InputCell> generateRouterHeaders(List<InputCell> listInputCellHeaders) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue("ID");

		listInputCellHeaders.add(inputCell);
		
		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue("Type");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("linktype");
		inputCell.setValue("Link Type");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("bodyid");
		inputCell.setValue("Body ID (DR's Interface)");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("data");
		inputCell.setValue("Data (Router's Interface)");

		listInputCellHeaders.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("metric");
		inputCell.setValue("Metric");

		listInputCellHeaders.add(inputCell);

		return listInputCellHeaders;
	}

	public List<InputCell> generateRouterRow(List<InputCell> listInputCell, RouterLsaModel lsaModel) {

		inputCell = new InputCell();
		inputCell.setAttribute("id");
		inputCell.setValue(lsaModel.getId());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("type");
		inputCell.setValue(lsaModel.getType());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("linktype");
		inputCell.setValue(lsaModel.getLinktype());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("bodyid");
		inputCell.setValue(lsaModel.getBodyid());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("data");
		inputCell.setValue(lsaModel.getData());

		listInputCell.add(inputCell);

		inputCell = new InputCell();
		inputCell.setAttribute("metric");
		inputCell.setValue(String.valueOf(lsaModel.getMetric()));

		listInputCell.add(inputCell);

		return listInputCell;
	}
}
