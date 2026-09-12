package com.example.api;

import com.example.database.DatabaseModule;

public final class ApiApplication {
    private ApiApplication() {
    }

    public static void main(String[] args) {
        System.out.println("API module started: " + DatabaseModule.status());
    }
}
