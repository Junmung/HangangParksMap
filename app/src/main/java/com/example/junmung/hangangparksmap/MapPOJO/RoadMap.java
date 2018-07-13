package com.example.junmung.hangangparksmap.MapPOJO;

import com.github.filosganga.geogson.model.Feature;

import java.util.List;

public class RoadMap {
    private int index = 0;


    private List<Feature> features = null;


//    private List<>

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public void nextPoint(){
        index++;
    }

    public int getIndex(){
        return index;
    }

}
