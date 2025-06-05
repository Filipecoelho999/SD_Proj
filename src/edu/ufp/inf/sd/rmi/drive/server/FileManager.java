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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileManager extends UnicastRemoteObject implements FileManagerRI {

    public static final String MODO_PROPAGACAO = System.getProperty("modo", "rmiserver");

    private final Path clientLocalPath;
    private final Path serverLocalPath;
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

        this.clientLocalPath = Paths.get("Clients", username, username + "_local");
        this.serverLocalPath = Paths.get("Server", username + "_local");
        this.basePath = clientLocalPath;

        try {
            Files.createDirectories(clientLocalPath);
            Files.createDirectories(serverLocalPath);
        } catch (IOException e) {
            throw new RemoteException("Erro a criar estrutura de pastas para o utilizador.", e);
        }
    }

    private Path resolvePath(String folderName) {
        if (!ownerUsername.equals(donoReal)) {
            // folderName pode vir com "shared_joao_ola/boas" ou "ola/boas"
            String[] parts = folderName.split("/");

            // remover prefixo "shared_joao_" se estiver incluído
            String rawPartilha = parts[0];
            if (rawPartilha.startsWith("shared_" + donoReal + "_")) {
                rawPartilha = rawPartilha.replace("shared_" + donoReal + "_", "");
            }

            Path sharedBase = Paths.get("Clients", ownerUsername, "shared", "shared_" + donoReal + "_" + rawPartilha);

            if (parts.length > 1) {
                return sharedBase.resolve(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
            } else {
                return sharedBase;
            }
        }

        return clientLocalPath.resolve(folderName);
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

    private String getTopFolder(String path) {
        if (path == null || path.isEmpty()) return "";
        return path.contains("/") ? path.substring(0, path.indexOf("/")) : path;
    }

    private boolean temPermissaoEscrita(String path) throws RemoteException {
        if (ownerUsername.equals(donoReal)) return true;

        // path pode vir como "shared_joao_ola/boas/oiii"
        // precisamos só de extrair "ola"
        String pastaRaiz;
        if (path.contains("/")) {
            pastaRaiz = path.substring(0, path.indexOf("/"));
        } else {
            pastaRaiz = path;
        }

        // Corrigir nome se estiver em modo partilha
        if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
            pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
        }

        return ((AuthImpl) auth).temPermissaoEscrita(ownerUsername, pastaRaiz);
    }
    @Override
    public boolean mkdir(String folderName) throws RemoteException {
        if (!temPermissaoEscrita(folderName)) {
            notificarTodos("Sem permissao de escrita: " + folderName);
            return false;
        }

        try {
            // Caminho local (cliente)
            Path folderPath = resolvePath(folderName);

            // Caminho no servidor
            Path serverFolder;
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String internal = folderName.contains("/") ? folderName.substring(folderName.indexOf("/") + 1) : "";

                serverFolder = Paths.get("Server", donoReal + "_local", pastaRaiz);
                if (!internal.isEmpty()) {
                    serverFolder = serverFolder.resolve(internal);
                }
            } else {
                serverFolder = serverLocalPath.resolve(folderName);
            }

            // Criar localmente e no servidor do dono real
            Files.createDirectories(folderPath);
            Files.createDirectories(serverFolder);

            notificarTodos("Pasta criada: " + folderName);

            String relativePathStr = folderName.replace("\\", "/");

            // Propagação para receivers
            SharedSyncManager.propagateChange(
                    serverFolder,
                    relativePathStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativePathStr)
            );

            // Se não for o dono real, sincronizar joao_local (client/server)
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                Path updatedPath = Paths.get("Clients", ownerUsername, "shared", "shared_" + donoReal + "_" + pastaRaiz);
                if (folderName.contains("/")) {
                    updatedPath = updatedPath.resolve(folderName.substring(folderName.indexOf("/") + 1));
                }

                SharedSyncManager.syncToOwnerServer(updatedPath, relativePathStr, donoReal);
                SharedSyncManager.syncToOwnerClient(updatedPath, relativePathStr, donoReal);
            }

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

            // --- CLIENTE ---
            Path folderPath = resolvePath(folderName);
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName);
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // --- SERVIDOR ---
            Path serverFolder;
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String internal = folderName.contains("/") ? folderName.substring(folderName.indexOf("/") + 1) : "";
                serverFolder = Paths.get("Server", donoReal + "_local", pastaRaiz);
                if (!internal.isEmpty()) {
                    serverFolder = serverFolder.resolve(internal);
                }
            } else {
                serverFolder = serverLocalPath.resolve(folderName);
            }

            Files.createDirectories(serverFolder);
            Path serverFilePath = serverFolder.resolve(fileName);
            Files.writeString(serverFilePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            notificarTodos("Ficheiro criado: " + path);

            String relativePathStr = (folderName + "/" + fileName).replace("\\", "/");

            // --- Propagação para receivers da partilha ---
            SharedSyncManager.propagateChange(
                    serverFilePath,
                    relativePathStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativePathStr)
            );

            // --- Sincronizar para o dono real (client/server) ---
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String relativeInternalPath = folderName.contains("/") ? folderName.substring(folderName.indexOf("/") + 1) : "";

                // Caminho para o ficheiro criado no shared do receiver
                Path updatedPath = Paths.get("Clients", ownerUsername, "shared", "shared_" + donoReal + "_" + pastaRaiz);
                if (!relativeInternalPath.isEmpty()) {
                    updatedPath = updatedPath.resolve(relativeInternalPath);
                }
                updatedPath = updatedPath.resolve(fileName);

                // Apagar ficheiros antigos
                Path serverTarget = Paths.get("Server", donoReal + "_local", pastaRaiz);
                Path clientTarget = Paths.get("Clients", donoReal, donoReal + "_local", pastaRaiz);
                if (!relativeInternalPath.isEmpty()) {
                    serverTarget = serverTarget.resolve(relativeInternalPath);
                    clientTarget = clientTarget.resolve(relativeInternalPath);
                }
                serverTarget = serverTarget.resolve(fileName);
                clientTarget = clientTarget.resolve(fileName);

                SharedSyncManager.deleteDirectory(serverTarget);
                SharedSyncManager.deleteDirectory(clientTarget);

                // Sincronizar conteúdo atualizado
                SharedSyncManager.syncToOwnerServer(updatedPath, relativePathStr, donoReal);
                SharedSyncManager.syncToOwnerClient(updatedPath, relativePathStr, donoReal);
            }

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

            // --- Diretório local (cliente) ---
            Path folderPath = resolvePath(folderName);
            Path oldPath = folderPath.resolve(oldName);
            Path newPath = folderPath.resolve(newName);
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            // --- Diretório do servidor (dono real) ---
            Path serverFolder;
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String internalPath = folderName.contains("/") ?
                        folderName.substring(folderName.indexOf("/") + 1) : "";

                serverFolder = Paths.get("Server", donoReal + "_local", pastaRaiz);
                if (!internalPath.isEmpty()) {
                    serverFolder = serverFolder.resolve(internalPath);
                }
            } else {
                serverFolder = serverLocalPath.resolve(folderName);
            }

            Path serverOldPath = serverFolder.resolve(oldName);
            Path serverNewPath = serverFolder.resolve(newName);
            Files.createDirectories(serverFolder);
            Files.move(serverOldPath, serverNewPath, StandardCopyOption.REPLACE_EXISTING);

            notificarTodos("Renomeado: " + folderName + "/" + oldName + " para " + newName);

            String relativeOldStr = (folderName + "/" + oldName).replace("\\", "/");
            String relativeNewStr = (folderName + "/" + newName).replace("\\", "/");

            // --- Apagar nos receivers o antigo ---
            SharedSyncManager.deleteFromAll(
                    relativeOldStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativeOldStr)
            );

            // --- Propagar o novo ---
            SharedSyncManager.propagateChange(
                    serverNewPath,
                    relativeNewStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativeNewStr)
            );

            // --- Atualizar server/client do dono real ---
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(folderName);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String relativeInternalPath = folderName.contains("/") ?
                        folderName.substring(folderName.indexOf("/") + 1) : "";

                // DELETE antigo
                Path oldServerPath = Paths.get("Server", donoReal + "_local", pastaRaiz);
                Path oldClientPath = Paths.get("Clients", donoReal, donoReal + "_local", pastaRaiz);
                if (!relativeInternalPath.isEmpty()) {
                    oldServerPath = oldServerPath.resolve(relativeInternalPath);
                    oldClientPath = oldClientPath.resolve(relativeInternalPath);
                }
                oldServerPath = oldServerPath.resolve(oldName);
                oldClientPath = oldClientPath.resolve(oldName);

                SharedSyncManager.deleteDirectory(oldServerPath);
                SharedSyncManager.deleteDirectory(oldClientPath);

                // SYNC novo
                Path updatedPath = Paths.get("Clients", ownerUsername, "shared", "shared_" + donoReal + "_" + pastaRaiz);
                if (!relativeInternalPath.isEmpty()) {
                    updatedPath = updatedPath.resolve(relativeInternalPath);
                }
                updatedPath = updatedPath.resolve(newName);

                String relativePathToSync = pastaRaiz;
                if (!relativeInternalPath.isEmpty()) relativePathToSync += "/" + relativeInternalPath;
                relativePathToSync += "/" + newName;

                SharedSyncManager.syncToOwnerServer(updatedPath, relativePathToSync, donoReal);
                SharedSyncManager.syncToOwnerClient(updatedPath, relativePathToSync, donoReal);
            }

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
        if (!temPermissaoEscrita(path)) {
            notificarTodos("Sem permissao de escrita: " + path);
            return false;
        }

        if (!LockManager.getInstance().lock(path, ownerUsername)) {
            notificarTodos("Recurso em uso: " + path);
            return false;
        }

        try {
            Thread.sleep(3000);

            // --- LOCAL (cliente) ---
            Path localPath = resolvePath(path);

            if (Files.exists(localPath)) {
                Files.walk(localPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }

            // --- SERVIDOR (caminho ajustado) ---
            Path serverPath;
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(path);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String internal = path.contains("/") ? path.substring(path.indexOf("/") + 1) : "";

                serverPath = Paths.get("Server", donoReal + "_local", pastaRaiz);
                if (!internal.isEmpty()) {
                    serverPath = serverPath.resolve(internal);
                }
            } else {
                serverPath = serverLocalPath.resolve(path);
            }

            if (Files.exists(serverPath)) {
                Files.walk(serverPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }

            // --- Notificação geral ---
            notificarTodos("Deleted: " + path);
            String relativePathStr = path.replace("\\", "/");

            SharedSyncManager.deleteFromAll(
                    relativePathStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativePathStr)
            );

            // --- Se for receiver, apagar também do dono real (client/server) ---
            if (!donoReal.equals(ownerUsername)) {
                String pastaRaiz = getTopFolder(path);
                if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                    pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
                }

                String relativeInternalPath = path.contains("/") ? path.substring(path.indexOf("/") + 1) : "";

                Path serverPathToDelete = Paths.get("Server", donoReal + "_local", pastaRaiz);
                Path clientPathToDelete = Paths.get("Clients", donoReal, donoReal + "_local", pastaRaiz);

                if (!relativeInternalPath.isEmpty()) {
                    serverPathToDelete = serverPathToDelete.resolve(relativeInternalPath);
                    clientPathToDelete = clientPathToDelete.resolve(relativeInternalPath);
                }

                SharedSyncManager.deleteDirectory(serverPathToDelete);
                SharedSyncManager.deleteDirectory(clientPathToDelete);
            }

            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            LockManager.getInstance().unlock(path, ownerUsername);
        }
    }


    @Override
    public boolean move(String sourcePath, String destPath) throws RemoteException {
        if (!temPermissaoEscrita(sourcePath)) {
            notificarTodos("Sem permissao de escrita: " + sourcePath);
            return false;
        }

        if (!LockManager.getInstance().lock(sourcePath, ownerUsername)) {
            notificarTodos("Recurso em uso: " + sourcePath);
            return false;
        }

        try {
            Thread.sleep(3000);

            // --- MOVE NO CLIENTE ---
            Path sourceClient = resolvePath(sourcePath);
            Path destClient = resolvePath(destPath).resolve(sourceClient.getFileName());

            Files.createDirectories(destClient.getParent());
            Files.move(sourceClient, destClient, StandardCopyOption.REPLACE_EXISTING);

            // --- MOVE NO SERVIDOR ---
            Path sourceServer, destServer;
            String pastaRaiz = getTopFolder(sourcePath);
            if (pastaRaiz.startsWith("shared_" + donoReal + "_")) {
                pastaRaiz = pastaRaiz.replace("shared_" + donoReal + "_", "");
            }

            String internalOld = sourcePath.contains("/") ? sourcePath.substring(sourcePath.indexOf("/") + 1) : "";
            String internalNew = destPath.contains("/") ? destPath.substring(destPath.indexOf("/") + 1) : "";

            if (!donoReal.equals(ownerUsername)) {
                Path baseServer = Paths.get("Server", donoReal + "_local", pastaRaiz);
                sourceServer = !internalOld.isEmpty() ? baseServer.resolve(internalOld) : baseServer;
                destServer = !internalNew.isEmpty() ? baseServer.resolve(internalNew) : baseServer;
            } else {
                sourceServer = serverLocalPath.resolve(sourcePath);
                destServer = serverLocalPath.resolve(destPath);
            }

            // --- DEBUG PRINT ---
            System.out.println("[DEBUG MOVE] sourceServer: " + sourceServer);
            System.out.println("[DEBUG MOVE] destServer: " + destServer);

            // --- MOVE ---
            Files.createDirectories(destServer.getParent());
            Files.move(sourceServer, destServer, StandardCopyOption.REPLACE_EXISTING);

            notificarTodos("Movido: " + sourcePath + " para " + destPath);

            // Apagar nos receivers o antigo
            String relativeOldStr = sourcePath.replace("\\", "/");
            SharedSyncManager.deleteFromAll(
                    relativeOldStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativeOldStr)
            );

            // Propagar novo caminho
            String relativeNewStr = (destPath + "/" + sourceClient.getFileName().toString()).replace("\\", "/");

            SharedSyncManager.propagateChange(
                    destServer,
                    relativeNewStr,
                    donoReal,
                    ((AuthImpl) auth).getUsersWithAccessToFolder(donoReal, relativeNewStr)
            );

            // --- Atualizar client do dono real ---
            if (!donoReal.equals(ownerUsername)) {
                Path oldClientPath = Paths.get("Clients", donoReal, donoReal + "_local", pastaRaiz);
                if (!internalOld.isEmpty()) {
                    oldClientPath = oldClientPath.resolve(internalOld);
                }

                SharedSyncManager.deleteDirectory(oldClientPath);

                Path updatedPath = Paths.get("Clients", ownerUsername, "shared", "shared_" + donoReal + "_" + pastaRaiz);
                if (!internalNew.isEmpty()) updatedPath = updatedPath.resolve(internalNew);
                updatedPath = updatedPath.resolve(sourceClient.getFileName());

                SharedSyncManager.syncToOwnerServer(updatedPath, relativeNewStr, donoReal);
                SharedSyncManager.syncToOwnerClient(updatedPath, relativeNewStr, donoReal);
            }

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
        if (!ownerUsername.equals(donoReal)) {
            throw new RemoteException("Apenas o dono pode partilhar pastas.");
        }

        boolean partilhaFeita = ((AuthImpl) auth).adicionarPartilha(targetUser, folderName, permissao, ownerUsername);
        if (partilhaFeita) {
            //  Criar a cópia física no receiver
            try {
                Path source = Paths.get("Server", ownerUsername + "_local", folderName);
                SharedSyncManager.createSharedFolder(source, targetUser, ownerUsername, folderName);
            } catch (IOException e) {
                System.err.println("Erro ao criar cópia física para partilha: " + e.getMessage());
            }

            // Notificações
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
        if (!ownerUsername.equals(donoReal)) {
            throw new RemoteException("Apenas o dono pode remover partilhas.");
        }
        boolean removida = auth.removerPartilha(targetUser, folderName);
        if (removida) {
            // Remover fisicamente do receiver
            Path sharedFolderPath = Paths.get("Clients", targetUser, "shared", "shared_" + ownerUsername + "_" + folderName);
            try {
                if (Files.exists(sharedFolderPath)) {
                    Files.walk(sharedFolderPath)
                            .sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Notificações
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
        this.donoReal = ownerUsername;

        List<String> partilhasRecebidas = auth.getPartilhasRecebidas(this.ownerUsername);
        if (partilhasRecebidas == null || partilhasRecebidas.isEmpty()) {
            System.out.println("Não tens nenhuma partilha ativa com " + ownerUsername);
            return false;
        }

        // Define a basePath para a pasta de partilhas recebidas
        this.basePath = Paths.get("Clients", this.ownerUsername, "shared");

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
