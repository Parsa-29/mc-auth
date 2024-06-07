package com.mcgeo.auth.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.mcgeo.auth.classes.User;

public class SaveUsers {
    private File dataFile;

    public SaveUsers(File dataFile) {
        this.dataFile = dataFile;
    }

    public void saveUsers(List<User> users) throws IOException {
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(users, writer);
        writer.close();
    }
}
