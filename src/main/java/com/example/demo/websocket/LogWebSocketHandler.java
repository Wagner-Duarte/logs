package com.example.demo.websocket;

import com.example.demo.service.SSHService;
import com.jcraft.jsch.ChannelExec;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private final SSHService sshService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public LogWebSocketHandler(SSHService sshService) {
        this.sshService = sshService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String fileName = (String) session.getAttributes().get("fileName");
        if (fileName != null) {
            executorService.submit(() -> streamLogContent(session, fileName));
        } else {
            session.close();
            System.err.println("FileName não fornecido na conexão WebSocket.");
        }
    }

    private void streamLogContent(WebSocketSession session, String fileName) {
        try {
            System.out.println("Iniciando streaming do log: " + fileName);
            ChannelExec channel = (ChannelExec) sshService.getSession().openChannel("exec");
            channel.setCommand("tail -f " + escapePath(fileName));
            InputStream inputStream = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null && session.isOpen()) {
                System.out.println("Enviando linha do log: " + line);
                session.sendMessage(new TextMessage(line));
            }
            channel.disconnect();
            System.out.println("Streaming do log encerrado: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                session.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private String escapePath(String path) {
        // Método para escapar o caminho do arquivo, se necessário
        // Por exemplo, substituir espaços por \ ou usar aspas
        return "'" + path.replace("'", "\\'") + "'";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        System.out.println("WebSocket fechado: " + session.getId());
    }
}