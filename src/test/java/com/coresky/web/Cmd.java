package com.coresky.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Cmd {

    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("ls " + System.getProperties().get("user.dir"));
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}