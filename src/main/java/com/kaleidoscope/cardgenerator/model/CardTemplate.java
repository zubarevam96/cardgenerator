package com.kaleidoscope.cardgenerator.model;

import jakarta.persistence.*;


@Entity
@Table(name="card_templates")
public class CardTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String cardStructure;

    private String cardValue;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCardStructure() {
        return cardStructure;
    }

    public void setCardStructure(String cardStructure) {
        this.cardStructure = cardStructure;
    }

    public String getCardValue() {
        return cardValue;
    }

    public void setCardValue(String cardValue) {
        this.cardValue = cardValue;
    }
}
