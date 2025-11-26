package com.example.riotshop.managers;

import com.example.riotshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private List<Product> cartItems = new ArrayList<>();

    private CartManager() {
        // Private constructor for Singleton
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addProduct(Product product) {
        cartItems.add(product);
    }

    public void removeProduct(Product product) {
        cartItems.remove(product);
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public int getCartItemCount() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
    }
}
