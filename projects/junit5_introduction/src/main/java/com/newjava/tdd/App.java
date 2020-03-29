package com.newjava.tdd;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class App {

    private ZoneService zoneService;

    public App(String filePath) throws IOException  {
        zoneService = new ZoneServiceImpl(filePath);
    }

    public static void main(String[] args) {
        String filePath = "zone_info.txt";
        if (args.length > 0) {
            filePath = args[0];
        }

        App app =null;
        try {
            app = new App(filePath);
        } catch (IOException e) {
            System.out.println("Can't find the file " + filePath);
            System.exit(1);
        }

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String prompt = "Please input a zone abbreviation, type exit to quit the program!";
            System.out.println(prompt);
            while(scanner.hasNext()) {
                String zoneAbbreviation = scanner.next();
                if ("exit".equalsIgnoreCase(zoneAbbreviation.trim())) {
                    System.exit(0);
                }

                String response = app.zoneService.zoneNameWithGmtOffice(zoneAbbreviation);
                if (Objects.isNull(response) || "".equals(response)) {
                    System.out.println("NOT FOUND");
                } else {
                    System.out.println(response);
                }
            }
        }
    }
}
