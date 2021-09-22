package com.example.bookapp.models;

public class ModelCatagory {
    long timestamp;
    //make sure to use same variables as firebase
    String id, catagory, uid;

    public ModelCatagory() {

    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ModelCatagory(String id, String catagory, String uid, long timestamp) {
        this.id = id;
        this.catagory = catagory;
        this.uid = uid;
        this.timestamp = timestamp;
    }
    //getter setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCatagory() {
        return catagory;
    }

    public void setCatagory(String catagory) {
        this.catagory = catagory;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
