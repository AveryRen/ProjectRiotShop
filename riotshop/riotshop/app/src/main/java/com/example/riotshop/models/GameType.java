package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class GameType {
    @SerializedName("gameId")
    private int gameId;
    
    @SerializedName("name")
    private String name;

    public int getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }
}

