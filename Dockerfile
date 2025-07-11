# =================================================================
# TAHAP 1: BUILD STAGE (Untuk meng-compile aplikasi dengan Maven)
# =================================================================
# Gunakan image resmi Maven yang sudah berisi JDK 17
FROM maven:3.9.6-eclipse-temurin-17-focal AS builder

# Tentukan direktori kerja di dalam container
WORKDIR /app

# Salin file pom.xml terlebih dahulu untuk optimasi cache Docker
# Docker hanya akan download dependency jika pom.xml berubah
COPY pom.xml .
RUN mvn dependency:go-offline

# Salin sisa source code proyek Anda
COPY src ./src

# Jalankan perintah Maven untuk membuat file .jar
# -DskipTests untuk mempercepat proses build dengan melewatkan testing
RUN mvn package -DskipTests

# =================================================================
# TAHAP 2: FINAL STAGE (Untuk menjalankan aplikasi)
# =================================================================
# Gunakan image JRE (Java Runtime Environment) yang lebih ringan
FROM eclipse-temurin:17-jre-focal

# Tentukan direktori kerja
WORKDIR /app

# Salin file .jar yang sudah dibuat di tahap 'builder' ke image final ini
# Perhatikan `COPY --from=builder`
COPY --from=builder /app/target/*.jar app.jar

# Expose port yang digunakan aplikasi Anda (misal: 8080)
EXPOSE 8080

# Perintah untuk menjalankan aplikasi saat container dimulai
ENTRYPOINT ["java", "-jar", "app.jar"]