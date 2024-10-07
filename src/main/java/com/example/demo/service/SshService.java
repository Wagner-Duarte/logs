package com.example.demo.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SshService {

    private Session createSession(String username, String password, String ipAddress) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, ipAddress, 22); // Porta SSH padrão: 22
        session.setPassword(password);

        // Evitar verificar a chave do host
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(30000); // Tempo limite de conexão: 30 segundos
        return session;
    }

    public List<String> listFiles(String username, String password, String ipAddress, String directory) throws Exception {
        Session session = null;
        ChannelExec channel = null;
        List<String> fileList = new ArrayList<>();

        try {
            session = createSession(username, password, ipAddress);
            channel = (ChannelExec) session.openChannel("exec");
            String command = "ls -1 " + directory;
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            channel.connect();

            int readByte = in.read();
            StringBuilder output = new StringBuilder();
            while (readByte != 0xffffffff) {
                output.append((char) readByte);
                readByte = in.read();
            }

            String[] files = output.toString().split("\\r?\\n");
            for (String file : files) {
                fileList.add(file);
            }

        } catch (JSchException | java.io.IOException e) {
            throw new Exception("Erro ao listar arquivos via SSH: " + e.getMessage());
        } finally {
            if (channel != null && !channel.isClosed()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return fileList;
    }

    public String readFile(String username, String password, String ipAddress, String directory, String filename) throws Exception {
        Session session = null;
        ChannelExec channel = null;
        StringBuilder fileContent = new StringBuilder();

        try {
            session = createSession(username, password, ipAddress);
            channel = (ChannelExec) session.openChannel("exec");
            String command = "cat " + directory + "/" + filename;
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            channel.connect();

            int readByte = in.read();
            while (readByte != 0xffffffff) {
                fileContent.append((char) readByte);
                readByte = in.read();
            }

        } catch (JSchException | java.io.IOException e) {
            throw new Exception("Erro ao ler arquivo via SSH: " + e.getMessage());
        } finally {
            if (channel != null && !channel.isClosed()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return fileContent.toString();
    }

}
