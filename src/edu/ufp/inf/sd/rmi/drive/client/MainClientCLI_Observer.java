package edu.ufp.inf.sd.rmi.drive.client;

import edu.ufp.inf.sd.rmi.drive.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class MainClientCLI_Observer {

    private static SubjectRI session;
    private static WorkspaceRI workspace;
    private static String currentPath = "/";
    private static String username = "";

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuthRI authRI = (AuthRI) registry.lookup("AuthService");

            System.out.println("=== Distributed Drive CLI (com Observer) ===");

            while (true) {
                System.out.println("\n1 - Registar\n2 - Login\n0 - Sair");
                System.out.print("Escolha uma opção: ");
                String opcao = sc.nextLine().trim();

                if (opcao.equals("1")) {
                    System.out.print("Escolha username: ");
                    username = sc.nextLine();
                    System.out.print("Escolha password: ");
                    String password = sc.nextLine();
                    boolean success = authRI.register(username, password);
                    System.out.println(success ? "Registo efetuado!" : "Utilizador já existe.");
                } else if (opcao.equals("2")) {
                    System.out.print("Username: ");
                    username = sc.nextLine();
                    System.out.print("Password: ");
                    String password = sc.nextLine();
                    session = authRI.login(username, password);
                    if (session != null) {
                        workspace = session.getWorkspace();
                        session.attachObserver(new ObserverImpl(username));
                        System.out.println("Login com sucesso! Bem-vindo, " + username);
                        break;
                    } else {
                        System.out.println("Credenciais inválidas.");
                    }
                } else if (opcao.equals("0")) {
                    System.out.println("A sair...");
                    return;
                } else {
                    System.out.println("Opção inválida.");
                }
            }

            showDriveCommands(sc);

        } catch (Exception e) {
            System.err.println("Erro no cliente CLI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showDriveCommands(Scanner sc) {
        System.out.println("\n=== Bem-vindo à tua Drive (com Observer) ===");

        while (true) {
            System.out.print(username + ":" + currentPath + " > ");
            String cmdLine = sc.nextLine().trim();
            String[] args = cmdLine.split(" ");

            try {
                switch (args[0]) {
                    case "logout":
                        System.out.println("🔒 Sessão terminada.");
                        return;

                    case "entershared": {
                        if (args.length < 2) {
                            System.out.println("Uso: entershared <utilizador>");
                            break;
                        }
                        boolean ok = workspace.entershared(args[1]);
                        if (ok) {
                            currentPath = "/shared_" + args[1] + "/";
                            System.out.println("🔗 Entraste na partilha de " + args[1]);
                        } else {
                            System.out.println("❌ Erro ao entrar na partilha de " + args[1]);
                        }
                        break;
                    }

                    case "cd": {
                        if (args.length < 2) {
                            System.out.println("Uso: cd <nova_pasta>");
                            break;
                        }
                        if (args[1].equals("..")) {
                            if (!currentPath.equals("/")) {
                                String[] split = currentPath.split("/");
                                StringBuilder newPath = new StringBuilder("/");
                                for (int i = 1; i < split.length - 1; i++) {
                                    newPath.append(split[i]).append("/");
                                }
                                currentPath = newPath.toString();
                            }
                        } else {
                            String path = normalize(currentPath);
                            boolean isShared = path.startsWith("shared_");
                            var contents = workspace.list(path, isShared);
                            if (!contents.contains(args[1])) {
                                System.out.println("❌ Pasta '" + args[1] + "' não encontrada.");
                                break;
                            }
                            if (!currentPath.endsWith("/")) currentPath += "/";
                            currentPath += args[1] + "/";
                        }
                        break;
                    }

                    case "ls": {
                        boolean isShared = currentPath.startsWith("/shared_");
                        String path = normalize(currentPath);
                        var contents = workspace.list(path, isShared);
                        if (contents.isEmpty()) System.out.println("(vazio)");
                        else contents.forEach(System.out::println);
                        break;
                    }

                    case "mkdir": {
                        if (args.length < 2) {
                            System.out.println("Uso: mkdir <nome_pasta>");
                            break;
                        }
                        String path = normalize(currentPath);
                        boolean isShared = currentPath.startsWith("/shared_");
                        boolean ok = workspace.createFolder(path, args[1], isShared);
                        System.out.println(ok ? "📁 Pasta criada." : "❌ Erro ao criar.");
                        break;
                    }

                    case "upload": {
                        if (args.length < 2) {
                            System.out.println("Uso: upload <nome_ficheiro>");
                            break;
                        }
                        String path = normalize(currentPath);
                        boolean isShared = currentPath.startsWith("/shared_");
                        boolean ok = workspace.createFile(path, args[1], "conteúdo de teste", isShared);
                        System.out.println(ok ? "📄 Ficheiro criado." : "❌ Erro ao criar ficheiro.");
                        break;
                    }

                    case "delete": {
                        if (args.length < 3) {
                            System.out.println("Uso: delete <nome> <file|folder>");
                            break;
                        }
                        String path = normalize(currentPath);
                        boolean isShared = currentPath.startsWith("/shared_");
                        boolean isFolder = args[2].equalsIgnoreCase("folder");
                        boolean ok = workspace.delete(path, args[1], isFolder, isShared);
                        System.out.println(ok ? "🗑️ Removido." : "❌ Erro ao remover.");
                        break;
                    }

                    case "rename": {
                        if (args.length < 4) {
                            System.out.println("Uso: rename <old> <new> <file|folder>");
                            break;
                        }
                        String path = normalize(currentPath);
                        boolean isShared = currentPath.startsWith("/shared_");
                        boolean isFolder = args[3].equalsIgnoreCase("folder");
                        boolean ok = workspace.rename(path, args[1], args[2], isFolder, isShared);
                        System.out.println(ok ? "✏️ Renomeado." : "❌ Erro ao renomear.");
                        break;
                    }

                    case "share": {
                        if (args.length < 4) {
                            System.out.println("Uso: share <destinatario> <nome> <file|folder>");
                            break;
                        }
                        String targetUser = args[1];
                        String path = normalize(currentPath);
                        String nome = args[2];
                        boolean isFolder = args[3].equalsIgnoreCase("folder");
                        String fullPath = normalize(path.isEmpty() ? nome : path + "/" + nome);
                        if (!fullPath.startsWith("/")) fullPath = "/" + fullPath;

                        boolean ok = workspace.share(fullPath, targetUser, isFolder);
                        System.out.println(ok ? "🔗 Partilhado com sucesso com " + targetUser : "❌ Erro ao partilhar.");
                        break;
                    }

                    case "unshare": {
                        if (args.length < 3) {
                            System.out.println("Uso: unshare <destinatario> <nome>");
                            break;
                        }
                        String targetUser = args[1];
                        String path = normalize(currentPath);
                        String nome = args[2];
                        String fullPath = path.isEmpty() ? nome : path + "/" + nome;
                        if (!fullPath.startsWith("/")) fullPath = "/" + fullPath;

                        boolean ok = workspace.unshare(fullPath, targetUser);
                        System.out.println(ok ? "❌ Partilha removida com " + targetUser : "⚠️ Nada para remover.");
                        break;
                    }

                    case "sharedwithme": {
                        var refs = workspace.getSharedWithMe(username);
                        if (refs.isEmpty()) System.out.println("Nenhum conteúdo partilhado contigo.");
                        else refs.forEach(ref -> System.out.println("📎 " + ref.path + (ref.isFolder ? " [pasta]" : " [ficheiro]") + " de " + ref.owner));
                        break;
                    }

                    case "ajuda":
                        System.out.println("""
                         📄 Comandos disponíveis:
                             - ls                             → listar conteúdo da pasta atual
                             - mkdir <nome>                  → criar nova pasta
                             - upload <nome>                 → criar novo ficheiro
                             - delete <nome> <file|folder>   → apagar ficheiro ou pasta
                             - rename <old> <new> <file|folder> → renomear ficheiro ou pasta
                             - cd <pasta> / cd ..            → navegar entre pastas
                             - share <user> <nome> <file|folder> → partilhar conteúdo com outro utilizador
                             - unshare <user> <nome>         → remover partilha
                             - sharedwithme                  → ver ficheiros/pastas partilhados contigo
                             - logout                        → terminar sessão """);
                        break;

                    default:
                        System.out.println("❌ Comando inválido. Usa 'ajuda' para ver comandos.");
                }
            } catch (Exception e) {
                System.out.println("❗ Erro: " + e.getMessage());
            }
        }
    }

    private static String normalize(String path) {
        if (path.equals("/")) return "";
        String cleaned = path.replaceFirst("^/", "");
        return cleaned.replaceAll("/+$", "");
    }
}
