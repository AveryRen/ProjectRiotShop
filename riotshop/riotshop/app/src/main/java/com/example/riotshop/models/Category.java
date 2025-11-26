package com.example.riotshop.models;

public class Category {
    private String id;
    private String name;
    private int iconResId; // Resource ID cho Icon (int)

    // Constructor
    public Category(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
    }

    // Constructor rỗng (cần cho Firebase/Database)
    public Category() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}