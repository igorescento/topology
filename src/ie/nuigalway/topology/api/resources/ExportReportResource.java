package ie.nuigalway.topology.api.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.api.model.LsaModel;
import ie.nuigalway.topology.api.model.NetworkLsaModel;
import ie.nuigalway.topology.api.model.ReportRequestModel;
import ie.nuigalway.topology.api.model.ReportRequestModel.InputCell;
import ie.nuigalway.topology.api.model.RouterLsaModel;
import ie.nuigalway.topology.domain.dao.hibernate.LsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.Lsa;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;
import ie.nuigalway.topology.util.export.WorkbookSingleton;    	

@Path("generate")
public class ExportReportResource {

	private static final String REGULAR_STYLE = "regular";
	private static final String HEADER_STYLE = "header";
	private static final String TITLE_STYLE = "title";

	private SessionFactory sessionFactory;
	private LsaDAO lsaDAOHibernate;
	private RouterLsaDAO routerLsaDAO;
	private NetworkLsaDAO netLsaDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.lsaDAOHibernate = new LsaDAO(sessionFactory);
		this.routerLsaDAO = new RouterLsaDAO(sessionFactory);
		this.netLsaDAO = new NetworkLsaDAO(sessionFactory);
	}

	private static Map<String, CellStyle> createCellStyles (Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String,CellStyle>();

		styles.put(REGULAR_STYLE, createBorderedStyle(wb));
		styles.put(HEADER_STYLE, createHeaderStyle(wb));
		styles.put(TITLE_STYLE, createTitleStyle(wb));

		return styles;		
	}

	private static CellStyle createBorderedStyle(Workbook wb){
		CellStyle style = wb.createCellStyle();

		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());

		return style;
	}

	private static CellStyle createTitleStyle(Workbook wb) {
		CellStyle titleStyle = createBorderedStyle(wb);

		Font headerFont = wb.createFont();
		headerFont.setBold(true);

		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		titleStyle.setFont(headerFont);

		return titleStyle;
	}

	private static CellStyle createHeaderStyle(Workbook wb) {
		CellStyle headerStyle = createBorderedStyle(wb);

		Font headerFont = wb.createFont();
		headerFont.setBold(true);

		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(headerFont);

		return headerStyle;
	}

	/**
	 * Given some report input, add it to the given sheet.
	 * 
	 * @param input header- and data information
	 * @param workbook
	 * @param sheet object representing excel sheet
	 * @return same sheet with data inserted
	 */
	private static Sheet inputToSheet(ReportRequestModel input, Workbook wb, Sheet sheet) {

		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);

		//the following three statements are required only for HSSF
		sheet.setAutobreaks(true);
		printSetup.setFitHeight((short)1);
		printSetup.setFitWidth((short)1);

		//create header rows
		Row headerRow = sheet.createRow(0);

		//Styles and epic list count as a title row
		Map<String, CellStyle> styles = createCellStyles(wb);


		//fill in the header row and generate a mapping for ids -> colnum
		ListIterator<InputCell> it = input.getHeaders().listIterator();
		Map<String,Integer> headerColnums = new HashMap<>();
		while (it.hasNext()) {

			int colnum = it.nextIndex();
			InputCell headerCell = it.next();

			Cell cell = headerRow.createCell(colnum);
			cell.setCellValue(headerCell.getValue());
			cell.setCellStyle(styles.get(HEADER_STYLE));

			headerColnums.put(headerCell.getAttribute(), colnum);
		}

		Row row;
		Cell cell;
		ListIterator<List<InputCell>> rowIt = input.getRows().listIterator();
		while (rowIt.hasNext()) {
			int rownum = rowIt.nextIndex() + 1;
			List<InputCell> rowCells = rowIt.next();

			row = sheet.createRow(rownum);
			if (rowCells.isEmpty()) continue;

			for (InputCell dataCell : rowCells) {
				Integer colnum = headerColnums.get(dataCell.getAttribute());
				if (colnum == null) {
					System.err.println("Missing header information for '" + dataCell.getAttribute() + "'");
					break;
				}

				cell = row.createCell(colnum);
				cell.setCellValue(dataCell.getValue());
			}
		}

		// Automatically size all columns based on contents
		for (int i=0; i < input.getRows().size(); i++) {
			sheet.autoSizeColumn(i);
		}

		sheet.setAutoFilter(new CellRangeAddress(sheet.getFirstRowNum(), sheet.getLastRowNum(), 0, headerRow.getPhysicalNumberOfCells() - 1));
		return sheet;
	}

	/**
	 * Create a JAX-RS Response that will stream the given workbook back.
	 * 
	 * @param workbook Excel workbook to put in the streaming response
	 * @param filename filename to suggest to the user.
	 * @return
	 */
	private static Response workbookToResponse(final Workbook workbook, String filename) {

		if (filename == null || filename.isEmpty()) {
			filename = "export.xls";
		}

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
			WebApplicationException {
				try {
					workbook.write(output);
				} catch (Exception e) {
					throw new WebApplicationException();
				}
			}
		};

		return Response.ok(stream)
				.header("content-disposition","attachment; filename = " + filename)
				.build();
	}

	/**
	 * Download the report that was generated last for this user.
	 * 
	 * @return
	 */
	@GET
	@Path("download/{id}")
	@Produces("application/xls")
	public Response downloadMRReport(@Context HttpServletRequest request, @PathParam("id") String id) {

		HSSFWorkbook workbook = WorkbookSingleton.getInstance().getWorkbook(id);

		if (workbook == null) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		WorkbookSingleton.getInstance().removeWorkbook(id);

		return workbookToResponse(workbook, "report.xls");
	}
	
	/**
	 * Download the report that was generated last for this user.
	 * 
	 * @return
	 */
	@GET
	@Path("downloadsvg")
	@Produces("application/xml")
	public Response downloadTopologySvg(@Context HttpServletRequest request) {
		
		File f = new File("topology.svg");
		ResponseBuilder response;
		response = Response.ok((Object) f);
		response.header("content-disposition","attachment; filename = topology.svg");
		
		return response.build();
	}

	@POST
	@Path("report")
	@Consumes(MediaType.TEXT_PLAIN)
	public String generateGeneralEpicReport(String svg) {
		
		try {
			//create svg
			if(svg.trim().length() > 0){
				BufferedWriter out = new BufferedWriter(new FileWriter("topology.svg"));
				out.write(svg);
				out.close();
			}
			
			List<LsaModel> lsaModelList = new ArrayList<LsaModel>();
			List<NetworkLsaModel> netLsaModelList = new ArrayList<>();
			List<RouterLsaModel> routerLsaModelList = new ArrayList<>();

			try {
				sessionFactory.getCurrentSession().getTransaction().begin();

				Collection<Lsa> lsaList = lsaDAOHibernate.findAll();
				Collection<NetworkLsa> netList = netLsaDAO.findAll();
				Collection<RouterLsa> routList = routerLsaDAO.findAll();

				for(Lsa l : lsaList) {
					lsaModelList.add(new LsaModel(l));
				}

				for(NetworkLsa nlsa : netList) {
					netLsaModelList.add(new NetworkLsaModel(nlsa));
				}

				for(RouterLsa rlsa : routList){
					routerLsaModelList.add(new RouterLsaModel(rlsa));
				}

				sessionFactory.getCurrentSession().getTransaction().commit();

			} catch (HibernateException e) {
				e.printStackTrace();
				sessionFactory.getCurrentSession().getTransaction().rollback();
				throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
						"Internal problem", "Error on trying to retrieve data: "
								+ e.getMessage());
			}
			HSSFWorkbook workbook = new HSSFWorkbook();
			ReportRequestModel rRequestLsa = new ReportRequestModel(lsaModelList);
			ReportRequestModel rRequestNet = new ReportRequestModel(netLsaModelList, true);
			ReportRequestModel rRequestRouter = new ReportRequestModel(routerLsaModelList, "routers");

			inputToSheet(rRequestLsa, workbook, workbook.createSheet("LSA"));
			inputToSheet(rRequestNet, workbook, workbook.createSheet("Networks"));
			inputToSheet(rRequestRouter, workbook, workbook.createSheet("Routers"));

			String id = generateUUID();
			WorkbookSingleton.getInstance().addWorkbook(id, workbook);

			return id;
		}catch(Exception e) {
			e.printStackTrace();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR, "emptylist-exception",
					"There is no data returned: " + e.getMessage());
		}
	}

	public String generateUUID() {
		return UUID.randomUUID().toString();
	}


}