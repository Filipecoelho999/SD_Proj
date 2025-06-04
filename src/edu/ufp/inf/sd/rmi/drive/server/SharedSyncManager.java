package edu.ufp.inf.sd.rmi.drive.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

public class SharedSyncManager {

    private static final String SERVER_DATA = "server_data/shared/";

    public static void createSharedFolder(Path sourcePath, String receiverUsername, String ownerUsername, String folderName) throws IOException {
        Path receiverPath = Paths.get("server_files", receiverUsername, "shared_" + ownerUsername + "_" + folderName);
        Path serverPath = Paths.get(SERVER_DATA, ownerUsername + "_" + folderName);
        deleteDirectory(receiverPath);
        deleteDirectory(serverPath);
        copyDirectory(sourcePath, receiverPath);
        copyDirectory(sourcePath, serverPath);
    }

    public static void propagateChange(Path updatedPath, String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return; // ✅ prevenir sincronização prematura

        Path basePath = updatedPath;

        for (String receiverUsername : receivers) {
            Path receiverPath = Paths.get("server_files", receiverUsername, "shared_" + ownerUsername + "_" + relativeToSharedRoot);
            deleteDirectory(receiverPath);
            copyDirectory(basePath, receiverPath);
        }

        Path serverPath = Paths.get(SERVER_DATA, ownerUsername + "_" + relativeToSharedRoot);
        deleteDirectory(serverPath);
        copyDirectory(basePath, serverPath);
    }

    public static void propagateMove(Path oldPath, Path newPath, String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return; // ✅ evitar movimentos desnecessários

        for (String receiverUsername : receivers) {
            Path oldReceiverPath = Paths.get("server_files", receiverUsername, "shared_" + ownerUsername + "_" + relativeToSharedRoot);
            Path newReceiverPath = Paths.get("server_files", receiverUsername, "shared_" + ownerUsername + "_" + newPath.getFileName());
            deleteDirectory(newReceiverPath);
            move(oldReceiverPath, newReceiverPath);
        }

        Path oldServerPath = Paths.get(SERVER_DATA, ownerUsername + "_" + relativeToSharedRoot);
        Path newServerPath = Paths.get(SERVER_DATA, ownerUsername + "_" + newPath.getFileName());
        deleteDirectory(newServerPath);
        move(oldServerPath, newServerPath);
    }

    public static void syncSingleChange(Path sourcePath, Path receiverPath) throws IOException {
        deleteDirectory(receiverPath);
        copyDirectory(sourcePath, receiverPath);
    }

    public static void deleteFromAll(String relativeToSharedRoot, String ownerUsername, List<String> receivers) throws IOException {
        if (receivers == null || receivers.isEmpty()) return; // ✅ não há partilhas

        for (String receiverUsername : receivers) {
            Path receiverPath = Paths.get("server_files", receiverUsername, "shared_" + ownerUsername + "_" + relativeToSharedRoot);
            deleteDirectory(receiverPath);
        }

        Path serverPath = Paths.get(SERVER_DATA, ownerUsername + "_" + relativeToSharedRoot);
        deleteDirectory(serverPath);
    }

    public static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) return;

        deleteDirectory(target); // garantir destino limpo antes de copiar

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