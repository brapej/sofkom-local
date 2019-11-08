package edu.raf.sofkom.localstorage;

import edu.raf.sofkom.FileStorage;
import edu.raf.sofkom.UnsuportedTypeException;
import edu.raf.sofkom.privileges.Privilege;
import edu.raf.sofkom.privileges.PrivilegeException;


import java.io.*;
import java.nio.file.*;

@SuppressWarnings("unused")
public class LocalStorage extends FileStorage  implements Serializable  {

    private static final long serialVersionUID = 1L;



    @Override
    public void init(String pathToParent,String storageName) throws IOException {

        Path toStorage = Paths.get(pathToParent,storageName);
        this.setPathToStorage(mkdir(toStorage.toString()));
        this.setPathToDownloads(mkdir(toStorage.toFile().getParent()+File.separator+storageName+"-downloads"));
    }

    public void init(Path pathToStorage,String storageName) throws IOException {
        init(pathToStorage.toString(),storageName);
    }

    @Override
    public boolean store(String to, String from) throws PrivilegeException, IOException, UnsuportedTypeException {
        return store(Paths.get(to),Paths.get(from));
    }

    @Override
    public boolean store(String to, String... from) throws PrivilegeException, IOException, UnsuportedTypeException {
        for(String s:from){
            store(to,s);
        }
        return true;
    }

    @Override
    public boolean store(Path to, Path from) throws PrivilegeException, IOException, UnsuportedTypeException {

        if(!getStorageUsers().getCurrentUser().checkPrivilege(Privilege.S) && !getStorageUsers().ifSuperUser())
            throw new PrivilegeException("No required privilege.");

        if(getFiletypeRestrictions().contains(from.getFileName().toString().substring(from.getFileName().toString().lastIndexOf('.'))))
            throw new UnsuportedTypeException("Filetype restricted.");

        else if(!(getStorageUsers().getCurrentUser() == null)) {

            System.out.println("Files.exists(to):"+Files.exists(toStoragePath(to).resolve(from))+"-"+toStoragePath(to).resolve(from).toString());
            System.out.println("Files.exists(from):"+Files.exists(from)+"-"+from.toString());
            String finalTo = Paths.get(toStoragePath(to).toString(),from.toString()).normalize().toString();
            System.out.println(finalTo);

            if (!Files.exists(toStoragePath(to).resolve(from))) {
                if (Files.exists(from)) {
                    System.out.println("******:"+from.normalize().toString()+toStoragePath(to).toString());
                    Files.copy(from.normalize(), Paths.get(finalTo),StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
                throw new FileNotFoundException(from.toString());
            }
            throw new FileAlreadyExistsException(to.toString());
        }
        return false;
    }

    @Override
    public boolean store(Path to, Path... from) throws IOException, PrivilegeException, UnsuportedTypeException {
        for(Path p : from){
            store(to,p);
        }
        return true;
    }

    @Override
    public boolean retrieve(String from) throws PrivilegeException, IOException {
        return retrieve(Paths.get(from));
    }

    @Override
    public boolean retrieve(Path from) throws PrivilegeException, IOException {
        if(!getStorageUsers().getCurrentUser().checkPrivilege(Privilege.R) && !getStorageUsers().ifSuperUser())
            throw new PrivilegeException("No required privilege.");

        if(Files.exists(from)){
            Files.copy(toStoragePath(from), Paths.get(getPathToDownloads(),from.toFile().getName()));
            return true;
        }
        throw new FileNotFoundException(from.toString()+File.separator+from.toFile().getName());
    }

    @Override
    public boolean delete(String path) throws PrivilegeException, IOException {

        return delete(Paths.get(path));
    }

    @Override
    public boolean delete(Path path) throws PrivilegeException, IOException {
        if(!getStorageUsers().getCurrentUser().checkPrivilege(Privilege.D) && !getStorageUsers().ifSuperUser())
            throw new PrivilegeException("No required privilege.");


        if(Files.exists(path)) {
            Files.delete(path);
            return true;
        }
            throw new FileNotFoundException(path.toString());

    }



    public String mkdir(String atPath) throws IOException {

        if(atPath == null) {
            return mkdir("");
        }

        if(Files.notExists(Paths.get(atPath))) {
            Files.createDirectory(Paths.get(atPath));
            return atPath;
        }

        char c = atPath.charAt(atPath.length()-1);

        return Character.isDigit(c)
                ?
                mkdir(atPath.substring(0,atPath.length()-1)+Integer.toString(Character.getNumericValue(c)+1))
                :
                mkdir(atPath+Integer.toString(1));
        
    }

}

