// File: src/main/java/com/palep/workoutrecommender/Rekomendasi.java
package com.palep.workoutrecommender;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Rekomendasi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nama;

    // Input Pengguna (Problem)
    private int usia;
    private double bmi;
    private String tujuanLatihan;
    private int frekuensiLatihanInput;

    // Output Sistem (Solution)
    private LocalDate tanggal;
    private String jenisLatihan;
    private String frekuensi;
    private String durasi;
    private String intensitas;
    @Column(length = 1000) // Perbanyak panjang kolom
    private String detailProgram;
    @Column(length = 1000)
    private String catatanKhusus;
}