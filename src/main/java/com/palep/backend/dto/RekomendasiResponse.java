package com.palep.backend.dto;

import java.time.LocalDateTime;

// Versi manual tanpa Lombok
public class RekomendasiResponse {

    private Long id;
    private LocalDateTime createdAt;
    private String nama;
    private String jenisLatihan;
    private String frekuensi;
    private String durasi;
    private String intensitas;
    private String detailProgram;
    private String catatanKhusus;

    // Constructor kosong (diperlukan oleh beberapa library)
    public RekomendasiResponse() {}

    // Constructor dengan SEMUA argumen (INI YANG DIPERLUKAN)
    public RekomendasiResponse(Long id, LocalDateTime createdAt, String nama, String jenisLatihan, String frekuensi, String durasi, String intensitas, String detailProgram, String catatanKhusus) {
        this.id = id;
        this.createdAt = createdAt;
        this.nama = nama;
        this.jenisLatihan = jenisLatihan;
        this.frekuensi = frekuensi;
        this.durasi = durasi;
        this.intensitas = intensitas;
        this.detailProgram = detailProgram;
        this.catatanKhusus = catatanKhusus;
    }

    // --- GETTERS & SETTERS UNTUK SEMUA FIELD ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJenisLatihan() { return jenisLatihan; }
    public void setJenisLatihan(String jenisLatihan) { this.jenisLatihan = jenisLatihan; }

    public String getFrekuensi() { return frekuensi; }
    public void setFrekuensi(String frekuensi) { this.frekuensi = frekuensi; }

    public String getDurasi() { return durasi; }
    public void setDurasi(String durasi) { this.durasi = durasi; }

    public String getIntensitas() { return intensitas; }
    public void setIntensitas(String intensitas) { this.intensitas = intensitas; }

    public String getDetailProgram() { return detailProgram; }
    public void setDetailProgram(String detailProgram) { this.detailProgram = detailProgram; }

    public String getCatatanKhusus() { return catatanKhusus; }
    public void setCatatanKhusus(String catatanKhusus) { this.catatanKhusus = catatanKhusus; }
}
