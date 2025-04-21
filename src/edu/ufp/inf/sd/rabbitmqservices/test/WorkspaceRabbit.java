package edu.ufp.inf.sd.rabbitmqservices.test;

import edu.ufp.inf.sd.rmi.drive.model.*;

import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;

public class WorkspaceRabbit extends Workspace {

    public WorkspaceRabbit(String username) {
        super(username, null);
    }

    @Override
    public boolean createFolder(String path, String folderName, boolean isShared) {
        boolean result = super.createFolder(path, folderName, isShared);
        if (result) RabbitUtils.publish("[RabbitMQ] " + getUsername() + " criou a pasta: " + path + "/" + folderName);
        return result;
    }

    @Override
    public boolean createFile(String path, String fileName, String content, boolean isShared) {
        boolean result = super.createFile(path, fileName, content, isShared);
        if (result) RabbitUtils.publish("[RabbitMQ] " + getUsername() + " criou o ficheiro: " + path + "/" + fileName);
        return result;
    }

    @Override
    public boolean rename(String path, String oldName, String newName, boolean isFolder, boolean isShared) {
        boolean result = super.rename(path, oldName, newName, isFolder, isShared);
        if (result) RabbitUtils.publish("[RabbitMQ] " + getUsername() + " renomeou o " + (isFolder ? "pasta" : "ficheiro") + ": " + oldName + " para " + newName);
        return result;
    }

    @Override
    public boolean move(String sourcePath, String name, String destPath, boolean isFolder, boolean isShared) {
        boolean result = super.move(sourcePath, name, destPath, isFolder, isShared);
        if (result) RabbitUtils.publish("[RabbitMQ] " + getUsername() + " moveu o " + (isFolder ? "pasta" : "ficheiro") + ": " + name + " de " + sourcePath + " para " + destPath);
        return result;
    }

    @Override
    public boolean delete(String path, String name, boolean isFolder, boolean isShared) {
        boolean result = super.delete(path, name, isFolder, isShared);
        if (result) RabbitUtils.publish("[RabbitMQ] " + getUsername() + " apagou o " + (isFolder ? "pasta" : "ficheiro") + ": " + path + "/" + name);
        return result;
    }
}
