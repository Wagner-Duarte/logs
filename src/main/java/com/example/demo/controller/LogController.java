package com.example.demo.controller;

import com.example.demo.service.SSHService;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Controller
public class LogController {

    @Autowired
    private SSHService sshService;

    // Endpoint para listar os arquivos de log e exibir a página com o formulário
    @GetMapping("/")
    public String index(@RequestParam(required = false) String username,
                        @RequestParam(required = false) String password,
                        @RequestParam(required = false) String host,
                        @RequestParam(required = false) String directory, Model model) {
        if (username != null && password != null && host != null && directory != null) {
            try {
                sshService.connect(username, password, host);  // Conectar ao servidor via SSH
                List<String> logs = sshService.listFiles(directory);  // Listar arquivos de log
                model.addAttribute("logs", logs);  // Passar a lista de logs para a página
                sshService.disconnect();
            } catch (JSchException | SftpException e) {
                model.addAttribute("error", "Erro ao conectar ao servidor ou listar arquivos: " + e.getMessage());
            }
        }
        return "index";  // Renderiza a página index.html
    }

    // Endpoint para download do arquivo de log
    @GetMapping("/logs/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadLog(@RequestParam String username, @RequestParam String password,
                                                           @RequestParam String host, @RequestParam String directory,
                                                           @PathVariable String fileName) {
        try {
            sshService.connect(username, password, host);  // Conecta via SSH
            InputStream logStream = sshService.downloadFile(directory, fileName);  // Baixa o arquivo
            sshService.disconnect();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(logStream));
        } catch (JSchException | SftpException e) {
            return ResponseEntity.internalServerError().build();  // Retorna erro no download
        }
    }
}
