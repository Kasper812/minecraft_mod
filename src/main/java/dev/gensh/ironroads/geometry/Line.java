package dev.gensh.ironroads.geometry;

/** Одна линия сети. Для Axis.Z линия тянется вдоль Z, её изменяемая координата — X. */
public record Line(Line.Axis axis, int index) {
    public enum Axis { X, Z }
}
