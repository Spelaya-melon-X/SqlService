
FROM ubuntu:24.04

# Обновляем пакетный менеджер и устанавливаем Java 21 (OpenJDK)
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    && rm -rf /var/list/apt/lists/*

# Проверяем версию Java при старте (чтобы контейнер не закрывался сразу, запускаем бесконечный цикл Bash)
CMD ["bash", "-c", "java -version && exec bash"]
