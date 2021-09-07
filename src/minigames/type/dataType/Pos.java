package minigames.type.dataType;

import arc.func.Prov;
import arc.math.geom.Position;

public class Pos implements Position {
    private final Prov<Float> getX, getY;

    public Pos(Prov<Float> x, Prov<Float> y) {
        getX = x;
        getY = y;
    }

    public Pos(Prov<Float> x, float y) {
        this(x, () -> y);
    }

    public Pos(float x, Prov<Float> y) {
        this(() -> x, y);
    }

    public Pos(float x, float y) {
        this(() -> x, () -> y);
    }

    @Override
    public float getX() {
        return getX.get();
    }

    @Override
    public float getY() {
        return getY.get();
    }
}
