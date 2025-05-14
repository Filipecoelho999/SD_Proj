package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.rabbitmq.Consumer;
import edu.ufp.inf.sd.rmi.drive.server.AuthRI;
import edu.ufp.inf.sd.rmi.drive.server.FileManagerRI;
import edu.ufp.inf.sd.rmi.drive.server.ObserverRI;
import edu.ufp.inf.sd.rmi.drive.session.Session;
import edu.ufp.inf.sd.rmi.drive.session.SessionFactory;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DriveClient {

    private static AuthRI auth;
    private static FileManagerRI fileManager;
    private static ObserverRI observer;
    private static String username;
    private static String currentFolder = "";
    private static List<String> pastasPartilhadas = new ArrayList<>();
    private static boolean inShared = false;
    private static String ownerUsername = "";

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("=== Distributed Drive CLI ===");
            auth = (AuthRI) Naming.lookup("rmi://localhost:1099/auth");

            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.println("\n1 - Registar\n2 - Login\n0 - Sair");
                System.out.print("Escolha uma opcao: ");
                String opcao = scanner.nextLine();

                switch (opcao) {
                    case "1":
                        System.out.print("Username: ");
                        username = scanner.nextLine();
                        System.out.print("Password: ");
                        String passwordReg = scanner.nextLine();
                        if (auth.register(username, passwordReg)) {
                            System.out.println("Registo efetuado!");
                        } else {
                            System.out.println("Falha no registo!");
                        }
                        break;
                    case "2":
                        System.out.print("Username: ");
                        username = scanner.nextLine();
                        System.out.print("Password: ");
                        String passwordLogin = scanner.nextLine();

                        observer = new DriveObserver(username);
                        fileManager = auth.login(username, passwordLogin, observer);

                        if (fileManager != null) {
                            fileManager.setMyObserver(observer);
                            fileManager.getSubject().attachObserver(observer); // <--- ESSENCIAL
                            System.out.println("Login com sucesso! Bem-vindo, " + username);
                            Session sessao = SessionFactory.getSession(username);
                            if (sessao != null) {
                                System.out.println("Sessão iniciada às: " + sessao.getLoginTimestamp());
                            }
                            loggedIn = true;
                            Consumer.currentUser = username;
                            new Thread(() -> Consumer.start()).start();
                        } else {
                            System.out.println("Login falhou!");
                        }
                        break;
                    case "0":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                        break;
                }
            }

            while (true) {
                System.out.print(username + ":/" + (currentFolder.isEmpty() ? "" : currentFolder + "/") + " > ");
                String commandLine = scanner.nextLine();
                String[] parts = commandLine.trim().split("\\s+");
                String command = parts[0];

                switch (command) {
                    case "mkdir":
                    case "upload":
                    case "rename":
                        tratarComandosDeEscrita(command, parts, scanner);
                        break;
                    case "ls":
                        tratarLs(parts);
                        break;
                    case "cd":
                        tratarCd(parts);
                        break;
                    case "open":
                        tratarOpen(parts);
                        break;
                    case "share":
                        if (parts.length < 3) {
                            System.out.println("Uso: share <pasta> <destinatario> [permissao]");
                        } else {
                            String path = buildPath(parts[1]);
                            String destinatario = parts[2];
                            String permissao = (parts.length >= 4) ? parts[3].toLowerCase() : "read";
                            if (!permissao.equals("read") && !permissao.equals("write")) {
                                System.out.println("Permissão inválida. Usa 'read' ou 'write'.");
                                break;
                            }
                            if (fileManager.shareFolder(path, destinatario, permissao)) {
                                System.out.println("Pasta partilhada com permissao: " + permissao);
                            } else {
                                System.out.println("Erro a partilhar.");
                            }
                        }
                        break;
                    case "unshare":
                        if (parts.length < 3) {
                            System.out.println("Uso: unshare <pasta> <utilizador>");
                        } else {
                            String path = buildPath(parts[1]);
                            if (fileManager.unshareFolder(path, parts[2])) {
                                System.out.println("Partilha removida!");
                            } else {
                                System.out.println("Erro ao remover partilha.");
                            }
                        }
                        break;
                    case "delete":
                        if (parts.length < 2) {
                            System.out.println("Uso: delete <caminho>");
                        } else {
                            if (fileManager.delete(buildPath(parts[1]))) {
                                System.out.println("Deletado!");
                            } else {
                                System.out.println("Erro ao deletar.");
                            }
                        }
                        break;
                    case "move":
                        if (parts.length < 3) {
                            System.out.println("Uso: move <origem> <destino>");
                        } else {
                            if (fileManager.move(buildPath(parts[1]), buildPath(parts[2]))) {
                                System.out.println("Movido!");
                            } else {
                                System.out.println("Erro ao mover.");
                            }
                        }
                        break;
                    case "sharedwithme":
                        pastasPartilhadas = fileManager.getSharedWithMe(username);
                        if (pastasPartilhadas.isEmpty()) {
                            System.out.println("Nenhuma pasta partilhada contigo.");
                        } else {
                            System.out.println("Pastas partilhadas contigo:");
                            for (String s : pastasPartilhadas) {
                                System.out.println(" - " + s);
                            }
                        }
                        break;
                    case "entershared":
                        if (parts.length < 2) {
                            System.out.println("Uso: entershared <utilizador>");
                        } else {
                            ownerUsername = parts[1];
                            fileManager.enterShared(ownerUsername);
                            currentFolder = "";
                            pastasPartilhadas = fileManager.getSharedWithMe(username);
                            inShared = true;
                            System.out.println("Entrou na partilha de " + ownerUsername);
                        }
                        break;
                    case "logout":
                        System.out.println("Logout efetuado.");
                        System.exit(0);
                        break;
                    case "ajuda":
                        System.out.println("""
                                 Comandos disponíveis:
                                 - mkdir <nome_pasta>        ➔ Criar nova pasta (apenas no seu drive)
                                 - upload <ficheiro>         ➔ Fazer upload de um ficheiro
                                 - rename <antigo> <novo>    ➔ Renomear ficheiro ou pasta
                                 - ls [pasta]                ➔ Listar conteúdo
                                 - cd <nova_pasta> / cd ..   ➔ Mudar de pasta ou voltar atras
                                 - open <ficheiro>           ➔ Abrir e ver conteúdo de ficheiro
                                 - share <pasta> <destinatario> [permissao] ➔ Partilhar pasta
                                 - sharedwithme              ➔ Listar pastas partilhadas contigo
                                 - entershared <utilizador>  ➔ Aceder às partilhas recebidas
                                 - unshare <pasta> <utilizador> ➔ Remover partilha
                                 - delete <caminho>          ➔ Deletar ficheiro ou pasta
                                 - move <origem> <destino>   ➔ Mover ficheiro ou pasta
                                 - logout                    ➔ Terminar sessão
                                 - ajuda                     ➔ Mostrar esta ajuda
                                """);
                        break;
                    default:
                        System.out.println("Comando nao reconhecido. Escreva 'ajuda' para ver comandos.");
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void tratarComandosDeEscrita(String command, String[] parts, Scanner scanner) throws RemoteException {
        if (parts.length < 2) {
            System.out.println("Uso: " + command + " <parametros>");
            return;
        }

        String path = buildPath(parts[1]);

        switch (command) {
            case "mkdir":
                if (fileManager.mkdir(path)) {
                    System.out.println("Pasta criada!");
                } else {
                    System.out.println("Erro a criar pasta.");
                }
                break;
            case "upload":
                System.out.print("Conteudo do ficheiro: ");
                String content = scanner.nextLine();
                if (fileManager.upload(buildPath(""), parts[1], content)) {
                    System.out.println("Ficheiro criado!");
                } else {
                    System.out.println("Erro no upload.");
                }
                break;
            case "rename":
                if (parts.length < 3) {
                    System.out.println("Uso: rename <antigo> <novo>");
                } else {
                    if (fileManager.rename(buildPath(""), parts[1], parts[2])) {
                        System.out.println("Renomeado!");
                    } else {
                        System.out.println("Erro ao renomear.");
                    }
                }
                break;
        }
    }

    private static void tratarLs(String[] parts) throws RemoteException {
        String path = (parts.length == 2) ? buildPath(parts[1]) : buildPath("");
        List<String> items = fileManager.list(path);
        if (items.isEmpty()) {
            System.out.println("(vazio)");
        } else {
            for (String item : items) {
                if (!inShared || pastasPartilhadas.contains(item) || !currentFolder.isEmpty()) {
                    System.out.println(item);
                }
            }
        }
    }

    private static void tratarCd(String[] parts) throws RemoteException {
        if (parts.length < 2) {
            System.out.println("Uso: cd <nova_pasta>");
            return;
        }

        if (parts[1].equals("..")) {
            if (!currentFolder.isEmpty()) {
                Path path = Paths.get(currentFolder).getParent();
                currentFolder = (path == null) ? "" : path.toString().replace("\\", "/");
            }
        } else {
            String candidatePath = buildPath(parts[1]);
            List<String> folders = fileManager.list(currentFolder.isEmpty() ? "" : currentFolder);

            if (folders.contains(parts[1])) {
                if (inShared && currentFolder.isEmpty() && !pastasPartilhadas.contains(parts[1])) {
                    System.out.println("Nao tens acesso a esta pasta.");
                    return;
                }
                if (currentFolder.isEmpty()) {
                    currentFolder = parts[1];
                } else {
                    currentFolder = currentFolder + "/" + parts[1];
                }
                System.out.println("Mudaste para: " + currentFolder);
            } else {
                System.out.println("Pasta '" + parts[1] + "' nao encontrada.");
            }
        }
    }

    private static void tratarOpen(String[] parts) throws RemoteException {
        if (parts.length < 2) {
            System.out.println("Uso: open <ficheiro>");
            return;
        }
        String path = buildPath("");
        String content = fileManager.readFile(path, parts[1]);
        System.out.println("Conteudo de " + parts[1] + ":");
        System.out.println(content);
    }

    private static String buildPath(String input) {
        if (currentFolder.isEmpty()) {
            return input;
        } else if (input.isEmpty()) {
            return currentFolder;
        } else {
            return currentFolder + "/" + input;
        }
    }
}
