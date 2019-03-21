package com.th_koeln.steve.klamottenverteiler.structures;

/**
 * Created by Frank on 15.01.2018.
 */

public class Request {

    private String name;
    private String size;
    private String brand;
    private String from;
    private String status;
    private String ouId;
    private String confirmed;
    private String closed;
    private String finished;
    private String title;
    private String art;


    public Request(String name, String size, String status, String from, String ouId, String confirmed, String closed, String finished, String title, String art) {
        this.name = name;
        this.size = size;
        this.status = status;
        this.from = from;
        this.ouId = ouId;
        this.confirmed = confirmed;
        this.closed = closed;
        this.finished = finished;
        this.title = title;
        this.art = art;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFinished() {
        return finished;
    }

    public void setFinished(String finished) {
        this.finished = finished;
    }

    public String getClosed() {
        return closed;
    }

    public void setClosed(String closed) {
        this.closed = closed;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getOuId() {
        return ouId;
    }

    public void setOuId(String ouId) {
        this.ouId = ouId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
