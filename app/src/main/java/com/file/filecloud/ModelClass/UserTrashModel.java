package com.file.filecloud.ModelClass;

public class UserTrashModel {

    String fileName, type, timestamp, uid, deleteTimestamp,fileUri,cutoffTimer, deleteTypeReceived;
    Boolean expanded = false;

    public UserTrashModel() {
    }

    public UserTrashModel(String fileName, String type, String timestamp, String uid, String deleteTimestamp, String fileUri, String cutoffTimer, String deleteTypeReceived, Boolean expanded) {
        this.fileName = fileName;
        this.type = type;
        this.timestamp = timestamp;
        this.uid = uid;
        this.deleteTimestamp = deleteTimestamp;
        this.fileUri = fileUri;
        this.cutoffTimer = cutoffTimer;
        this.deleteTypeReceived = deleteTypeReceived;
        this.expanded = expanded;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeleteTimestamp() {
        return deleteTimestamp;
    }

    public void setDeleteTimestamp(String deleteTimestamp) {
        this.deleteTimestamp = deleteTimestamp;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getCutoffTimer() {
        return cutoffTimer;
    }

    public void setCutoffTimer(String cutoffTimer) {
        this.cutoffTimer = cutoffTimer;
    }

    public String getDeleteTypeReceived() {
        return deleteTypeReceived;
    }

    public void setDeleteTypeReceived(String deleteTypeReceived) {
        this.deleteTypeReceived = deleteTypeReceived;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }
}