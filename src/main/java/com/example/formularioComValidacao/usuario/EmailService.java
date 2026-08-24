package com.example.formularioComValidacao.usuario;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;
    private final String senhaConfigurada;
    private final String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.username:}") String remetente,
                        @Value("${spring.mail.password:}") String senhaConfigurada,
                        @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender; this.remetente = remetente; this.senhaConfigurada = senhaConfigurada; this.baseUrl = baseUrl;
    }

    public void enviarBoasVindas(String destinatario, String nome) {
        enviar(destinatario, "Cadastro realizado com sucesso", "Olá, " + nome + "!\n\nSua conta foi criada com sucesso.");
    }

    public void enviarLinkRedefinicao(String destinatario, String token) {
        String link = baseUrl + "/redefinir-senha.html?token=" + token;
        enviar(destinatario, "Redefinição de senha", "Recebemos um pedido para redefinir sua senha.\n\nUse este link em até 30 minutos:\n" + link + "\n\nSe não foi você, ignore este e-mail.");
    }

    public void enviarAvisoLogin(String destinatario, String nome) {
        enviar(destinatario, "Novo acesso à sua conta", "Olá, " + nome + "!\n\nUm login foi realizado com sucesso em sua conta.\n\nSe não foi você, altere sua senha imediatamente.");
    }

    private void enviar(String destinatario, String assunto, String texto) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null || remetente.isBlank() || senhaConfigurada.isBlank()) {
            log.warn("E-mail não enviado: configure MAIL_PASSWORD antes de iniciar a aplicação.");
            throw new EmailDeliveryException("A configuração de e-mail está incompleta.", null);
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente); mensagem.setTo(destinatario); mensagem.setSubject(assunto); mensagem.setText(texto);
        try {
            sender.send(mensagem);
            log.info("E-mail enviado para {}: {}", destinatario, assunto);
        } catch (MailException exception) {
            log.error("Não foi possível enviar o e-mail para {}", destinatario, exception);
            throw new EmailDeliveryException("O servidor de e-mail recusou a mensagem.", exception);
        }
    }
}
