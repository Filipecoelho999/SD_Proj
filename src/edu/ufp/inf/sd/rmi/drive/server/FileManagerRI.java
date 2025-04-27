package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileManagerRI extends Remote {
    SubjectRI getSubject() throws RemoteException;
    boolean mkdir(String path) throws RemoteException;
    boolean upload(String path, String filename, String content) throws RemoteException;
    boolean rename(String path, String oldName, String newName) throws RemoteException;
    List<String> list(String path) throws RemoteException;
    boolean shareFolder(String folderName, String targetUser) throws RemoteException;
    List<String> getSharedWithMe(String myUsername) throws RemoteException;
    boolean enterShared(String username) throws RemoteException;
    String readFile(String path, String filename) throws RemoteException;
    void setMyObserver(ObserverRI observer) throws RemoteException;
    boolean unshareFolder(String folderName, String targetUser) throws RemoteException;
    boolean delete(String path) throws RemoteException;
    boolean move(String sourcePath, String destPath) throws RemoteException;

}
