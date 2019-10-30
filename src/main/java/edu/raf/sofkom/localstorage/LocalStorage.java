package edu.raf.sofkom.localstorage;

import edu.raf.sofkom.FileStorage;
import edu.raf.sofkom.NoUserException;
import edu.raf.sofkom.PrivilegeException;
import edu.raf.sofkom.model.Privilege;
import edu.raf.sofkom.model.User;


import java.io.*;
import java.nio.file.*;

public class LocalStorage extends FileStorage  implements Serializable  {

    private static final long serialVersionUID = 1L;

    public LocalStorage(){
        super();
    }

    public LocalStorage(String userName,String password) {
        super(userName,password);
    }
    @Override
    public boolean init(String pathToStorageStr) throws FileAlreadyExistsException {
        Path path = Paths.get(pathToStorageStr);
        Path storagePath = path.resolve("sofkom-storage");
        Path downloadsPath = Paths.get(System.getProperty("user.home"),"storage-downloads");

        try {
            if(Files.exists(storagePath)){
                return false;
            }
            Files.createDirectory(storagePath);
            setPathToStorage(storagePath.toString()/**/);
        } catch (IOException e) {
            throw new FileAlreadyExistsException(storagePath.toString());
        }

        try {
            Files.createDirectory(downloadsPath);
            setPathToDownloads(downloadsPath.toString()/**/);

        } catch (IOException e) {
            throw new FileAlreadyExistsException(downloadsPath.toString());
        }

        return true;

    }

    @Override
    public boolean store(String toStorage, String filePath) throws PrivilegeException, NoUserException {
        if(getCurrentUser() == null)
            throw new NoUserException("No current user") ;

        if(!getCurrentUser().checkPrivilege(Privilege.S)) {
            throw new PrivilegeException(getCurrentUser().getUserName() + " have no permission.");
        }
        store(Paths.get(toStorage),Paths.get(filePath));
        return true;
    }


    @Override
    public boolean store(String toStorage, String... filePaths) throws PrivilegeException,NoUserException {

        for(String filePath:filePaths)
             store(Paths.get(toStorage),Paths.get(filePath));
        return true;
    }

    @Override
    public boolean store(Path toStorage, Path filePath) throws PrivilegeException,NoUserException {
        if(getCurrentUser() == null)
            throw new NoUserException("No current user") ;

        if(!getCurrentUser().checkPrivilege(Privilege.S)) {
            throw new PrivilegeException(getCurrentUser().getUserName() + " have no permission.");
        }

        try {

            Path fileabs = filePath.toAbsolutePath();
            Path storerel=storageRelativePath(filePath);
            Files.copy(fileabs,storerel,StandardCopyOption.REPLACE_EXISTING);

        }
        catch (IOException e) {
            System.out.println("io");
            e.printStackTrace();
            //TODO store IOE?
        }

        return true;
    }
    @Override
    public boolean store(Path toStorage, Path... filePaths) throws PrivilegeException,NoUserException {

        for (Path filePath:filePaths) {
            store(toStorage,filePath);
        }
        return true;

    }
    @Override
    public boolean retrieve(String from) throws PrivilegeException,NoUserException{

        if(getCurrentUser() == null)
            throw new NoUserException("No current user") ;

        if(!getCurrentUser().checkPrivilege(Privilege.S)) {
            throw new PrivilegeException(getCurrentUser().getUserName() + "no" +Privilege.S.toString()+"permission.");
        }



        try {
            if(!Files.exists(Paths.get(getPathToDownloads()).resolve(Paths.get(from).getFileName()))) {
                Files.copy(Paths.get(from).toAbsolutePath(), Paths.get(getPathToDownloads()/**/).resolve(Paths.get(from).getFileName()));
                return true;
            }
        }
        catch (FileNotFoundException fnfe){
            System.err.println("fnf");
        }
        catch (IOException e) {

           System.err.println("Ioex: retrieve");
            //TODO retrieve IOE
        }
        return true;

    }

    @Override
    public boolean delete(String toDelete) throws PrivilegeException,NoUserException{
        return delete(Paths.get(toDelete));
    }
    @Override
    public boolean delete(Path toDelete) throws PrivilegeException,NoUserException{

        if(getCurrentUser() == null)
            throw new NoUserException("No current user") ;

        if(!getCurrentUser().checkPrivilege(Privilege.D)) {
            throw new PrivilegeException(getCurrentUser().getUserName() + "no" +Privilege.S.toString()+"permission.");
        }

        try {
            Files.delete(toDelete);
        } catch (IOException e) {
            System.err.println("delete IOex");
        }
        return true;
    }


    protected boolean stateSave() throws IOException {

            Path stateFile = Paths.get(getPathToStorage(),"state.txt");
            if(Files.exists(stateFile)){
                Files.delete(stateFile);
            }

            FileOutputStream f = new FileOutputStream(new File(getPathToStorage()+File.separator+"state.txt"));
            ObjectOutputStream o = new ObjectOutputStream(f);

            // Write objects to file
            o.writeObject(this);

            o.close();
            f.close();


        return true;

    }

    protected LocalStorage stateLoad(Path from) throws IOException, ClassNotFoundException {
        return   stateLoad(from.toString());
    }

    private LocalStorage stateLoad(String from) throws ClassNotFoundException, IOException {
        LocalStorage ls = new LocalStorage();

            FileInputStream fi = new FileInputStream(new File(from));
            ObjectInputStream oi = new ObjectInputStream(fi);
            // Read objects


            ls = (LocalStorage)oi.readObject();

            oi.close();
            fi.close();

        return ls;

    }

    public boolean connect(String userName,String password, String pathToStorage) throws IOException, ClassNotFoundException {
        if(!getUsers().containsKey(userName))
            if(!getUsers().get(userName).getPassword().equals(password))
                return false;
        stateLoad(Paths.get(pathToStorage,"state.txt"));
        this.setCurrentUser(new User(userName,password));
        return true;
    }

    public void disconnect(Path pathToStorage) throws IOException, ClassNotFoundException {
        setCurrentUser(null);
        stateSave();
    }


    public static void main(String[] args) throws NoUserException, PrivilegeException, IOException, ClassNotFoundException {

        LocalStorage fs = /*new edu.raf.sofkom.local.LocalStorage();*/ new LocalStorage("mika", "mikic");
        try {
            if(!Files.exists(Paths.get(System.getProperty("user.home")  ,"Desktop","sofkom-storage"))){
            fs.init(System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "Desktop");
            fs.stateSave();
            }else
           fs= fs.stateLoad(System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"sofkom-storage"+File.separator+"state.txt");
        } catch (IOException e) {
            System.err.println("Storage exists at specified location.");
        }

        fs.setCurrentUser(new User("Milanka","cao"));
        System.out.println(fs.getCurrentUser().getUserName());
        fs = fs.stateLoad(System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"sofkom-storage"+File.separator+"state.txt");
        System.out.println(fs.getCurrentUser().getUserName());
        fs.toString();




            fs.store(fs.getPathToStorage().toString(),System.getProperty("user.home")+ File.separator+"Documents"+File.separator+"file.txt");
            fs.store(fs.getPathToStorage().toString(),System.getProperty("user.home")+ File.separator+"Documents"+File.separator+"file.txt",System.getProperty("user.home")+ File.separator+"Documents"+ File.separator+"file2.txt",System.getProperty("user.home")+ File.separator+"Documents"+File.separator+"file3.txt");
            fs.retrieve(fs.getPathToStorage().toString()+File.separator+"file.txt");
            fs.delete(fs.getPathToStorage().toString()+File.separator+"file.txt");

    }

    @Override
    public String toString() {
        return super.toString();
    }
}

