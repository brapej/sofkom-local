package edu.raf.sofkom.localstorage;

import edu.raf.sofkom.FileStorage;
import edu.raf.sofkom.privileges.PrivilegeException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

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

        HashMap<String,String> meta = new HashMap<>();
        //String key = "";
        boolean more=true;
        Scanner sc = new Scanner(System.in);
        String key,value;
        while(more){
            System.out.println("key:");
            key = sc.nextLine();
            System.out.println("value:");
            value=sc.nextLine();
            meta.put(key,value);

            System.out.println("More (y/n)?");
            key=sc.nextLine();
            if(key.trim().equals("y"))
                continue;
            else if(key.trim().equals("n"))
                break;
            else
                System.out.println("Y/N ?!");

        }

       // fs.store("",toStore.toString(),meta);
       // System.out.println(fs.readFileMeta(Paths.get("",toStore.toString()).toString()));
        //fs.retrieve("pom.xml");


    }

}
