package com.th_koeln.steve.klamottenverteiler.structures;

/**
 * Created by Michael on 20.01.2018.
 */

public class ClothingOffer {

    String id, uId, art, size, style, gender,
            color, fabric, title, brand, imagePath;
    double distance;
    //Speziell fuer ShowRequest.class Adapter
    String status, from, finished, closed, confirmed;

    public ClothingOffer(String id, String uId, String art, String size,
                         String style, String gender, String color, String fabric,
                         String title, String brand, String imagePath, double distance) {
        this.id = id;
        this.uId = uId;
        this.art = art;
        this.size = size;
        this.style = style;
        this.gender = gender;
        this.color = color;
        this.fabric = fabric;
        this.title = title;
        this.brand = brand;
        this.imagePath = imagePath;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFabric() {
        return fabric;
    }

    public void setFabric(String fabric) {
        this.fabric = fabric;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getDistance() {
        return distance;
    }

    //Spezifische Methoden fuer ShowRequests.class

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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
}