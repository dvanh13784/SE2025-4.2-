package com.example.ardemoapp;
public class ModelItem {
    public final String name;
    public final String url;

    public ModelItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        // để Spinner hiển thị name
        return name;
    }
}
