package com.palep.backend.service;

import com.palep.backend.dto.RekomendasiRequest;
import com.palep.backend.dto.RekomendasiResponse;
import com.palep.backend.model.RekomendasiHistory;
import com.palep.backend.repository.RekomendasiHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RekomendasiService {

    private final RekomendasiHistoryRepository historyRepository;

    @Autowired
    public RekomendasiService(RekomendasiHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    private static final Map<String, List<String>> exercises = Map.of(
            "push", List.of("Bench Press", "Overhead Press", "Incline Press", "Push-ups", "Dips"),
            "pull", List.of("Pull-ups / Lat Pulldown", "Bent Over Rows", "Seated Cable Rows", "Face Pulls", "Bicep Curls"),
            "legs", List.of("Squats", "Romanian Deadlifts", "Leg Press", "Lunges", "Leg Curls", "Calf Raises"),
            "full_body", List.of("Squats", "Bench Press", "Rows", "Overhead Press", "Romanian Deadlift"),
            "cardio_hiit", List.of("Burpees", "High Knees", "Jumping Jacks", "Mountain Climbers", "Kettlebell Swings"),
            "cardio_low_impact", List.of("Brisk Walking (inclined)", "Cycling", "Elliptical Trainer", "Swimming"),
            "glute_focus", List.of("Hip Thrusts", "Glute Bridges", "Cable Kickbacks")
    );

    public RekomendasiResponse getRecommendation(RekomendasiRequest request) {
        double bmi = calculateBmi(request.getBeratBadan(), request.getTinggiBadan());
        RekomendasiResponse recommendationResult = buildRecommendation(request, bmi);

        RekomendasiHistory historyToSave = mapToEntity(request, recommendationResult, bmi);

        RekomendasiHistory savedEntity = historyRepository.save(historyToSave);

        return mapToResponse(savedEntity);
    }

    private RekomendasiResponse buildRecommendation(RekomendasiRequest request, double bmi) {
        String bmiCategory = getBmiCategory(bmi);
        String ageCategory = getAgeCategory(request.getUsia());
        String jenisLatihan = "";
        String durasi = "";
        String intensitas = "";
        String detailProgram = "";
        List<String> catatanKhususList = new ArrayList<>();

        switch (request.getTujuanLatihan()) {
            case "Penurunan Berat Badan":
                jenisLatihan = "Kombinasi Kardio & Latihan Beban";
                durasi = "50-60 menit/sesi";
                if ("Obese".equals(bmiCategory) || "Senior".equals(ageCategory)) {
                    intensitas = "Rendah hingga Sedang";
                    detailProgram = generateProgram("low_impact", request);
                    catatanKhususList.add("Fokus pada konsistensi dan hindari latihan berdampak tinggi untuk melindungi persendian. Defisit kalori dari pola makan sangat penting.");
                } else {
                    intensitas = "Sedang hingga Tinggi";
                    detailProgram = generateProgram("hiit", request);
                    catatanKhususList.add("HIIT sangat efektif, tapi pastikan pemulihan cukup. Pola makan defisit kalori akan mempercepat hasil.");
                }
                break;

            case "Pembentukan Otot":
                jenisLatihan = "Fokus Utama: Latihan Beban Progresif";
                durasi = "60-75 menit/sesi";
                if ("Senior".equals(ageCategory)) {
                    intensitas = "Sedang (Fokus pada Teknik)";
                    detailProgram = generateProgram("full_body_senior", request);
                    catatanKhususList.add("Gunakan beban yang terkontrol. Prioritaskan pemulihan dan nutrisi, terutama protein dan kalsium.");
                } else {
                    intensitas = "Tinggi (Beban Menantang)";
                    if (request.getFrekuensiLatihan() <= 3) {
                        detailProgram = generateProgram("full_body_strength", request);
                    } else if (request.getFrekuensiLatihan() == 4) {
                        detailProgram = generateProgram("upper_lower", request);
                    } else {
                        detailProgram = generateProgram("ppl", request);
                    }
                    catatanKhususList.add("Pastikan Anda dalam kondisi surplus kalori dan asupan protein tinggi (1.6-2.2g per kg berat badan).");
                }
                if ("Underweight".equals(bmiCategory)) {
                    catatanKhususList.add("Surplus kalori adalah WAJIB. Jangan takut untuk makan lebih banyak makanan berkualitas.");
                }
                break;

            case "Peningkatan Stamina":
                jenisLatihan = "Latihan Kardiovaskular Terstruktur";
                durasi = "40-60 menit/sesi";
                if ("Senior".equals(ageCategory) || "Obese".equals(bmiCategory)) {
                    intensitas = "Rendah hingga Sedang (Fokus Durasi)";
                    detailProgram = generateProgram("stamina_low_impact", request);
                    catatanKhususList.add("Konsistensi lebih penting daripada intensitas. Bangun fondasi aerobik secara perlahan.");
                } else {
                    intensitas = "Bervariasi (HIIT & LISS)";
                    detailProgram = generateProgram("stamina_hiit_liss", request);
                    catatanKhususList.add("Variasi intensitas adalah kunci untuk meningkatkan VO2 max dan daya tahan.");
                }
                break;
        }

        if ("Senior".equals(ageCategory)) {
            catatanKhususList.add("Selalu lakukan pemanasan dinamis 10-15 menit sebelum latihan dan pendinginan statis setelahnya.");
        }

        // --- LOGIKA SPESIFIK GENDER ---
        if ("Wanita".equals(request.getJenisKelamin())) {
            if ("Pembentukan Otot".equals(request.getTujuanLatihan())) {
                catatanKhususList.add("Latihan beban tidak akan membuat Anda 'terlalu besar', justru akan membentuk tubuh dan meningkatkan metabolisme. Pertimbangkan untuk menambah fokus pada latihan glutes (seperti Hip Thrusts) untuk hasil optimal.");
            }
            if("Penurunan Berat Badan".equals(request.getTujuanLatihan())){
                catatanKhususList.add("Bagi wanita, latihan beban sangat penting saat defisit kalori untuk menjaga kepadatan tulang dan massa otot.");
            }
        }

        return new RekomendasiResponse(
                null, null, request.getNama(), jenisLatihan,
                request.getFrekuensiLatihan() + " kali/minggu", durasi, intensitas, detailProgram,
                "- " + String.join("\n- ", catatanKhususList)
        );
    }

    private RekomendasiHistory mapToEntity(RekomendasiRequest request, RekomendasiResponse result, double bmi) {
        RekomendasiHistory entity = new RekomendasiHistory();
        entity.setNama(request.getNama());
        entity.setUsia(request.getUsia());
        entity.setJenisKelamin(request.getJenisKelamin());
        entity.setBeratBadan(request.getBeratBadan());
        entity.setTinggiBadan(request.getTinggiBadan());
        entity.setBmi(bmi);
        entity.setTujuanLatihan(request.getTujuanLatihan());
        entity.setFrekuensiLatihan(request.getFrekuensiLatihan());
        entity.setJenisLatihanRec(result.getJenisLatihan());
        entity.setFrekuensi(result.getFrekuensi());
        entity.setDurasi(result.getDurasi());
        entity.setIntensitas(result.getIntensitas());
        entity.setDetailProgram(result.getDetailProgram());
        entity.setCatatanKhusus(result.getCatatanKhusus());
        return entity;
    }

    private RekomendasiResponse mapToResponse(RekomendasiHistory entity) {
        return new RekomendasiResponse(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getNama(),
                entity.getJenisLatihanRec(),
                entity.getFrekuensi(),
                entity.getDurasi(),
                entity.getIntensitas(),
                entity.getDetailProgram(),
                entity.getCatatanKhusus()
        );
    }

    private double calculateBmi(double weight, double height) { return weight / Math.pow(height / 100, 2); }
    private String getBmiCategory(double bmi) { if (bmi < 18.5) return "Underweight"; if (bmi >= 25 && bmi < 30) return "Overweight"; if (bmi >= 30) return "Obese"; return "Normal"; }
    private String getAgeCategory(int age) { if (age < 30) return "Young Adult"; if (age >= 50) return "Senior"; return "Adult"; }
    private String generateProgram(String type, RekomendasiRequest request) {
        String program = "";
        switch (type) {
            case "low_impact":
                program = String.format("• Kardio: 3-4 sesi/minggu.\n  - Pilih 2 dari: %s.\n  - Lakukan selama 30-40 menit.\n\n• Latihan Beban: 2 sesi/minggu.\n  - Pilih 3 dari: %s (3 set x 10-15 repetisi).",
                        String.join(", ", exercises.get("cardio_low_impact")),
                        String.join(", ", exercises.get("full_body")));
                break;
            case "hiit":
                program = String.format("• HIIT: 2 sesi/minggu.\n  - Pilih 3 dari: %s.\n  - 30 detik ON, 30 detik OFF selama 15-20 menit.\n\n• Latihan Beban: 2-3 sesi/minggu.\n  - Pilih 4 dari: %s (3 set x 8-12 repetisi).",
                        String.join(", ", exercises.get("cardio_hiit")),
                        String.join(", ", exercises.get("full_body")));
                break;
            case "full_body_strength":
                program = String.format("Program Full Body 3x seminggu.\n\n• Hari A: %s (3x6-8), %s (3x6-8), %s (3x8-10).\n\n• Hari B: %s (3x6-8), %s (3x6-8), %s (3x8-10).",
                        exercises.get("legs").get(0), exercises.get("push").get(0), exercises.get("pull").get(1),
                        exercises.get("legs").get(1), exercises.get("push").get(1), exercises.get("pull").get(0));
                break;
            case "upper_lower":
                program = String.format("Program Upper/Lower Split 4x seminggu.\n\n• Hari Upper: %s, %s, %s.\n\n• Hari Lower: %s, %s, %s.",
                        exercises.get("push").get(0), exercises.get("pull").get(1), exercises.get("push").get(1),
                        exercises.get("legs").get(0), exercises.get("legs").get(2), exercises.get("legs").get(1));
                break;
            case "ppl":
                program = "Program Push/Pull/Legs (PPL) Split.\n\n• Hari Push: Latihan dada, bahu, trisep.\n\n• Hari Pull: Latihan punggung, bisep.\n\n• Hari Legs: Latihan seluruh kaki.";
                break;
            case "full_body_senior":
                program = String.format("Program Full Body 3x seminggu.\n\n• Latihan Contoh: %s (3x10-12), %s (3x10-12), %s (3x10-12).",
                        exercises.get("legs").get(0), exercises.get("push").get(0), exercises.get("pull").get(1));
                break;
            case "stamina_low_impact":
                program = String.format("Bangun fondasi aerobik secara bertahap.\n\n• Sesi Interval Ringan: 2-3x seminggu.\n  - %s atau %s.\n  - 3 menit tempo sedang, 2 menit tempo santai. Ulangi selama 30-40 menit.",
                        exercises.get("cardio_low_impact").get(1), exercises.get("cardio_low_impact").get(2));
                break;
            case "stamina_hiit_liss":
                program = String.format("Kombinasi ini akan membangun kecepatan dan daya tahan.\n\n• Sesi HIIT: 1-2x seminggu.\n  - %s atau %s.\n  - 40 detik ON, 20 detik OFF. Lakukan selama 20 menit.\n\n• Sesi LISS: 2-3x seminggu.\n  - Lari atau bersepeda dengan kecepatan stabil selama 45-60 menit.",
                        exercises.get("cardio_hiit").get(0), exercises.get("cardio_hiit").get(4));
                break;
            default:
                program = "Program spesifik tidak ditemukan, mohon konsultasikan dengan pelatih.";
                break;
        }

        // --- LOGIKA TAMBAHAN UNTUK WANITA ---
        if ("Wanita".equals(request.getJenisKelamin()) && "Pembentukan Otot".equals(request.getTujuanLatihan())) {
            program += String.format("\n\n• Fokus Tambahan (opsional): Tambahkan 1-2 latihan dari: %s di akhir sesi latihan kaki.",
                    String.join(", ", exercises.get("glute_focus")));
        }
        return program;
    }
}
