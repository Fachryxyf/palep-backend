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

    // Bobot fitur dan nilai K sesuai dokumen skripsi
    private static final double BOBOT_TUJUAN = 0.4;
    private static final double BOBOT_BMI = 0.3;
    private static final double BOBOT_USIA = 0.2;
    private static final double BOBOT_FREKUENSI = 0.1;
    private static final int K_VALUE = 5;

    /**
     * Method utama untuk membuat rekomendasi baru menggunakan siklus CBR.
     */
    public Rekomendasi createRekomendasi(RekomendasiRequest request) {
        // TAHAP 1: RETRIEVE
        List<Rekomendasi> caseBase = rekomendasiRepository.findAll();
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
        return (simTujuan * BOBOT_TUJUAN) + (simBmi * BOBOT_BMI) + (simUsia * BOBOT_USIA)
                + (simFrekuensi * BOBOT_FREKUENSI);
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
        if (request.getUsia() > 40) {
            newRekomendasi.setIntensitas("Ringan ke Sedang");
        }
        if (request.getBmi() > 25) {
            newRekomendasi.setCatatanKhusus("Fokus pada sesi cardio untuk memaksimalkan pembakaran kalori.");
        } else {
            newRekomendasi.setCatatanKhusus("Program disesuaikan dengan tujuan: " + request.getTujuanLatihan());
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
        defaultRekomendasi.setJenisLatihan("Kombinasi Cardio dan Strength");
        defaultRekomendasi.setFrekuensi(request.getFrekuensiLatihan() + " kali/minggu");
        defaultRekomendasi.setDurasi("45 menit/sesi");
        defaultRekomendasi.setIntensitas("Sedang");
        defaultRekomendasi.setDetailProgram(
                "• Cardio: 20 menit (70% MHR)\n• Strength: 25 menit (fokus pada area core dan lower body)");
        defaultRekomendasi
                .setCatatanKhusus("Ini adalah rekomendasi awal. Akurasi akan meningkat seiring bertambahnya data.");
        return defaultRekomendasi;
    }
}