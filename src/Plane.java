import java.awt.*;

class Plane {
    Vect3D point;
    Vect3D n;
    Color color;

    public Plane(Vect3D point, Vect3D n, Color color) {
        this.point = point;
        this.n = n;
        this.color = color;
    }
}
