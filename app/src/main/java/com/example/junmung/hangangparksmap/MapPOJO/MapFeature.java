package com.example.junmung.hangangparksmap.MapPOJO;

import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.Geometry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

public class MapFeature extends Feature {


    public MapFeature(Geometry<?> geometry, ImmutableMap<String, JsonElement> properties, Optional<String> id) {
        super(geometry, properties, id);
    }



    public boolean isEndPoint(){
        if(properties().containsValue("EP"))
            return true;
        else
            return false;
    }
}
