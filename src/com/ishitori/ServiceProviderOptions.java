package com.ishitori;

public class ServiceProviderOptions {
    private String appName;
    private String serviceAccountId;
    private String p12FilePath;

    public ServiceProviderOptions(String appName, String serviceAccountId, String p12FilePath)
    {
        this.appName = appName;
        this.serviceAccountId = serviceAccountId;
        this.p12FilePath = p12FilePath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceAccountId() {
        return serviceAccountId;
    }

    public void setServiceAccountId(String serviceAccountId) {
        this.serviceAccountId = serviceAccountId;
    }

    public String getP12FilePath() {
        return p12FilePath;
    }

    public void setP12FilePath(String p12FilePath) {
        this.p12FilePath = p12FilePath;
    }

    public static ServiceProviderOptions getDefault()
    {
        return new ServiceProviderOptions(
                "<App_name>",
                "<Your_Account>.gserviceaccount.com",
                "<Path_To_Your_P12_File>");
    }
}
