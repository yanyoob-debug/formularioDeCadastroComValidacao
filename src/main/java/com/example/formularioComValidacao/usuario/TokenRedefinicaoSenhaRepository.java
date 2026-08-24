package com.example.formularioComValidacao.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {
    Optional<TokenRedefinicaoSenha> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByEmail(String email);
}
