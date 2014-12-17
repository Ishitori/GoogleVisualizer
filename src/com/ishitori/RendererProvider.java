package com.ishitori;

public class RendererProvider {
    private final ServiceProvider serviceProvider;

    public RendererProvider(ServiceProviderOptions options) throws Exception {
        this.serviceProvider = new ServiceProvider(options);
    }

    public DataTableRenderer getDataTableRenderer()
    {
        return new DataTableRenderer(this.serviceProvider);
    }
}
