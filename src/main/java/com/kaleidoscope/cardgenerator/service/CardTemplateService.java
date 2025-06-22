package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.model.CardTemplate;
import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.repository.CardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardTemplateService {

    @Autowired
    private CardTemplateRepository cardTemplateRepository;

    @Autowired
    private UserService userService;

    public List<CardTemplate> getByUsername(String username) {
        User user = userService.getByUsername(username);

        return getByUser(user);
    }

    public List<CardTemplate> getByUser(User user) {
        return cardTemplateRepository.findByUser(user);
    }

    public CardTemplate saveOrUpdate(CardTemplate cardTemplate) {
        User user = userService.getCurrentUser();
        cardTemplate.setUser(user);
        Optional<CardTemplate> existingTemplate = cardTemplateRepository.findByNameAndUser(cardTemplate.getName(), user);

        // noinspection OptionalIsPresent
        if (existingTemplate.isPresent())
            cardTemplate.setId(existingTemplate.get().getId());

        return cardTemplateRepository.save(cardTemplate);
    }

    public List<CardTemplate> getAllForCurrentUser() {
        return cardTemplateRepository.findByUser(userService.getCurrentUser());
    }

    public boolean deleteTemplateById(Long id) {
        Optional<CardTemplate> templateToDelete = cardTemplateRepository.findById(id);
        if (templateToDelete.isEmpty())
            return false;

        User user = userService.getCurrentUser();
        if (!user.equals(templateToDelete.get().getUser()))
            throw new AuthorizationDeniedException("Not enough rights to delete this template");

        cardTemplateRepository.deleteById(id);
        return true;
    }
}
