package com.file.filecloud.ModelClass;

public class Files_Model {

    String fileName, timestamp, type, uid, fileUri;


    public Files_Model() {
    }

    public Files_Model(String fileName, String timestamp, String type, String uid, String fileUri) {
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.type = type;
        this.uid = uid;
        this.fileUri = fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
}
