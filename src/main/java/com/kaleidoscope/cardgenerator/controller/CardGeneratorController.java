package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.model.CardTemplate;
import com.kaleidoscope.cardgenerator.service.CardTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/generating")
public class CardGeneratorController {

    @Autowired
    private CardTemplateService cardTemplateService;

    @GetMapping("/cards")
    public List<CardTemplate> getCardTemplates() {
        return cardTemplateService.getCardTemplatesForUser();
    }

    @PostMapping("/cards")
    public CardTemplate createCardTemplate(@RequestBody CardTemplate cardTemplate) {

        return cardTemplateService.save(cardTemplate);
    }

}
