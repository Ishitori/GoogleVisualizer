package com.ishitori;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws Exception {
	/* Usage example */
        RendererProvider provider = new RendererProvider(ServiceProviderOptions.getDefault());
        DataTableRenderer renderer = provider.getDataTableRenderer();
        Random randomGenerator = new Random();

        ArrayList<String> headers = new ArrayList<String>();
        headers.add("Column1");
        headers.add("Column2");
        headers.add("Column3");

        ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
        table.add(new ArrayList<String>());
        table.add(new ArrayList<String>());
        table.add(new ArrayList<String>());

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 5000; j++)
            {
                table.get(i).add(Integer.toString(randomGenerator.nextInt(1000)));
            }
        }

        renderer.Render(headers, table, new RendererOptions("DocumentKey", "Chart Title here", "SheetName", true));
    }
}
