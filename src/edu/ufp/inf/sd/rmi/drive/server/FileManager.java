
package edu.ufp.inf.sd.rmi.drive.server;

import edu.ufp.inf.sd.rmi.drive.rabbitmq.Publisher;
import edu.ufp.inf.sd.rmi.drive.session.Session;
import edu.ufp.inf.sd.rmi.drive.session.SessionFactory;
import edu.ufp.inf.sd.rmi.drive.util.LockManager;

import java.io.IOException;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FileManager extends UnicastRemoteObject implements FileManagerRI {

    public static final String MODO_PROPAGACAO = System.getProperty("modo", "rmiserver");

    private Path basePath;
    private final SubjectRI subject;
    private final AuthRI auth;
    private final String ownerUsername;
    private String donoReal;
    private SubjectRI subjectDonoReal;
    private ObserverRI myObserver;

    public FileManager(String username, AuthRI auth) throws RemoteException {
        super();
        this.ownerUsername = username;
        this.donoReal = username;
        this.auth = auth;
        Session sessao = SessionFactory.getSession(username);
        this.subject = (sessao != null) ? sessao.getSubject() : new SubjectImpl();
        this.subjectDonoReal = null;
        this.basePath = Paths.get(System.getProperty("user.dir"), "server_files", username);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RemoteException("Erro a criar pasta base.", e);
        }
    }

    private void notificarTodos(String mensagem) {
        try {
            if (MODO_PROPAGACAO.equalsIgnoreCase("rabbitmq")) {
                Publisher.publish(donoReal, mensagem);
                List<String> sharedUsers = ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, getTopFolder(mensagem));
                for (String sharedUser : sharedUsers) {
                    if (!sharedUser.equals(donoReal)) {
                        Publisher.publish(sharedUser, mensagem);
                    }
                }
            }

            if (MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
                if (donoReal.equals(ownerUsername)) {
                    subject.notifyObservers("[rmi][" + ownerUsername + "] " + mensagem);
                } else if (subjectDonoReal != null) {
                    subjectDonoReal.notifyObservers("[rmi][" + ownerUsername + "] " + mensagem);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getTopFolder(String mensagem) {
        String[] tokens = mensagem.split(":");
        if (tokens.length >= 2) {
            String path = tokens[1].trim();
            if (path.contains("/")) {
                return path.split("/")[0];
            } else {
                return path;
            }
        }
        return "";
    }

    private boolean temPermissaoEscrita(String path) throws RemoteException {
        String pasta = path.contains("/") ? path.substring(0, path.indexOf("/")) : path;
        if (ownerUsername.equals(donoReal)) return true;
        List<String> partilhas = auth.getPartilhasRecebidas(ownerUsername);
        return partilhas.contains(pasta);
    }

    @Override
    public boolean mkdir(String folderName) throws RemoteException {
        if (!temPermissaoEscrita(folderName)) {
            notificarTodos("Sem permissao de escrita: " + folderName);
            return false;
        }
        try {
            Path folderPath = basePath.resolve(folderName);
            Files.createDirectories(folderPath);
            notificarTodos("Pasta criada: " + folderName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean upload(String folderName, String fileName, String content) throws RemoteException {
        String path = folderName + "/" + fileName;
        if (!temPermissaoEscrita(folderName)) {
            notificarTodos("Sem permissao de escrita: " + folderName);
            return false;
        }
        if (!LockManager.getInstance().lock(path, ownerUsername)) {
            notificarTodos("Recurso em uso: " + path);
            return false;
        }
        try {
            Thread.sleep(3000);
            Path folderPath = basePath.resolve(folderName);
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName);
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            notificarTodos("Ficheiro criado: " + path);
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.getInstance().unlock(path, ownerUsername);
        }
    }

    @Override
    public boolean rename(String folderName, String oldName, String newName) throws RemoteException {
        String path = folderName + "/" + oldName;
        if (!temPermissaoEscrita(folderName)) {
            notificarTodos("Sem permissao de escrita: " + folderName);
            return false;
        }
        if (!LockManager.getInstance().lock(path, ownerUsername)) {
            notificarTodos("Recurso em uso: " + path);
            return false;
        }
        try {
            Thread.sleep(3000);
            Path oldPath = basePath.resolve(folderName).resolve(oldName);
            Path newPath = basePath.resolve(folderName).resolve(newName);
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            notificarTodos("Renomeado: " + folderName + "/" + oldName + " para " + newName);
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.getInstance().unlock(path, ownerUsername);
        }
    }

    @Override
    public boolean delete(String path) throws RemoteException {
        String pasta = path.contains("/") ? path.substring(0, path.indexOf("/")) : path;
        if (!temPermissaoEscrita(pasta)) {
            notificarTodos("Sem permissao de escrita: " + pasta);
            return false;
        }
        if (!LockManager.getInstance().lock(path, ownerUsername)) {
            notificarTodos("Recurso em uso: " + path);
            return false;
        }
        try {
            Thread.sleep(3000);
            Path targetPath = basePath.resolve(path);
            if (Files.exists(targetPath)) {
                if (Files.isDirectory(targetPath)) {
                    Files.walk(targetPath).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Files.delete(targetPath);
                }
                notificarTodos("Deletado: " + path);
                return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.getInstance().unlock(path, ownerUsername);
        }
    }

    @Override
    public boolean move(String sourcePath, String destPath) throws RemoteException {
        String pasta = sourcePath.contains("/") ? sourcePath.substring(0, sourcePath.indexOf("/")) : sourcePath;
        if (!temPermissaoEscrita(pasta)) {
            notificarTodos("Sem permissao de escrita: " + pasta);
            return false;
        }
        if (!LockManager.getInstance().lock(sourcePath, ownerUsername)) {
            notificarTodos("Recurso em uso: " + sourcePath);
            return false;
        }
        try {
            Thread.sleep(3000);
            Path source = basePath.resolve(sourcePath);
            Path destination = basePath.resolve(destPath).resolve(source.getFileName());
            Files.createDirectories(destination.getParent());
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            notificarTodos("Movido: " + sourcePath + " para " + destPath);
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.getInstance().unlock(sourcePath, ownerUsername);
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
    public boolean shareFolder(String folderName, String targetUser) throws RemoteException {
        return shareFolder(folderName, targetUser, "read");
    }

    public boolean shareFolder(String folderName, String targetUser, String permissao) throws RemoteException {
        boolean partilhaFeita = ((AuthImpl) auth).adicionarPartilha(targetUser, folderName, permissao);
        if (partilhaFeita) {
            FileManagerRI targetDrive = auth.getDrive(targetUser);
            if (targetDrive != null && MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
                SubjectRI subjectTarget = targetDrive.getSubject();
                for (ObserverRI observer : subjectTarget.getObservers().values()) {
                    observer.update("[rmi][" + targetUser + "] Recebeste uma nova partilha: " + folderName + " de " + ownerUsername + " [" + permissao + "]");
                }
            }

            if (MODO_PROPAGACAO.equalsIgnoreCase("rabbitmq")) {
                Publisher.publish(targetUser, "Partilha criada: " + folderName + " de " + ownerUsername + " [" + permissao + "]");
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean unshareFolder(String folderName, String targetUser) throws RemoteException {
        boolean removida = auth.removerPartilha(targetUser, folderName);
        if (removida) {
            FileManagerRI targetDrive = auth.getDrive(targetUser);
            if (targetDrive != null && MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
                SubjectRI subjectTarget = targetDrive.getSubject();
                for (ObserverRI observer : subjectTarget.getObservers().values()) {
                    observer.update("[rmi][" + targetUser + "] Partilha removida: " + folderName + " de " + ownerUsername);
                }
            }

            if (MODO_PROPAGACAO.equalsIgnoreCase("rabbitmq")) {
                Publisher.publish(targetUser, "Partilha removida: " + folderName + " de " + ownerUsername);
                Publisher.publish(ownerUsername, "Removeste a partilha de " + folderName + " com " + targetUser);
            }

            notificarTodos("Partilha removida: " + folderName + " de " + targetUser);
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
        this.donoReal = ownerUsername;

        List<String> partilhasRecebidas = auth.getPartilhasRecebidas(this.ownerUsername);
        if (partilhasRecebidas == null || partilhasRecebidas.isEmpty()) {
            System.out.println("Não tens nenhuma partilha ativa com " + ownerUsername);
            return false;
        }

        if (MODO_PROPAGACAO.equalsIgnoreCase("rmiserver")) {
            FileManagerRI donoDrive = auth.getDrive(ownerUsername);
            if (donoDrive != null && myObserver != null) {
                this.subjectDonoReal = donoDrive.getSubject();
                subjectDonoReal.attachObserver(myObserver);
                System.out.println("Observer adicionado ao dono: " + ownerUsername);
            }
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

    public static void setModoPropagacao(String modo) {
        System.setProperty("modo", modo);
    }
}
