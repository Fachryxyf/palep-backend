package com.palep.workoutrecommender;

import com.palep.workoutrecommender.dto.RekomendasiUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RekomendasiService {

    @Autowired
    private RekomendasiRepository rekomendasiRepository;

    // Bobot fitur dan nilai K sesuai dokumen skripsi - DITAMBAH GENDER
    private static final double BOBOT_TUJUAN = 0.3;
    private static final double BOBOT_BMI = 0.25;
    private static final double BOBOT_USIA = 0.2;
    private static final double BOBOT_FREKUENSI = 0.1;
    private static final double BOBOT_GENDER = 0.15; // Bobot baru untuk gender
    private static final int K_VALUE = 5;

    /**
     * Method utama untuk membuat rekomendasi baru menggunakan siklus CBR.
     */
    public Rekomendasi createRekomendasi(RekomendasiRequest request) {
        // TAHAP 1: RETRIEVE - Filter berdasarkan gender terlebih dahulu
        List<Rekomendasi> caseBase = rekomendasiRepository.findAll()
                .stream()
                .filter(oldCase -> oldCase.getJenisKelamin() != null && 
                        oldCase.getJenisKelamin().equalsIgnoreCase(request.getJenisKelamin()))
                .collect(Collectors.toList());

        if (caseBase.isEmpty()) {
            return retainNewCase(request, createDefaultRekomendasi(request));
        }

        Map<Rekomendasi, Double> similarityScores = new HashMap<>();
        for (Rekomendasi oldCase : caseBase) {
            double similarity = calculateSimilarity(request, oldCase);
            similarityScores.put(oldCase, similarity);
        }

        List<Rekomendasi> nearestNeighbors = similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Rekomendasi, Double>comparingByValue().reversed())
                .limit(K_VALUE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // TAHAP 2: REUSE
        Rekomendasi adaptedSolution = adaptSolution(nearestNeighbors, request);

        // TAHAP 3: RETAIN
        return retainNewCase(request, adaptedSolution);
    }

    /**
     * Method untuk mengambil semua riwayat rekomendasi.
     */
    public List<Rekomendasi> getAllRekomendasi() {
        return rekomendasiRepository.findAll(Sort.by(Sort.Direction.DESC, "tanggal"));
    }

    /**
     * Method untuk merevisi rekomendasi yang ada.
     */
    public Rekomendasi updateRekomendasi(Long id, RekomendasiUpdateRequest request) {
        Rekomendasi caseToUpdate = rekomendasiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rekomendasi tidak ditemukan dengan id: " + id));

        caseToUpdate.setJenisLatihan(request.getJenisLatihan());
        caseToUpdate.setFrekuensi(request.getFrekuensi());
        caseToUpdate.setDurasi(request.getDurasi());
        caseToUpdate.setIntensitas(request.getIntensitas());
        caseToUpdate.setDetailProgram(request.getDetailProgram());
        caseToUpdate.setCatatanKhusus(request.getCatatanKhusus());

        return rekomendasiRepository.save(caseToUpdate);
    }

    // --- Private Helper Methods ---

    private double calculateSimilarity(RekomendasiRequest newCase, Rekomendasi oldCase) {
        double simUsia = 1 - Math.abs(normalize(newCase.getUsia(), 15, 80) - normalize(oldCase.getUsia(), 15, 80));
        double simBmi = 1 - Math.abs(normalize(newCase.getBmi(), 15, 40) - normalize(oldCase.getBmi(), 15, 40));
        double simFrekuensi = 1 - Math.abs(
                normalize(newCase.getFrekuensiLatihan(), 1, 7) - normalize(oldCase.getFrekuensiLatihanInput(), 1, 7));
        double simTujuan = newCase.getTujuanLatihan().equalsIgnoreCase(oldCase.getTujuanLatihan()) ? 1.0 : 0.0;
        double simGender = newCase.getJenisKelamin().equalsIgnoreCase(oldCase.getJenisKelamin()) ? 1.0 : 0.0;
        
        return (simTujuan * BOBOT_TUJUAN) + (simBmi * BOBOT_BMI) + (simUsia * BOBOT_USIA)
                + (simFrekuensi * BOBOT_FREKUENSI) + (simGender * BOBOT_GENDER);
    }

    private Rekomendasi adaptSolution(List<Rekomendasi> neighbors, RekomendasiRequest request) {
        Map<String, Long> solutionCounts = neighbors.stream()
                .map(Rekomendasi::getDetailProgram)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        String winningSolutionDetail = Collections.max(solutionCounts.entrySet(), Map.Entry.comparingByValue())
                .getKey();
        Rekomendasi template = neighbors.stream().filter(c -> c.getDetailProgram().equals(winningSolutionDetail))
                .findFirst().orElse(neighbors.get(0));
        
        Rekomendasi newRekomendasi = new Rekomendasi();
        newRekomendasi.setJenisLatihan(template.getJenisLatihan());
        newRekomendasi.setDurasi(template.getDurasi());
        newRekomendasi.setIntensitas(template.getIntensitas());
        newRekomendasi.setDetailProgram(template.getDetailProgram());
        
        // Adaptasi berdasarkan karakteristik pengguna
        if (request.getUsia() > 40) {
            newRekomendasi.setIntensitas("Ringan ke Sedang");
        }
        
        // Adaptasi berdasarkan gender dan BMI
        if (request.getBmi() > 25) {
            if (request.getJenisKelamin().equalsIgnoreCase("Perempuan")) {
                newRekomendasi.setCatatanKhusus("Fokus pada cardio dan toning untuk pembakaran kalori. Program disesuaikan untuk wanita.");
            } else {
                newRekomendasi.setCatatanKhusus("Fokus pada strength training dan cardio intensif untuk pembakaran kalori.");
            }
        } else {
            newRekomendasi.setCatatanKhusus("Program disesuaikan dengan tujuan: " + request.getTujuanLatihan() + 
                    " untuk " + request.getJenisKelamin().toLowerCase());
        }
        
        newRekomendasi.setFrekuensi(request.getFrekuensiLatihan() + " kali/minggu");
        return newRekomendasi;
    }

    private Rekomendasi retainNewCase(RekomendasiRequest request, Rekomendasi solution) {
        Rekomendasi newCase = new Rekomendasi();
        newCase.setNama(request.getNama());
        newCase.setUsia(request.getUsia());
        newCase.setBmi(request.getBmi());
        newCase.setTujuanLatihan(request.getTujuanLatihan());
        newCase.setFrekuensiLatihanInput(request.getFrekuensiLatihan());
        newCase.setJenisKelamin(request.getJenisKelamin()); // Set gender
        newCase.setTanggal(LocalDate.now());
        newCase.setJenisLatihan(solution.getJenisLatihan());
        newCase.setFrekuensi(solution.getFrekuensi());
        newCase.setDurasi(solution.getDurasi());
        newCase.setIntensitas(solution.getIntensitas());
        newCase.setDetailProgram(solution.getDetailProgram());
        newCase.setCatatanKhusus(solution.getCatatanKhusus());
        return rekomendasiRepository.save(newCase);
    }

    private double normalize(double value, double min, double max) {
        if (max == min)
            return 0;
        return (value - min) / (max - min);
    }

    private Rekomendasi createDefaultRekomendasi(RekomendasiRequest request) {
        Rekomendasi defaultRekomendasi = new Rekomendasi();
        
        // Rekomendasi default berdasarkan gender
        if (request.getJenisKelamin().equalsIgnoreCase("Perempuan")) {
            defaultRekomendasi.setJenisLatihan("Kombinasi Cardio dan Toning");
            defaultRekomendasi.setDetailProgram(
                    "• Cardio: 25 menit (65-70% MHR)\n• Toning & Strength: 20 menit (fokus pada core, glutes, dan arms)\n• Flexibility: 10 menit stretching");
            defaultRekomendasi.setCatatanKhusus("Program khusus wanita dengan fokus pada toning dan fleksibilitas.");
        } else {
            defaultRekomendasi.setJenisLatihan("Kombinasi Strength dan Cardio");
            defaultRekomendasi.setDetailProgram(
                    "• Strength Training: 25 menit (fokus compound movements)\n• Cardio: 20 menit (70-75% MHR)\n• Core strengthening: 10 menit");
            defaultRekomendasi.setCatatanKhusus("Program khusus pria dengan fokus pada strength building dan massa otot.");
        }
        
        defaultRekomendasi.setFrekuensi(request.getFrekuensiLatihan() + " kali/minggu");
        defaultRekomendasi.setDurasi("50-60 menit/sesi");
        defaultRekomendasi.setIntensitas("Sedang");
        
        return defaultRekomendasi;
    }
}