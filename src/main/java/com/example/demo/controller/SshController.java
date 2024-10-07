package com.example.demo.controller;

import com.example.demo.dto.SshRequestDTO;
import com.example.demo.service.SshService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class SshController {

    @Autowired
    private SshService sshService;

    /**
     * Renderiza a página inicial com o formulário.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("request", new SshRequestDTO());
        return "index";
    }

    /**
     * Endpoint para listar arquivos em um diretório via SSH.
     */
    @PostMapping("/api/ssh/list-files")
    public String listFiles(@Valid @ModelAttribute("request") SshRequestDTO request,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "index"; // Retorna para a página inicial se houver erros de validação
        }

        try {
            List<String> files = sshService.listFiles(
                    request.getUsername(),
                    request.getPassword(),
                    request.getIpAddress(),
                    request.getDirectory()
            );
            model.addAttribute("files", files);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao listar arquivos: " + e.getMessage());
        }

        return "index";
    }

    /**
     * Endpoint para ler o conteúdo de um arquivo via SSH.
     */
    @PostMapping("/api/ssh/read-file")
    public String readFile(@Valid @ModelAttribute("request") SshRequestDTO request,
                           BindingResult bindingResult,
                           @RequestParam String filename,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "index"; // Retorna para a página inicial se houver erros de validação
        }

        try {
            String content = sshService.readFile(
                    request.getUsername(),
                    request.getPassword(),
                    request.getIpAddress(),
                    request.getDirectory(),
                    filename
            );
            model.addAttribute("fileContent", content);
            model.addAttribute("fileName", filename);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao ler arquivo: " + e.getMessage());
        }

        return "index";
    }

    /**
     * Endpoint para baixar um arquivo via SSH.
     */
    @PostMapping("/api/ssh/download-file")
    public ResponseEntity<byte[]> downloadFile(@Valid @ModelAttribute("request") SshRequestDTO request,
                                               BindingResult bindingResult,
                                               @RequestParam String filename) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build(); // Retorna 400 se houver erros de validação
        }

        try {
            String content = sshService.readFile(
                    request.getUsername(),
                    request.getPassword(),
                    request.getIpAddress(),
                    request.getDirectory(),
                    filename
            );

            byte[] fileBytes = content.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
