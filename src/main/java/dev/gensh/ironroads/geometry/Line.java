package dev.gensh.ironroads.geometry;

public record Line(Line.Axis axis, int index, int offset, int anchorAlong) {
    public enum Axis { X, Z }

    public Line(Axis axis, int index) {
        this(axis, index, 0, 0);
    }

    public boolean isBranch() {
        return offset != 0;
    }

    public Axis crossAxis() {
        return axis == Axis.Z ? Axis.X : Axis.Z;
    }
}
