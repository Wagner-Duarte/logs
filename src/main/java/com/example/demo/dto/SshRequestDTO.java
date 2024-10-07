package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SshRequestDTO {
    @NotBlank(message = "Usuário é obrigatório")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @NotBlank(message = "Endereço IP é obrigatório")
    private String ipAddress;

    @NotBlank(message = "Diretório é obrigatório")
    private String directory;
}
