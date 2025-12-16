# SASIPCA - App Android & Desktop
Esta é a aplicação para o sistema de gestão de inventário e apoio social do IPCA (SASIPCA).

Elaborada pelo Grupo 8, no contexto do projeto *'50+10'*
* Miguel Areal - 29559
* Paulo Costa  - 22934
* João Lopes - 12168
* Júlio Faria - 22920

Desenvolvida em Kotlin Multiplatform (KMP) com Jetpack Compose, esta aplicação partilha a lógica entre Android e Desktop (Windows, macOS, Linux).

## Visão Geral
O projeto visa facilitar a gestão de stocks alimentares, gestão de beneficiários e agendamento de entregas.
A aplicação comunica com uma API RESTful (.NET) e utiliza SignalR/Firebase para notificações em tempo real.

### Principais Funcionalidades
* **Autenticação:** Integração com Autenticação Federada do IPCA.
* **Gestão de Inventário:** Listagem, pesquisa, criação e edição de produtos.
* **Movimentos de Stock:** Registo de entradas (receções), saídas (entregas) e ajustes manuais de stock.
* **Leitura de Códigos de Barras:** Utilização da câmara (Android) ou introdução manual/scanner USB (Desktop).
* **Gestão de Entregas:** Agendamento via calendário interativo, validação de estados e histórico.
* **Beneficiários:** Gestão de perfis e histórico de recolhas.
* **Estatísticas:** Dashboard com gráficos de consumo e fluxo de stock.
* **Notificações:** Alertas em tempo real para validades e stock baixo.
* **Relatórios:** Geração e exportação de relatórios em PDF e CSV.

## Stack Tecnológico
* **Linguagem:** Kotlin
* **UI Framework:** Compose Multiplatform (Material 3)
* **Arquitetura:** MVVM (Model-View-ViewModel)
* **Navegação:** Voyager
* **Rede (HTTP):** Ktor Client (com Content Negotiation e Auth)
* **Rede (Sockets):** Microsoft SignalR (Desktop) & Firebase Messaging (Android)

## Pré-requisitos
Para compilar e executar este projeto, é necessário ter instalado:
1. **JDK:** Versão 17 ou superior (recomendado JDK 17 ou 21).
3. **Android SDK:** Configurado no Android Studio.
4. **Kotlin Plugin:** Compatível com a versão definida no `libs.versions.toml`.

## Configuração do Projeto
1. Clone o repositório.
2. Abra o projeto no Android Studio ou IntelliJ e aguarde a sincronização do Gradle.
3. Verifique o ficheiro `sasipca/network/ApiConfig.kt` (ou equivalente) para garantir que o URL da API aponta para o ambiente correto (Desenvolvimento ou Produção).

## Como Executar
### Android
Pode executar diretamente através do Android Studio/IntelliJ selecionando a configuração `composeApp` e um emulador/dispositivo, ou via terminal:

```bash
./gradlew :composeApp:installDebug
```

### Desktop (JVM)
Para executar a aplicação desktop em modo de desenvolvimento:
```bash
./gradlew :composeApp:run
```

## Compilação e Distribuição (Release)
### Android (APK/Bundle)
Para gerar o APK assinado ou Android App Bundle:
```bash
./gradlew :composeApp:assembleRelease
```

O ficheiro gerado estará em `composeApp/build/outputs/apk/release/`.

### Desktop (Instaladores Nativos)
A aplicação utiliza a ferramenta de empacotamento nativa do Compose (`jpackage`).
Devido à dependência do MSAL (Login Microsoft), é necessário incluir módulos específicos do JDK na distribuição.

Comando para gerar o instalador (deteta automaticamente o SO atual):

```bash
./gradlew packageDistributionForCurrentOS
```

Os instaladores gerados (MSI/EXE para Windows, DMG para macOS, DEB para Linux) estarão localizados em:
`composeApp/build/compose/binaries/main/`

## Resolução de Problemas Comuns 
### Erro "com/sun/net/httpserver/HttpHandler" após instalação Desktop
Este erro ocorre porque o `jpackage` remove módulos "não utilizados" para otimizar o tamanho, mas o MSAL necessita do servidor HTTP para o callback de login.
**Solução:** Certifique-se de que o `build.gradle.kts` contém a seguinte configuração no bloco `nativeDistributions`:

```kotlin
modules("java.base", "java.desktop", "jdk.httpserver", "java.instrument", "jdk.unsupported")
```
