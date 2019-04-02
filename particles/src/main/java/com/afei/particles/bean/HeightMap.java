package com.afei.particles.bean;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.afei.particles.program.HeightMapProgram;
import com.afei.particles.program.SkyBoxShaderProgram;
import com.afei.particles.util.Geometry;

import java.nio.ByteBuffer;
import java.util.logging.Handler;

public class HeightMap {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * 4; // float

    private final int WIDTH;
    private final int HEIGHT;
    private final int ELEMENTS_NUM;

    private final VertexBuffer VERTEX_BUFFER;
    private final IndexBuffer INDEX_BUFFER;

    public HeightMap(Bitmap bitmap) {
        WIDTH = bitmap.getWidth();
        HEIGHT = bitmap.getHeight();
        if (WIDTH * HEIGHT > 65536) {
            throw new RuntimeException("Heightmap is too large for the index buffer.");
        }
        ELEMENTS_NUM = calculateElementNum();
        VERTEX_BUFFER = new VertexBuffer(loadBitmapData(bitmap));
        INDEX_BUFFER = new IndexBuffer(createIndexData());
    }

    private int calculateElementNum() {
        return (WIDTH - 1) * (HEIGHT - 1) * 2 * 3;
    }

    private float[] loadBitmapData(Bitmap bitmap) {
        final int[] pixels = new int[WIDTH * HEIGHT];
        bitmap.getPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        bitmap.recycle();

        final float[] vertices = new float[WIDTH * HEIGHT * TOTAL_COMPONENT_COUNT];
        int offset = 0;

        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                final Geometry.Point point = getPoint(pixels, row, col);
                vertices[offset++] = point.x;
                vertices[offset++] = point.y;
                vertices[offset++] = point.z;

                final Geometry.Point left = getPoint(pixels, row, col - 1);
                final Geometry.Point top = getPoint(pixels, row - 1, col);
                final Geometry.Point right = getPoint(pixels, row, col + 1);
                final Geometry.Point bottom = getPoint(pixels, row + 1, col);
                final Geometry.Vector rightToLeft = Geometry.vectorBetween(right, left);
                final Geometry.Vector topToBottom = Geometry.vectorBetween(top, bottom);
                final Geometry.Vector normal = rightToLeft.crossProduct(topToBottom).normalize();
                vertices[offset++] = normal.x;
                vertices[offset++] = normal.y;
                vertices[offset++] = normal.z;
            }
        }
        return vertices;
    }

    private short[] createIndexData() {
        final short[] indexData = new short[ELEMENTS_NUM];
        int offset = 0;

        for (int row = 0; row < HEIGHT - 1; row++) {
            for (int col = 0; col < WIDTH - 1; col++) {
                short topLeft = (short) (row * WIDTH + col);
                short topRight = (short) (row * WIDTH + col + 1);
                short bottomLeft = (short) ((row + 1) * WIDTH + col);
                short bottomRight = (short) ((row + 1) * WIDTH + col + 1);

                // Write out two triangles.
                indexData[offset++] = topLeft;
                indexData[offset++] = bottomLeft;
                indexData[offset++] = topRight;

                indexData[offset++] = topRight;
                indexData[offset++] = bottomLeft;
                indexData[offset++] = bottomRight;
            }
        }
        return indexData;
    }

    public void bindData(HeightMapProgram program) {
        VERTEX_BUFFER.setVertexAttribPointer(0, program.getPositionLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        VERTEX_BUFFER.setVertexAttribPointer(POSITION_COMPONENT_COUNT * 4, program.getNormalLocation(), NORMAL_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, INDEX_BUFFER.getBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, ELEMENTS_NUM, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private Geometry.Point getPoint(int[] pixels, int row, int col) {
        float x = (float) col / (float) (WIDTH - 1) - 0.5f;
        float z = (float) row / (float) (HEIGHT - 1) - 0.5f;
        row = clamp(row, 0, WIDTH - 1);
        col = clamp(col, 0, HEIGHT - 1);
        float y = (float) Color.red(pixels[(row * HEIGHT + col)]) / (float) 255;
        return new Geometry.Point(x, y, z);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(Math.min(val, max), min);
    }

}
