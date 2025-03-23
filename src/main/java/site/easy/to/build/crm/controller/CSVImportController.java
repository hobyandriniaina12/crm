package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.service.data.CSVImportService;

import java.util.List;

@Controller
@RequestMapping("/import")
public class CSVImportController {

    @Autowired
    private CSVImportService csvImportService;

    @GetMapping
    public String showImportForm() {
        return "data/formImport";
    }

    @PostMapping
    public ResponseEntity<String> handleCSVUpload(
            @RequestParam("importType") String importType,
            @RequestParam(value = "singleCsv", required = false) MultipartFile singleCsv,
            @RequestParam(value = "csvFiles", required = false) List<MultipartFile> csvFiles) {

        try {
            if ("single".equals(importType) && singleCsv != null && !singleCsv.isEmpty()) {
                csvImportService.importCSV(singleCsv);  // Correction ici
                return ResponseEntity.ok("Importation réussie avec le fichier global.");
            } else if ("multiple".equals(importType) && csvFiles != null && !csvFiles.isEmpty()) {
                for (MultipartFile file : csvFiles) {
                    csvImportService.importCSV(file);  // Correction ici
                }
                return ResponseEntity.ok("Importation réussie avec plusieurs fichiers.");
            } else {
                return ResponseEntity.badRequest().body("Aucun fichier sélectionné.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de l'importation : " + e.getMessage());
        }
    }
}
