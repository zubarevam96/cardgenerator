package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.model.CardTemplate;
import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.repository.CardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardTemplateService {

    @Autowired
    private CardTemplateRepository cardTemplateRepository;

    @Autowired
    private AppUserDetailsService userService;

    public List<CardTemplate> getByUsername(String username) {
        User user = userService.getByUsername(username);

        return getByUser(user);
    }

    public List<CardTemplate> getByUser(User user) {
        return cardTemplateRepository.findByUser(user);
    }

    public CardTemplate save(CardTemplate cardTemplate) {
        cardTemplate.setUser(getUser());
        return cardTemplateRepository.save(cardTemplate);
    }

    public List<CardTemplate> getCardTemplatesForUser() {
        return cardTemplateRepository.findByUser(getUser());
    }

    private User getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }
}
