import java.awt.*;

class Sphere {
    Vect3D center;
    Color color;
    double radius;
    int specular;

    public Sphere(Vect3D center, double radius, Color color, int specular) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.specular = specular;
    }
}
