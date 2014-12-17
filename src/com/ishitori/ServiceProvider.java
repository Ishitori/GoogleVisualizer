package com.ishitori;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServiceProvider {

    private SpreadsheetService spreadsheetService;
    private Drive driveService;

    private GoogleCredential credential;
    private ServiceProviderOptions options;

    public ServiceProvider(ServiceProviderOptions options) throws Exception {
        this.options = options;

        File file = new File(this.options.getP12FilePath());
        ArrayList<String> scopes = new ArrayList<String>();

        scopes.add(0, DriveScopes.DRIVE);
        scopes.add(1, "https://spreadsheets.google.com/feeds");

        this.credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new JacksonFactory())
                .setServiceAccountId(this.options.getServiceAccountId())
                .setServiceAccountPrivateKeyFromP12File(file)
                .setServiceAccountScopes(scopes)
                .build();

        this.credential.refreshToken();


    }

    public SpreadsheetService getSpreadsheetService() {
        if (this.spreadsheetService == null)
        {
            this.spreadsheetService = new SpreadsheetService(this.options.getAppName());
            this.spreadsheetService.setOAuth2Credentials(credential);
        }

        return this.spreadsheetService;
    }

    public Drive getDriveService() {
        if (this.driveService == null)
        {
            this.driveService = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                    .setApplicationName(this.options.getAppName())
                    .build();
        }

        return this.driveService;
    }
}
