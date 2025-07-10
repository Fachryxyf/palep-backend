# Gunakan base image Java 17 yang ringan
FROM eclipse-temurin:17-jre-jammy

# Tentukan argumen untuk file JAR
ARG JAR_FILE=target/*.jar

# Salin file .jar yang sudah di-build oleh Maven ke dalam image
COPY ${JAR_FILE} app.jar

# Expose port yang digunakan oleh Spring Boot di dalam container
EXPOSE 8080

# Perintah untuk menjalankan aplikasi saat container dimulai
ENTRYPOINT ["java","-jar","/app.jar"]