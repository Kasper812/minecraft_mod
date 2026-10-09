package com.atsuishio.portlib.gui;

import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public final class LegacyPose {
    private final Matrix3x2fStack stack;

    public LegacyPose(Matrix3x2fStack stack) {
        this.stack = stack;
    }

    public Matrix3x2fStack stack() {
        return stack;
    }

    public void pushPose() {
        stack.pushMatrix();
    }

    public void popPose() {
        stack.popMatrix();
    }

    public void translate(double x, double y, double z) {
        stack.translate((float) x, (float) y);
    }

    public void translate(float x, float y, float z) {
        stack.translate(x, y);
    }

    public void scale(float x, float y, float z) {
        stack.scale(x, y);
    }

    public void mulPose(Quaternionfc rotation) {
        float angle = (float) Math.atan2(2.0 * (rotation.w() * rotation.z() + rotation.x() * rotation.y()),
                1.0 - 2.0 * (rotation.y() * rotation.y() + rotation.z() * rotation.z()));
        stack.rotate(angle);
    }

    public void rotateAround(Quaternionfc rotation, float x, float y, float z) {
        stack.translate(x, y);
        mulPose(rotation);
        stack.translate(-x, -y);
    }

    public void mulPose(Matrix4f matrix) {
        Matrix3x2f m = new Matrix3x2f(matrix.m00(), matrix.m01(), matrix.m10(), matrix.m11(), matrix.m30(), matrix.m31());
        stack.mul(m);
    }

    public void setIdentity() {
        stack.identity();
    }

    public Pose last() {
        return new Pose(stack);
    }

    public static final class Pose {
        private final Matrix4f pose;
        private final Matrix3f normal = new Matrix3f();

        Pose(Matrix3x2f m) {
            this.pose = new Matrix4f(
                    m.m00(), m.m01(), 0.0F, 0.0F,
                    m.m10(), m.m11(), 0.0F, 0.0F,
                    0.0F, 0.0F, 1.0F, 0.0F,
                    m.m20(), m.m21(), 0.0F, 1.0F);
        }

        public Matrix4f pose() {
            return pose;
        }

        public Matrix3f normal() {
            return normal;
        }
    }
}
