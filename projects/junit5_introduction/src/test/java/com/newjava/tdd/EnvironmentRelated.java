package com.newjava.tdd;

import java.io.File;

public class EnvironmentRelated {
    public static String generatePath(String pathPart1, String pathPart2) {
        return pathPart1 + File.separator + pathPart2;
    }
}
