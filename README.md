# Formulário de Cadastro com Validação

Aplicação web para cadastro e autenticação de usuários. Possui validações de dados, login seguro, recuperação de senha e notificações por e-mail.

## Recursos

- Cadastro com validação de nome completo, e-mail e senha.
- Bloqueio de e-mails duplicados.
- Login com senhas protegidas por BCrypt.
- Recuperação de senha por link temporário enviado por e-mail.
- Avisos por e-mail no cadastro e no login.
- Persistência de dados no MySQL.
- Interface responsiva com feedback visual de validação.

## Tecnologias

- Java 17 e Spring Boot
- Spring Data JPA
- MySQL
- HTML, CSS e JavaScript
- Gradle

## Pré-requisitos

- JDK 17 ou superior.
- MySQL em execução.
- Um banco de dados chamado `formulario_db`.

Crie o banco, caso ainda não exista:

```sql
CREATE DATABASE formulario_db;
```

## Como executar

No PowerShell, acesse a pasta do projeto:

```powershell
cd "C:\caminho\para\formularioComValidacao"
```

Defina as credenciais locais do banco e, se desejar receber os e-mails, configure uma senha de app do Gmail. Não use a senha comum da sua conta.

```powershell
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
$env:MAIL_PASSWORD="SUA_SENHA_DE_APP_GMAIL"
```

Em seguida, inicie a aplicação:

```powershell
.\gradlew.bat bootRun
```

Abra [http://localhost:8080](http://localhost:8080) no navegador.

## Configuração de e-mail

O projeto usa SMTP do Gmail. Para habilitar o envio, ative a verificação em duas etapas na conta Google e gere uma **senha de app**. Configure a senha com a variável `MAIL_PASSWORD` antes de iniciar a aplicação.

Os e-mails são enviados para:

- confirmação de criação de conta;
- aviso de login realizado;
- recuperação de senha.

> Nunca envie senhas, senhas de app ou credenciais do banco para o repositório.

## Banco de dados

O Spring Boot cria e atualiza as tabelas automaticamente. A tabela principal é `usuarios`, com os campos `id`, `nome`, `email` e `senha`.

As senhas são armazenadas como hash BCrypt, nunca em texto puro.
