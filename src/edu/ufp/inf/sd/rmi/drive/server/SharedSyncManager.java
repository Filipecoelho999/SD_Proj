package edu.ufp.inf.sd.rmi.drive.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

public class SharedSyncManager {

    public static void createSharedFolder(Path sourcePath, String receiverUsername, String ownerUsername, String folderName) throws IOException {
        Path receiverSharedPath = Paths.get("Clients", receiverUsername, "shared", "shared_" + ownerUsername + "_" + folderName);
        deleteDirectory(receiverSharedPath);
        copyDirectory(sourcePath, receiverSharedPath);
    }
    public static void propagateChange(Path updatedPath, String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return;

        for (String receiverUsername : receivers) {
            Path sharedBase = Paths.get("Clients", receiverUsername, "shared", "shared_" + ownerUsername + "_" + getTopFolder(relativeToSharedRoot));
            Path receiverTarget = sharedBase.resolve(removeTopFolder(relativeToSharedRoot));

            // Cria só o que for necessário
            Files.createDirectories(receiverTarget.getParent());
            deleteDirectory(receiverTarget); // apenas o ficheiro/pasta a substituir
            copyDirectory(updatedPath, receiverTarget); // substitui só essa parte
        }
    }

    public static void propagateMove(Path oldPath, Path newPath, String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return;

        for (String receiverUsername : receivers) {
            Path oldReceiverPath = Paths.get("Clients", receiverUsername, "shared", "shared_" + ownerUsername + "_" + relativeToSharedRoot);
            Path newReceiverPath = Paths.get("Clients", receiverUsername, "shared", "shared_" + ownerUsername + "_" + newPath.getFileName());
            deleteDirectory(newReceiverPath);
            move(oldReceiverPath, newReceiverPath);
        }
    }

    public static void syncSingleChange(Path sourcePath, Path receiverPath) throws IOException {
        deleteDirectory(receiverPath);
        copyDirectory(sourcePath, receiverPath);
    }
    public static void syncToOwnerServer(Path updatedPath, String relativeToSharedRoot, String ownerUsername) throws IOException {
        // Extrair só o nome da partilha (sem o prefixo "shared_<dono>_")
        String folderName = getTopFolder(relativeToSharedRoot);
        String relativeInternalPath = removeTopFolder(relativeToSharedRoot);

        // Corrigir se o folderName tiver o prefixo (shared_joao_ola → ola)
        if (folderName.startsWith("shared_" + ownerUsername + "_")) {
            folderName = folderName.replace("shared_" + ownerUsername + "_", "");
        }

        // Construir o caminho correto no servidor
        Path serverTarget = Paths.get("Server", ownerUsername + "_local", folderName);
        if (!relativeInternalPath.isEmpty()) {
            serverTarget = serverTarget.resolve(relativeInternalPath);
        }

        System.out.println("[DEBUG CORRIGIDO] Origem: " + updatedPath);
        System.out.println("[DEBUG CORRIGIDO] Destino: " + serverTarget);

        // Copiar o conteúdo
        copyDirectory(updatedPath, serverTarget);
    }
    public static void syncToOwnerClient(Path updatedPath, String relativeToSharedRoot, String ownerUsername) throws IOException {
        // Ex: shared_joao_ola/boas/oi → ola/boas/oi
        String folderName = getTopFolder(relativeToSharedRoot);
        String relativeInternalPath = removeTopFolder(relativeToSharedRoot);

        if (folderName.startsWith("shared_" + ownerUsername + "_")) {
            folderName = folderName.replace("shared_" + ownerUsername + "_", "");
        }

        Path clientTarget = Paths.get("Clients", ownerUsername, ownerUsername + "_local", folderName);
        if (!relativeInternalPath.isEmpty()) {
            clientTarget = clientTarget.resolve(relativeInternalPath);
        }

        System.out.println("[DEBUG CLIENT] Destino: " + clientTarget);
        copyDirectory(updatedPath, clientTarget);
    }


    public static void deleteFromAll(String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return;

        for (String receiverUsername : receivers) {
            Path sharedRoot = Paths.get("Clients", receiverUsername, "shared", "shared_" + ownerUsername + "_" + getTopFolder(relativeToSharedRoot));
            Path toDelete = sharedRoot.resolve(removeTopFolder(relativeToSharedRoot)); // apenas parte interna
            deleteDirectory(toDelete);
        }
    }
    public static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) return;

        deleteDirectory(target);

        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath).toString());
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void move(Path source, Path target) throws IOException {
        if (Files.exists(source)) {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String getTopFolder(String path) {
        int index = path.indexOf("/");
        return index == -1 ? path : path.substring(0, index);
    }

    public static String removeTopFolder(String path) {
        int index = path.indexOf("/");
        return index == -1 ? "" : path.substring(index + 1);
    }
}
