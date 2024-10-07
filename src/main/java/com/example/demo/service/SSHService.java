package com.example.demo.service;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
public class SSHService {

    private Session session;

    // Método para conectar ao servidor via SSH
    public void connect(String username, String password, String host) throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, 22);
        session.setPassword(password);

        // Configurando para não verificar a chave do host (não recomendado para produção)
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    }

    // Método para desconectar a sessão SSH
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    // Método para listar arquivos em um diretório remoto
    public List<String> listFiles(String directory) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        List<String> files = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(directory);
        for (ChannelSftp.LsEntry entry : list) {
            String fileName = entry.getFilename();
            if (!fileName.equals(".") && !fileName.equals("..")) {
                files.add(fileName);
            }
        }

        sftpChannel.disconnect();
        return files;
    }

    // Método para baixar um arquivo de log específico
    public InputStream downloadFile(String directory, String fileName) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        InputStream inputStream = sftpChannel.get(directory + "/" + fileName);

        sftpChannel.disconnect();
        return inputStream;
    }

    // Método para obter a sessão SSH atual
    public Session getSession() {
        return session;
    }

}
