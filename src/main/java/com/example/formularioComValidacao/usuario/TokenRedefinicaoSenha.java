package com.example.formularioComValidacao.usuario;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens_redefinicao_senha")
public class TokenRedefinicaoSenha {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String token;
    @Column(nullable = false, length = 150)
    private String email;
    @Column(nullable = false)
    private LocalDateTime expiraEm;

    protected TokenRedefinicaoSenha() { }
    public TokenRedefinicaoSenha(String token, String email, LocalDateTime expiraEm) {
        this.token = token; this.email = email; this.expiraEm = expiraEm;
    }
    public String getEmail() { return email; }
    public boolean expirado() { return LocalDateTime.now().isAfter(expiraEm); }
}
