package edu.raf.sofkom.localstorage;

import edu.raf.sofkom.FileStorage;
import edu.raf.sofkom.privileges.PrivilegeException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainTest {
    public static void main(String[] args) throws IOException, PrivilegeException {
        FileStorage fs = new LocalStorage();
        Path toStorage = Paths.get(System.getProperty("user.home"),"Desktop");
        try {
            fs.init(String.valueOf(toStorage),"storage");
        } catch (IOException e) {
            e.printStackTrace();
        }

        fs.getStorageUsers().init("bane","bane");

        Path toStore = Paths.get("pom.xml");
        System.out.println(Paths.get(fs.getPathToStorage()).resolve(toStore).normalize());
        fs.store("",toStore.toString());


    }

}
