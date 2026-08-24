const cadastro = document.querySelector('#formulario-cadastro');
const campos = { nome: document.querySelector('#nome'), email: document.querySelector('#email'), senha: document.querySelector('#senha'), confirmar: document.querySelector('#confirmar-senha') };
const emailValido = (valor) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor.trim());
const nomeCompleto = (valor) => /^[\p{L}]{2,}(?:[ '-][\p{L}]+)*[ '-][\p{L}]{2,}$/u.test(valor.trim());
const senhaForte = (valor) => /^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\s]).{8,}$/.test(valor);
function erro(campo, texto) { document.querySelector(`#erro-${campo.id}`).textContent = texto; campo.classList.toggle('invalido', !!texto); campo.classList.toggle('valido', !texto && !!campo.value); return !texto; }
function mensagem(id, texto, tipo = '') { const el = document.querySelector(id); el.textContent = texto; el.className = `mensagem ${tipo}`; }
function validarCadastro(campo) {
  if (campo === campos.nome) return erro(campo, !nomeCompleto(campo.value) ? 'Informe seu nome e sobrenome usando apenas letras.' : '');
  if (campo === campos.email) return erro(campo, !emailValido(campo.value) ? 'Informe um e-mail válido.' : '');
  if (campo === campos.senha) return erro(campo, !senhaForte(campo.value) ? 'A senha deve ter ao menos 8 caracteres, incluindo letra maiúscula, minúscula e símbolo.' : '');
  return erro(campo, campo.value !== campos.senha.value ? 'As senhas não coincidem.' : '');
}
function mostrarTela(tela) {
  const formularios = { cadastro, login: document.querySelector('#formulario-login'), recuperar: document.querySelector('#formulario-recuperar') };
  Object.entries(formularios).forEach(([nome, form]) => form.classList.toggle('oculto', nome !== tela));
  document.querySelector('#rodape-cadastro').classList.toggle('oculto', tela !== 'cadastro');
  const textos = { cadastro: ['Criar conta', 'Preencha seus dados para começar.'], login: ['Entrar', 'Informe seus dados para acessar sua conta.'], recuperar: ['Recuperar senha', 'Vamos ajudar você a voltar para sua conta.'] };
  document.querySelector('#titulo').textContent = textos[tela][0]; document.querySelector('#subtitulo').textContent = textos[tela][1];
}
Object.values(campos).forEach((campo) => campo.addEventListener('input', () => {
  if (campo.classList.contains('invalido')) validarCadastro(campo);
  if (campo === campos.senha && campos.confirmar.classList.contains('invalido')) validarCadastro(campos.confirmar);
}));
document.querySelectorAll('[data-tela]').forEach((link) => link.addEventListener('click', (e) => { e.preventDefault(); mostrarTela(link.dataset.tela); }));
document.querySelectorAll('.mostrar-senha').forEach((botao) => botao.addEventListener('click', () => { const campo = document.querySelector(`#${botao.dataset.alvo}`); const mostrar = campo.type === 'password'; campo.type = mostrar ? 'text' : 'password'; botao.textContent = mostrar ? 'Ocultar' : 'Mostrar'; }));
cadastro.addEventListener('submit', async (e) => {
  e.preventDefault(); mensagem('#mensagem-geral', ''); if (!Object.values(campos).map(validarCadastro).every(Boolean)) return;
  const botao = cadastro.querySelector('button[type="submit"]'); botao.disabled = true; botao.textContent = 'Cadastrando...';
  try { const resposta = await fetch('/api/usuarios', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({nome:campos.nome.value.trim(),email:campos.email.value.trim(),senha:campos.senha.value}) }); const dados = await resposta.json(); if (!resposta.ok) throw new Error(dados.mensagem); mensagem('#mensagem-geral', dados.mensagem, 'sucesso'); cadastro.reset(); Object.values(campos).forEach((c) => c.classList.remove('valido')); } catch (ex) { mensagem('#mensagem-geral', ex.message, 'erro-geral'); } finally { botao.disabled = false; botao.textContent = 'Cadastrar'; }
});
document.querySelector('#formulario-login').addEventListener('submit', async (e) => {
  e.preventDefault(); const email = document.querySelector('#login-email'), senha = document.querySelector('#login-senha'); const valido = erro(email, !emailValido(email.value) ? 'Informe um e-mail válido.' : '') && erro(senha, !senha.value ? 'Informe sua senha.' : ''); if (!valido) return;
  try { const resposta = await fetch('/api/usuarios/login', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({email:email.value,senha:senha.value}) }); const dados = await resposta.json(); if (!resposta.ok) throw new Error(dados.mensagem); mensagem('#mensagem-login', dados.mensagem, 'sucesso'); } catch (ex) { mensagem('#mensagem-login', ex.message, 'erro-geral'); }
});
document.querySelector('#formulario-recuperar').addEventListener('submit', async (e) => {
  e.preventDefault(); const email = document.querySelector('#recuperar-email'); if (!erro(email, !emailValido(email.value) ? 'Informe um e-mail válido.' : '')) return;
  try { const resposta = await fetch('/api/usuarios/recuperar-senha', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({email:email.value}) }); const dados = await resposta.json(); if (!resposta.ok) throw new Error(dados.mensagem); mensagem('#mensagem-recuperar', dados.mensagem, 'sucesso'); } catch (ex) { mensagem('#mensagem-recuperar', ex.message, 'erro-geral'); }
});
