package com.example.formularioComValidacao.usuario;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioController(UsuarioRepository usuarioRepository, TokenRedefinicaoSenhaRepository tokenRepository,
                             EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> cadastrar(@RequestBody CadastroRequest dados) {
        String nome = dados.nome() == null ? "" : dados.nome().trim();
        String email = dados.email() == null ? "" : dados.email().trim().toLowerCase(Locale.ROOT);
        String senha = dados.senha() == null ? "" : dados.senha();

        if (!nome.matches("^[\\p{L}]{2,}(?:[ '-][\\p{L}]+)*[ '-][\\p{L}]{2,}$")) return erro("Informe seu nome e sobrenome usando apenas letras.");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return erro("Informe um e-mail válido.");
        if (!senhaForte(senha)) return erro("A senha deve ter ao menos 8 caracteres, incluindo letra maiúscula, minúscula e símbolo.");
        if (usuarioRepository.existsByEmailIgnoreCase(email)) return erro("Este e-mail já está cadastrado.");

        try {
            usuarioRepository.saveAndFlush(new Usuario(nome, email, passwordEncoder.encode(senha)));
            try {
                emailService.enviarBoasVindas(email, nome);
            } catch (EmailDeliveryException exception) {
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensagem", "Conta criada, mas não foi possível enviar o e-mail de confirmação. Verifique a configuração de e-mail."));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Conta criada com sucesso!"));
        } catch (DataIntegrityViolationException exception) {
            return erro("Este e-mail já está cadastrado.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest dados) {
        String email = dados.email() == null ? "" : dados.email().trim().toLowerCase(Locale.ROOT);
        String senha = dados.senha() == null ? "" : dados.senha();
        var usuario = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuario.isEmpty() || !passwordEncoder.matches(senha, usuario.get().getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "E-mail ou senha incorretos."));
        }
        try {
            emailService.enviarAvisoLogin(usuario.get().getEmail(), usuario.get().getNome());
        } catch (EmailDeliveryException exception) {
            return ResponseEntity.ok(Map.of("mensagem", "Login realizado, mas não foi possível enviar o aviso por e-mail."));
        }
        return ResponseEntity.ok(Map.of("mensagem", "Login realizado com sucesso!"));
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@RequestBody EmailRequest dados) {
        String email = dados.email() == null ? "" : dados.email().trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return erro("Informe um e-mail válido.");
        var usuario = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuario.isPresent()) {
            tokenRepository.deleteByEmail(email);
            String token = UUID.randomUUID().toString();
            tokenRepository.save(new TokenRedefinicaoSenha(token, email, LocalDateTime.now().plusMinutes(30)));
            try {
                emailService.enviarLinkRedefinicao(email, token);
            } catch (EmailDeliveryException exception) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("mensagem", "Não foi possível enviar as instruções por e-mail. Tente novamente em instantes."));
            }
        }
        return ResponseEntity.ok(Map.of("mensagem", "Se houver uma conta com este e-mail, as instruções serão enviadas."));
    }

    @PostMapping("/redefinir-senha")
    @Transactional
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody RedefinirSenhaRequest dados) {
        String senha = dados.senha() == null ? "" : dados.senha();
        if (!senhaForte(senha)) return erro("A senha deve ter ao menos 8 caracteres, incluindo letra maiúscula, minúscula e símbolo.");
        var token = tokenRepository.findByToken(dados.token() == null ? "" : dados.token());
        if (token.isEmpty() || token.get().expirado()) return erro("Este link é inválido ou expirou.");
        var usuario = usuarioRepository.findByEmailIgnoreCase(token.get().getEmail());
        if (usuario.isEmpty()) return erro("Este link é inválido ou expirou.");
        usuario.get().alterarSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario.get());
        tokenRepository.delete(token.get());
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso. Você já pode entrar."));
    }

    private boolean senhaForte(String senha) {
        return senha.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s]).{8,}$");
    }

    private ResponseEntity<Map<String, String>> erro(String mensagem) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }
}
