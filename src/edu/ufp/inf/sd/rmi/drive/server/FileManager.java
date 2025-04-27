package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.rabbitmq.Publisher;
import java.io.IOException;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FileManager extends UnicastRemoteObject implements FileManagerRI {

    private Path basePath;
    private final SubjectRI subject;
    private final AuthRI auth;
    private final String ownerUsername;
    private ObserverRI myObserver;

    public FileManager(String username, AuthRI auth) throws RemoteException {
        super();
        this.ownerUsername = username;
        this.auth = auth;
        this.subject = new SubjectImpl();
        this.basePath = Paths.get(System.getProperty("user.dir"), "server_files", username);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RemoteException("Erro a criar pasta base.", e);
        }
    }

    @Override
    public boolean mkdir(String folderName) throws RemoteException {
        try {
            Path folderPath = basePath.resolve(folderName);
            Files.createDirectories(folderPath);
            subject.notifyObservers("Pasta criada: " + folderName);
            Publisher.publish("Pasta criada: " + folderName); // RabbitMQ publish
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean upload(String folderName, String fileName, String content) throws RemoteException {
        try {
            Path folderPath = basePath.resolve(folderName);
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName);
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            subject.notifyObservers("Ficheiro criado: " + folderName + "/" + fileName);
            Publisher.publish("Ficheiro enviado: " + folderName + "/" + fileName); // RabbitMQ publish
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> list(String folderName) throws RemoteException {
        List<String> fileList = new ArrayList<>();
        try {
            Path folderPath = basePath.resolve(folderName);
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                    for (Path entry : stream) {
                        fileList.add(entry.getFileName().toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao listar ficheiros.");
        }
        return fileList;
    }

    @Override
    public String readFile(String path, String filename) throws RemoteException {
        try {
            Path filePath = basePath.resolve(path).resolve(filename);
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                return Files.readString(filePath);
            } else {
                return "Ficheiro nao encontrado.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao ler o ficheiro.");
        }
    }

    @Override
    public boolean rename(String folderName, String oldName, String newName) throws RemoteException {
        try {
            Path oldPath = basePath.resolve(folderName).resolve(oldName);
            Path newPath = basePath.resolve(folderName).resolve(newName);
            Files.move(oldPath, newPath);
            subject.notifyObservers("Renomeado: " + oldName + " para " + newName);
            Publisher.publish("Renomeado: " + oldName + " para " + newName); // RabbitMQ publish
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean delete(String path) throws RemoteException {
        try {
            Path targetPath = basePath.resolve(path);
            if (Files.exists(targetPath)) {
                if (Files.isDirectory(targetPath)) {
                    Files.walk(targetPath)
                            .sorted((a, b) -> b.compareTo(a)) // Deletar subpastas primeiro
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    Files.delete(targetPath);
                }
                subject.notifyObservers("Deleted: " + path);
                Publisher.publish("Deleted: " + path);
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean move(String sourcePath, String destPath) throws RemoteException {
        try {
            Path source = basePath.resolve(sourcePath);
            Path destination = basePath.resolve(destPath).resolve(source.getFileName());
            Files.createDirectories(basePath.resolve(destPath)); // Garante que a pasta destino existe
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            subject.notifyObservers("Movido: " + sourcePath + " para " + destPath);
            Publisher.publish("Movido: " + sourcePath + " para " + destPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean shareFolder(String folderName, String targetUser) throws RemoteException {
        boolean partilhaFeita = auth.adicionarPartilha(targetUser, folderName);
        if (partilhaFeita) {
            FileManagerRI targetDrive = auth.getDrive(targetUser);
            if (targetDrive != null) {
                SubjectRI subjectTarget = targetDrive.getSubject();
                for (ObserverRI observer : subjectTarget.getObservers().values()) {
                    observer.update("Recebeste uma nova partilha: " + folderName + " de " + ownerUsername);
                }
            }
            Publisher.publish("Partilha criada: " + folderName + " para " + targetUser); // RabbitMQ publish
            return true;
        }
        return false;
    }

    @Override
    public boolean unshareFolder(String folderName, String targetUser) throws RemoteException {
        boolean removida = auth.removerPartilha(targetUser, folderName);
        if (removida) {
            FileManagerRI targetDrive = auth.getDrive(targetUser);
            if (targetDrive != null) {
                SubjectRI subjectTarget = targetDrive.getSubject();
                for (ObserverRI observer : subjectTarget.getObservers().values()) {
                    observer.update("Partilha removida: " + folderName + " de " + ownerUsername);
                }
            }
            Publisher.publish("Partilha removida: " + folderName + " de " + ownerUsername); // RabbitMQ publish
            return true;
        }
        return false;
    }

    @Override
    public List<String> getSharedWithMe(String myUsername) throws RemoteException {
        return auth.getPartilhasRecebidas(myUsername);
    }

    @Override
    public boolean enterShared(String ownerUsername) throws RemoteException {
        this.basePath = Paths.get(System.getProperty("user.dir"), "server_files", ownerUsername);

        FileManagerRI donoDrive = auth.getDrive(ownerUsername);
        if (donoDrive != null && myObserver != null) {
            donoDrive.getSubject().attachObserver(myObserver);
            System.out.println("Observer adicionado ao dono: " + ownerUsername);
        }
        return true;
    }

    @Override
    public SubjectRI getSubject() throws RemoteException {
        return subject;
    }

    public void setMyObserver(ObserverRI observer) {
        this.myObserver = observer;
    }

    private String getOwnerUsername() {
        return ownerUsername;
    }
}
