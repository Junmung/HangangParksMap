package com.example.junmung.hangangparksmap;


import com.example.junmung.hangangparksmap.ARDrawUtils.ColorShaderProgram;
import com.example.junmung.hangangparksmap.ARDrawUtils.Geometry;
import com.example.junmung.hangangparksmap.ARDrawUtils.ObjectBuilder;
import com.example.junmung.hangangparksmap.ARDrawUtils.ObjectBuilder.GeneratedData;
import com.example.junmung.hangangparksmap.ARDrawUtils.VertexArray;

import java.util.List;

public class Arrow {
    private static final int POSITION_COMPONENT = 3;
    public float radiusCone, heightCone, radiusCylinder, heightCylinder;

    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> drawList;

    public Arrow() {
    }

    public Arrow(float radiusCone, float heightCone, float radiusCylinder, float heightCylinder, int numPoints) {
        GeneratedData generatedData = ObjectBuilder.createArrow(new Geometry.Cylinder(new Geometry.Point(0f, 0f, 0f), radiusCylinder, heightCylinder),
                new Geometry.Cone(new Geometry.Point(0f, -heightCylinder/2f, 0f), radiusCone, heightCone), numPoints);
        this.radiusCylinder = radiusCylinder;
        this.radiusCone = radiusCone;
        this.heightCone = heightCone;
        this.heightCylinder = heightCylinder;
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0, colorProgram.getPositionAttributeLocation(), POSITION_COMPONENT,
                4*(POSITION_COMPONENT+1));
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT, colorProgram.getColorAttributeLocation(), 1,
                4*(POSITION_COMPONENT+1));
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand i: drawList) {
            i.draw();
        }
    }

}