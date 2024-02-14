package ru.shaderey.lab15;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;

public class Task {

    public Task() {

    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DocumentId
    public String getId() {
        return id;
    }

    @DocumentId
    public void setId(String id) {
        this.id = id;
    }

    private String title;
    private String description;
    @DocumentId
    private String id;

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
