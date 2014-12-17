package com.ishitori;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import com.sun.org.apache.xml.internal.utils.XMLChar;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataTableRenderer {

    private final SpreadsheetService spreadsheetService;
    private final Drive driveService;
    private Random randomGenerator;

    public DataTableRenderer(ServiceProvider serviceProvider) {
        this.spreadsheetService = serviceProvider.getSpreadsheetService();
        this.driveService = serviceProvider.getDriveService();
        this.randomGenerator = new Random();
    }

    public String Render(ArrayList<String> headers, ArrayList<ArrayList<String>> dataTable, RendererOptions options) throws Exception {
        File spreadsheetFile = this.getSpreadsheetFile(options.getFileKey(), options.getFileName());
        SpreadsheetEntry spreadsheet = this.getSpreadsheet(spreadsheetFile);
        WorksheetEntry worksheet = this.getDataWorksheet(spreadsheet, options.getDataWorksheetTitle());

        this.RenderHeader(worksheet, headers);
        this.RenderBody(worksheet, dataTable, headers, options.getCumulativeUpdate());

        return spreadsheetFile.getId();
    }

    private void RenderHeader(WorksheetEntry worksheet, ArrayList<String> headers) throws IOException, ServiceException {
        URL cellFeedUrl = worksheet.getCellFeedUrl();

        CellQuery cellQuery = new CellQuery(cellFeedUrl);
        cellQuery.setMaximumRow(1);

        CellFeed topRowCellFeed = this.spreadsheetService.query(cellQuery, CellFeed.class);

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);

            CellEntry cellEntry = new CellEntry(1, i + 1, header);
            topRowCellFeed.insert(cellEntry);
        }
    }

    private void RenderBody(WorksheetEntry worksheet, ArrayList<ArrayList<String>> table, ArrayList<String> headers, Boolean isCumulativeUpdate) throws Exception {
        ListEntry row;
        URL listFeedUrl = worksheet.getListFeedUrl();
        ListFeed listFeed = this.spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
        List<ListEntry> entries = listFeed.getEntries();
        int startDataIndex = isCumulativeUpdate && table.get(0).size() > entries.size() ? entries.size() : 0;
        int startRowIndex = entries.size();
        ArrayList<String> cellHeaders = convertToCellHeaders(headers);

        for (int dataIndex = startDataIndex, rowIndex = startRowIndex; dataIndex < table.get(0).size(); dataIndex++, rowIndex++) {

            try
            {
                row = getRow(listFeed, entries, rowIndex);

                for (int j = 0; j < table.size(); j++)
                {
                    row.getCustomElements().setValueLocal(cellHeaders.get(j), table.get(j).get(dataIndex));
                }

                saveRow(listFeed, entries, row, rowIndex);
            }
            catch (Exception exc)
            {
                this.ProcessException(exc);
            }
        }
    }

    private void saveRow(ListFeed listFeed, List<ListEntry> entries, ListEntry row, int i) throws IOException, ServiceException {
        if (i < entries.size())
        {
            row.update();
        }
        else
        {
            listFeed.insert(row);
        }
    }

    private ListEntry getRow(ListFeed listFeed, List<ListEntry> entries, int rowIndex) {
        ListEntry row;
        if (rowIndex < entries.size())
        {
            row = entries.get(rowIndex);
        }
        else
        {
            row = listFeed.createEntry();
        }
        return row;
    }

    private void RenderBodyBatched(WorksheetEntry worksheet, ArrayList<ArrayList<String>> table) throws IOException, ServiceException {

        URL cellFeedUrl = worksheet.getCellFeedUrl();
        CellFeed batchRequest = new CellFeed();

        for (int i = 0; i < table.get(0).size(); i++) {
            for (int j = 0; j < table.size(); j++) {
                CellEntry batchOperation = createUpdateOperation(cellFeedUrl, i + 2, j + 1, table.get(j).get(i));
                batchRequest.getEntries().add(batchOperation);
            }
        }
        CellFeed feed = this.spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
        Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
        URL batchUrl = new URL(batchLink.getHref());
        CellFeed batchResponse = this.spreadsheetService.batch(batchUrl, batchRequest);

        // Print any errors that may have happened.
        boolean isSuccess = true;
        for (CellEntry entry : batchResponse.getEntries()) {
            String batchId = BatchUtils.getBatchId(entry);
            if (!BatchUtils.isSuccess(entry)) {
                isSuccess = false;
                BatchStatus status = BatchUtils.getBatchStatus(entry);
                System.out.println("\n" + batchId + " failed (" + status.getReason()
                        + ") " + status.getContent());
            }
        }
        if (isSuccess) {
            System.out.println("Batch operations successful.");
        }
    }
    private File getSpreadsheetFile(String fileKey, String fileName) throws IOException, ServiceException {
        File file = null;

        try {
            Drive.Files.Get getCommand = driveService.files().get(fileKey);
            file = getCommand.execute();
        } catch (IOException e) {
        }

        if (file == null)
        {
            file = new com.google.api.services.drive.model.File();
            file.setTitle(fileName);
            file.setMimeType("application/vnd.google-apps.spreadsheet");
            com.google.api.services.drive.Drive.Files.Insert insert = driveService.files().insert(file);
            file = insert.execute();
            this.setFilePermission(file.getId(), "<your_google_account_domain_here>.com", "domain", "writer");
        }

        return file;
    }
    private SpreadsheetEntry getSpreadsheet(File file) throws IOException, ServiceException {
        String spreadsheetURL = "https://spreadsheets.google.com/feeds/spreadsheets/" + file.getId();
        SpreadsheetEntry spreadsheet = this.spreadsheetService.getEntry(new URL(spreadsheetURL), SpreadsheetEntry.class);

        return spreadsheet;
    }
    private WorksheetEntry getDataWorksheet(SpreadsheetEntry spreadsheet, String dataWorksheetTitle) throws IOException, ServiceException {
        WorksheetFeed worksheetFeed = this.spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

        for (WorksheetEntry worksheet: worksheets)
        {
            if (worksheet.getTitle().getPlainText().equalsIgnoreCase(dataWorksheetTitle))
            {
                return worksheet;
            }
        }

        return worksheets.get(0);
    }
    private Permission setFilePermission(String fileId, String value, String type, String role) {
        Permission newPermission = new Permission();

        newPermission.setValue(value);
        newPermission.setType(type);
        newPermission.setRole(role);

        try {
            return this.driveService.permissions().insert(fileId, newPermission).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return null;
    }

    private CellEntry createUpdateOperation(URL cellFeedUrl, int row, int col, String value)
            throws ServiceException, IOException {
        String batchId = "R" + row + "C" + col;
        URL entryUrl = new URL(cellFeedUrl.toString() + "/" + batchId);
        CellEntry entry = this.spreadsheetService.getEntry(entryUrl, CellEntry.class);
        entry.changeInputValueLocal(value);
        BatchUtils.setBatchId(entry, batchId);
        BatchUtils.setBatchOperationType(entry, BatchOperationType.UPDATE);

        return entry;
    }

    private ArrayList<String> convertToCellHeaders(ArrayList<String> headers) {
        ArrayList<String> cellHeaders = new ArrayList<String>();

        for (String header: headers)
        {
            String cellHeader = "";

            for (int i = 0; i < header.length(); i++)
            {
                char c = header.charAt(i);
                if (!XMLChar.isInvalid(c) && c != ' ' && c != ',' && c != '%')
                {
                    cellHeader += c;
                }
            }

            cellHeaders.add(cellHeader);
        }

        return cellHeaders;
    }

    private void ProcessException(Exception exc) throws Exception {
        HttpResponseException httpException = (exc instanceof HttpResponseException ? (HttpResponseException) exc : null);
        ServiceException serviceException = (exc instanceof ServiceException ? (ServiceException) exc : null);

        if (httpException != null)
        {
            int statusCode = httpException.getStatusCode();

            if (statusCode >= 400 && statusCode <= 499)
            {
                Thread.sleep(5000 + this.randomGenerator.nextInt(1001));
            }
            else
            {
                throw exc;
            }
        }
        else if (serviceException != null)
        {
            Thread.sleep(5000 + this.randomGenerator.nextInt(1001));
        }
        else
        {
            throw exc;
        }
    }
}
