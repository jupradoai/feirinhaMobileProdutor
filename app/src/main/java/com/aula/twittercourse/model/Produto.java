package com.aula.twittercourse.model;

public class Produto {
    private String id; // Adicione um ID para o produto
    private String name;
    private String description; // Adicione a descrição
    private double price; // Adicione o preço
    private int quantity;

    // Construtor atualizado
    public Produto(String id, String name, String description, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Métodos getter
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
