package com.file.filecloud.ModelClass;

public class ModelReceivedFiles {
    String sender, fileName, fileUri, timestamp, type;

    public ModelReceivedFiles() {
    }

    public ModelReceivedFiles(String sender, String fileName, String fileUri, String timestamp, String type) {
        this.sender = sender;
        this.fileName = fileName;
        this.fileUri = fileUri;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
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
}
