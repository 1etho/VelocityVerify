package com.etho.discordlink.utils.sql;

import com.etho.discordlink.Discordlink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;

public class JDBC {

    public static void inject() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            if (!new File(Discordlink.getDataFolder() + "/jdbc.jar").exists()) download();
            Discordlink.server().getPluginManager().addToClasspath(Discordlink.class, Paths.get(new File(Discordlink.getDataFolder() + "/jdbc.jar").getAbsolutePath()));
        }
    }

    private static void download() {
        File tmp = new File("tmp_vbans/");
        tmp.mkdir();
        Path zip = Paths.get(tmp.getAbsolutePath(), "jdbc.zip");
        try {
            URL jdbc = new URL("https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.20.zip");
            try (InputStream in = jdbc.openStream()) {
                Files.copy(in, zip, StandardCopyOption.REPLACE_EXISTING);
            }
            try (FileSystem zipFileSystem = FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                Files.copy(zipFileSystem.getPath("mysql-connector-java-8.0.20/mysql-connector-java-8.0.20.jar"), Paths.get(Discordlink.getDataFolder().getAbsolutePath(), "jdbc.jar"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cant download JDBC!");
        }
        try {
            Files.delete(Paths.get(tmp.getAbsolutePath(), "jdbc.zip"));
            Files.delete(Paths.get(tmp.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}