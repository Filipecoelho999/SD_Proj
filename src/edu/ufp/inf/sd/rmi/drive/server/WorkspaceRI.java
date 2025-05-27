package edu.ufp.inf.sd.rmi.drive.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WorkspaceRI extends Remote {
    String getUsername() throws RemoteException;

    boolean createFolder(String path, String folderName, boolean isShared) throws RemoteException;
    boolean createFile(String path, String fileName, String content, boolean isShared) throws RemoteException;
    boolean delete(String path, String name, boolean isFolder, boolean isShared) throws RemoteException;
    boolean rename(String path, String oldName, String newName, boolean isFolder, boolean isShared) throws RemoteException;
    boolean move(String sourcePath, String name, String destPath, boolean isFolder, boolean isShared) throws RemoteException;
    List<String> list(String path, boolean isShared) throws RemoteException;
    SubjectRI getSubject() throws RemoteException;
    boolean share(String path, String targetUser, boolean isFolder) throws RemoteException;
    List<SharedReference> getSharedWithMe(String username) throws RemoteException;
    boolean unshare(String path, String targetUser) throws RemoteException;

    boolean entershared(String owner) throws RemoteException;
}