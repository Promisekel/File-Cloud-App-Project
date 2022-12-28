package com.file.filecloud.ModelClass;

public class ModelPeers {
    String  uid, firstName,surName, fullName,image;

    public ModelPeers() {
    }

    public ModelPeers(String uid, String firstName, String surName, String fullName, String image) {
        this.uid = uid;
        this.firstName = firstName;
        this.surName = surName;
        this.fullName = fullName;
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
