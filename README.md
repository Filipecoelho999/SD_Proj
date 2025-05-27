# SD
Sistemas Distribuidos 2024/25

# 📦 Distributed Drive System (Projecto SD 2025)

Este projeto implementa um sistema de partilha de ficheiros distribuído com autenticação, estrutura de diretórios, propagação de alterações entre clientes, utilizando Java RMI e RabbitMQ.

---

## Requisitos Implementados

###  R1 – Autenticação
- Interface remota `AuthRI` com métodos `register()` e `login()`
- Implementação em `AuthImpl` com suporte a `UserStore`
- Utilizadores autenticados recebem uma instância de `Workspace`

### R2 – Sistema de Ficheiros
- Estrutura local e partilhada (`Folder`, `FileObject`)
- Operações: criar, renomear, mover, apagar e listar ficheiros/pastas
- Ações executadas através da classe `Workspace`
- Testado com `MainClient.java`

### R3.1 – Propagação Síncrona via RMI
- Interfaces `ObserverRI`, `SubjectRI`
- Cliente regista-se como observador via RMI
- Notificações recebidas via `ObserverImpl`

### R3.2 – Propagação Assíncrona via RabbitMQ
- `RabbitUtils.java` para publish automático
- `SubscribeClient.java` para escuta de eventos
- `MainClientRabbitOnly.java` para testar RabbitMQ sem depender de RMI
- Mensagens publicadas no exchange `updatesExchange`

---

## Estrutura do Projeto

```
src/
├── edu.ufp.inf.sd.rmi.drive/         # Código do RMI (Auth, Observer, Workspace)
├── edu.ufp.inf.sd.rabbitmqservices/
│   ├── drive/                        # PublishClient e SubscribeClient
│   ├── util/                         # RabbitUtils (abstração do RabbitMQ)
│   └── test/                         # MainClientRabbitOnly.java
├── runscripts/                       # Scripts .bat para build e execução
```

---

## Como testar

### RMI (R1 a R3.1)
1. `runscripts/build_&_run.bat` → compila e inicia RMI + servidor
2. `runscripts/runclient.bat` → corre cliente principal (login + operações)
3. `ClienteNotificador.java` → simula outro cliente a disparar notificações

### RabbitMQ (R3.2)
1. `runscripts/build_rabbitmq.bat` → compila apenas classes do RabbitMQ
2. `runscripts/runconsumer.bat` → escuta mensagens RabbitMQ
3. Corre `MainClient.java` ou `MainClientRabbitOnly.java`

---

## Notas Finais
- O projeto está modularizado por camadas e tecnologias.
- O `Workspace` dispara `notifyAll(...)` para RMI e RabbitMQ.
- Requisitos até R3 estão completos, testados e funcionais.

---

© Engenharia de Software • Sistemas Distribuídos 2025 • UFP