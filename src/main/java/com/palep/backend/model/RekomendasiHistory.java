package com.palep.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_history")
@Data // Menggunakan Lombok untuk getter, setter, dll.
public class RekomendasiHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Kolom dari input pengguna
    private String nama;
    private int usia;

    @Column(name = "jenis_kelamin")
    private String jenisKelamin;

    @Column(name = "berat_badan")
    private double beratBadan;

    @Column(name = "tinggi_badan")
    private double tinggiBadan;

    private double bmi;

    @Column(name = "tujuan_latihan")
    private String tujuanLatihan;

    @Column(name = "frekuensi_latihan")
    private int frekuensiLatihan;

    // Kolom dari hasil rekomendasi
    @Column(name = "jenis_latihan")
    private String jenisLatihanRec; // Nama dibedakan agar tidak konflik

    private String frekuensi;
    private String durasi;
    private String intensitas;

    @Column(name = "detail_program", columnDefinition = "TEXT")
    private String detailProgram;

    @Column(name = "catatan_khusus", columnDefinition = "TEXT")
    private String catatanKhusus;
}
