package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.service.data.ReinitialisationService;

@Controller
public class ReinitialisationController {

    @Autowired
    private ReinitialisationService reinitialisationService;

    @PostMapping("/reinitialisation")
    public String resetData() {
        reinitialisationService.resetDatabase();
        return "redirect:/";
    }
}
