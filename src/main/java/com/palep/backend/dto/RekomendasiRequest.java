package com.palep.backend.dto;

// Anotasi @Data dihapus untuk sementara
public class RekomendasiRequest {
    private String nama;
    private int usia;
    private String jenisKelamin;
    private double beratBadan;
    private double tinggiBadan;
    private String tujuanLatihan;
    private int frekuensiLatihan;

    // --- GETTER MANUAL ---
    public String getNama() {
        return nama;
    }

    public int getUsia() {
        return usia;
    }
    
    public String getTujuanLatihan() {
        return tujuanLatihan;
    }
    
    // Anda bisa tambahkan getter lain jika dibutuhkan
    public String getJenisKelamin() { return jenisKelamin; }
    public double getBeratBadan() { return beratBadan; }
    public double getTinggiBadan() { return tinggiBadan; }
    public int getFrekuensiLatihan() { return frekuensiLatihan; }
}
