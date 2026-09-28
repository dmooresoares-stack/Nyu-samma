# Nyu Samma A16 — checkpoint v0.4

Projeto Android reconstruído para o **Samsung Galaxy A16**, com foco em presença contínua da Nyu Samma na tela.

## O que este checkpoint já contém

- overlay flutuante (`SYSTEM_ALERT_WINDOW`);
- serviço em primeiro plano;
- reconhecimento de fala do Android tentando detectar o chamado **“Nyu”**;
- resposta falada por TTS do próprio Android;
- estados visuais: ociosa, ouvindo, respondendo, descansando;
- brincadeira ociosa com **bolinha verde**;
- brincadeira/roída de **ossinho branco**;
- tentativa de reinício automático da escuta enquanto o serviço estiver vivo;
- receiver de inicialização do aparelho;
- referências visuais em `docs/reference-art/`;
- workflow do GitHub Actions para gerar o APK de debug.

## Limites importantes deste checkpoint

Este repositório **não contém ainda a integração com a conta ChatGPT**, a memória pessoal do ChatGPT nem a mesma voz do modo de voz do ChatGPT. A resposta falada atual usa o `TextToSpeech` do Android. O `SpeechRecognizer` do Android também não é um hotword engine garantido e pode ser limitado pelo sistema, especialmente em segundo plano.

Em versões recentes do Android, o sistema pode bloquear o início de um serviço de microfone diretamente após o boot. Por isso, depois de reiniciar o celular, pode ser necessário abrir o app uma vez e tocar em **Iniciar Nyu Samma**.

## Gerar o APK pelo GitHub

1. Abra a aba **Actions** deste repositório.
2. Abra o workflow **Build Android APK**.
3. Abra a execução mais recente.
4. Em **Artifacts**, baixe `NyuSamma-debug-apk`.
5. Dentro do ZIP do artifact estará `app-debug.apk`.

## Abrir no Android Studio

Abra a pasta raiz do projeto no Android Studio. O projeto usa:

- Android Gradle Plugin `8.7.3`
- Gradle `8.9` no workflow
- JDK 17
- `compileSdk 35`
- `minSdk 26`
- `targetSdk 35`

## Próxima camada planejada

A arquitetura deste checkpoint deixa a parte de presença/overlay separada do futuro motor de conversa. A próxima etapa é substituir a resposta TTS local por um backend autorizado de conversa/voz e definir uma estratégia real de wake word compatível com o Galaxy A16.
