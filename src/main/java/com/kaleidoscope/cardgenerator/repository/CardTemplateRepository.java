package com.kaleidoscope.cardgenerator.repository;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.model.CardTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardTemplateRepository extends JpaRepository<CardTemplate, Long> {
    List<CardTemplate> findByUser(User user);
}
