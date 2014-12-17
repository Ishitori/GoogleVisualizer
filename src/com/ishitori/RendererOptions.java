package com.ishitori;

public class RendererOptions {
    private String fileKey;
    private String fileName;
    private String dataWorksheetTitle;
    private Boolean cumulativeUpdate;

    public RendererOptions(String fileKey, String fileName, String dataWorksheetTitle, Boolean cumulativeUpdate) {
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.dataWorksheetTitle = dataWorksheetTitle;
        this.cumulativeUpdate = cumulativeUpdate;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDataWorksheetTitle() {
        return dataWorksheetTitle;
    }

    public Boolean getCumulativeUpdate() {
        return cumulativeUpdate;
    }

    public void setCumulativeUpdate(Boolean cumulativeUpdate) {
        this.cumulativeUpdate = cumulativeUpdate;
    }
}
