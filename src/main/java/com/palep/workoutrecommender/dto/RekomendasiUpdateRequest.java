package com.palep.workoutrecommender.dto;

import lombok.Data;

@Data
public class RekomendasiUpdateRequest {
    private String jenisLatihan;
    private String frekuensi;
    private String durasi;
    private String intensitas;
    private String detailProgram;
    private String catatanKhusus;
}