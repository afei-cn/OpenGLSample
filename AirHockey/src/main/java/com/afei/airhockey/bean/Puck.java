package com.afei.airhockey.bean;

import com.afei.airhockey.program.ColorShaderProgram;
import com.afei.airhockey.util.Geometry;

import java.util.List;

public class Puck {

    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius;
    public final float height;

    private final VertexArray mVertexArray;
    private final List<ObjectBuilder.DrawCommand> mDrawList;

    public Puck(float radius, float height, int numPoints) {
        this.radius = radius;
        this.height = height;

        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createPuck(new Geometry.Cylinder(new Geometry.Point
                (0f, 0f, 0f), radius, height), numPoints);
        mVertexArray = new VertexArray(generatedData.vertexData);
        mDrawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
        mVertexArray.setVertexAttribPointer(0, colorShaderProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand command : mDrawList) {
            command.draw();
        }
    }
}
