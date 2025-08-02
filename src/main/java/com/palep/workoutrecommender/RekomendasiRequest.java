package com.palep.workoutrecommender;

import lombok.Data;

@Data
public class RekomendasiRequest {
    private String nama;
    private int usia;
    private double bmi;
    private String tujuanLatihan;
    private int frekuensiLatihan;
    private String jenisKelamin; // Field baru untuk gender filtering
}