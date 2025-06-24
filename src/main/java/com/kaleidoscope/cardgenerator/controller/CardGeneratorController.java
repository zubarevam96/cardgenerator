package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.model.CardTemplate;
import com.kaleidoscope.cardgenerator.service.CardTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/card-templates")
public class CardGeneratorController {

    @Autowired
    private CardTemplateService cardTemplateService;

    @Autowired
    UserDetailsService userDetailsService;

    @GetMapping
    public String basePage(Model model) {
        model.addAttribute("templates", getCardTemplates());

        return "card-templates";
    }

    @GetMapping("/cards")
    @ResponseBody
    public List<CardTemplate> getCardTemplates() {
        return cardTemplateService.getAllForCurrentUser();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCardTemplate(@PathVariable Long id) {
        boolean deleted = cardTemplateService.deleteTemplateById(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/")
    @ResponseBody
    public CardTemplate createCardTemplate(@RequestBody CardTemplate cardTemplate) {

        return cardTemplateService.saveOrUpdate(cardTemplate);
    }
}
