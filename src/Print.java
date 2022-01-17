import javax.swing.*;
import java.awt.*;
import java.util.Objects;

class Print extends JComponent {
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        Vect3D O = new Vect3D(0, 0, 0); //положение камеры
        double projection_plane_d = 1;
        double radians = Math.toRadians(0);
        double Mupdown[][] = {{1, 0, 0}, {0, Math.cos(radians), -Math.sin(radians)}, {0, Math.sin(radians), Math.cos(radians)}};
        double Mrightleft[][] = {{Math.cos(radians), 0, Math.sin(radians)}, {0, 1, 0}, {-Math.sin(radians), 0, Math.cos(radians)}};
        Sphere[] scene = {
                new Sphere(new Vect3D(0, -1, 5), 1, new Color(45, 200, 0), 250),
                new Sphere(new Vect3D(1, 0, 6), 1, new Color(70, 100, 150), 1000),
                new Sphere(new Vect3D(-2, 0, 4), 1, new Color(125, 95, 43), 10),
                new Sphere(new Vect3D(0, -5001, 0), 5000, new Color(255, 255, 0), 10),
                new Sphere(new Vect3D(0, 0, 6000), 5000, new Color(0, 0, 255), 10)
        };
        Light[] lights = {
                new Light("ambient", 0.2f, null, null),
                new Light("point", 0.6f, new Vect3D(2, 4, 0), null),
                new Light("directional", 0.2f, null, new Vect3D(1, 4, 4))
        };
        for (int S_x = 0; S_x <= Main.C_W; S_x++) {
            int x = S_x - Main.C_W / 2;
            for (int S_y = 0; S_y <= Main.C_H; S_y++) {
                int y = Main.C_H / 2 - S_y;
                Vect3D D = CanvasToViewport(x, y, projection_plane_d);
                Color color = TraceRay(O, D, 1, Double.POSITIVE_INFINITY, scene, lights);
                graphics2D.setPaint(color);
                graphics2D.fillRect(S_x, S_y, 1, 1);
            }
        }
    }

    private Vect3D CanvasToViewport(double x, double y, double d) { //d-расстояние до плоскости проекции
        return new Vect3D(x * Main.V_W / Main.C_W, y * Main.V_H / Main.C_H, d);
    }

    //Метод TraceRay вычисляет пересечение луча с каждой сферой, и возвращает цвет сферы в ближайшей точке пересечения,
    //которая находится в требуемом интервале t:
    private Color TraceRay(Vect3D O, Vect3D D, double tMin, double tMax, Sphere[] spheres, Light[] lights) {
        Sphere_t st = ClosestIntersection(O, D, tMin, tMax, spheres);
        Sphere closest_sphere = st.sphere;
        double closest_t = st.t;
        if (closest_sphere == null) {
            return getBackground();
        }
        Vect3D P = new Vect3D(O.x + closest_t * D.x, O.y + closest_t * D.y, O.z + closest_t * D.z); //вычисление пересечения
        //вычисление нормали сферы в точке пересечения
        Vect3D N = new Vect3D(P.x - closest_sphere.center.x, P.y - closest_sphere.center.y, P.z - closest_sphere.center.z);
        Vect3D N_real = new Vect3D(N.x / Vect3D.Length(N), N.y / Vect3D.Length(N), N.z / Vect3D.Length(N));
        int argb = closest_sphere.color.getRGB();
        //int alpha = (argb >> 24) & 0xff;
        int red = (argb >> 16) & 0xff;
        int green = (argb >> 8) & 0xff;
        int blue = (argb) & 0xff;
        float r, g, b;
        r = (float) red / 255;
        g = (float) green / 255;
        b = (float) blue / 255;
        Vect3D _D = new Vect3D(-D.x, -D.y, -D.z);
        float redNum = r * ComputeLighting(P, N_real, _D, closest_sphere.specular, lights, spheres);
        float greenNum = g * ComputeLighting(P, N_real, _D, closest_sphere.specular, lights, spheres);
        float blueNum = b * ComputeLighting(P, N_real, _D, closest_sphere.specular, lights, spheres);
        if (redNum > 1)
            redNum = 1;
        if (greenNum > 1)
            greenNum = 1;
        if (blueNum > 1)
            blueNum = 1;
        return new Color(redNum, greenNum, blueNum);
    }

    //код ближайшего пересечения, для вычисления теней:
    private Sphere_t ClosestIntersection(Vect3D O, Vect3D D, double tMin, double tMax, Sphere[] spheres) {
        double closestT = Double.POSITIVE_INFINITY;
        Sphere closestSphere = null;
        for (Sphere sphere :
                spheres) {
            Vect2D T1T2 = IntersectRaySphere(O, D, sphere);
            if (T1T2.x >= tMin && T1T2.x <= tMax && T1T2.x < closestT) {
                closestT = T1T2.x;
                closestSphere = sphere;
            }
            if (T1T2.y >= tMin && T1T2.y <= tMax && T1T2.y < closestT) {
                closestT = T1T2.y;
                closestSphere = sphere;
            }
        }
        return new Sphere_t(closestSphere, closestT);
    }

    //пересечение луча со сферой (решает квадратное уравнение)
    //Его решение даёт нам значения параметра t, при которых луч пересекается со сферой:
    private Vect2D IntersectRaySphere(Vect3D O, Vect3D D, Sphere sphere) {
        Vect3D C = sphere.center;
        double r = sphere.radius;
        Vect3D oc = new Vect3D(O.x - C.x, O.y - C.y, O.z - C.z);
        double k1, k2, k3, disc, t1, t2;
        k1 = Vect3D.Dot(D, D);
        k2 = 2 * Vect3D.Dot(oc, D);
        k3 = Vect3D.Dot(oc, oc) - r * r;
        disc = k2 * k2 - 4 * k1 * k3;
        if (disc < 0)
            return new Vect2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        t1 = (-k2 + Math.sqrt(disc)) / (2 * k1);
        t2 = (-k2 - Math.sqrt(disc)) / (2 * k1);
        return new Vect2D(t1, t2);
    }

    //Уравнение освещения
    private float ComputeLighting(Vect3D P, Vect3D N, Vect3D V, int s, Light[] lights, Sphere[] spheres) {
        float i = 0;
        Vect3D L;
        double tMax;
        for (Light light : lights) {
            //окружающее
            if (Objects.equals(light.type, "ambient")) {
                i += light.intensity;
            } else {
                //освещенность одной точки
                if (Objects.equals(light.type, "point")) {
                    L = new Vect3D(light.position.x - P.x, light.position.y - P.y, light.position.z - P.z);
                    tMax = 1;
                } else {
                    //направленное освещение
                    L = new Vect3D(light.direction.x, light.direction.y, light.direction.z);
                    tMax = Double.POSITIVE_INFINITY;
                }
                double n_dot_l = Vect3D.Dot(N, L);
                //проверка тени
                Sphere_t st = ClosestIntersection(P, L, 0.001, tMax, spheres);
                if (st.sphere != null)
                    continue;
                //диффузность
                if (n_dot_l > 0)
                    i += light.intensity * n_dot_l / (Vect3D.Length(N) * Vect3D.Length(L));
                //зеркальность, вычисляется значение и прибавляется к общему освещению
                if (s != 1) {
                    Vect3D R = new Vect3D(2 * Vect3D.Dot(N, L) * N.x - L.x,
                            2 * Vect3D.Dot(N, L) * N.y - L.y,
                            2 * Vect3D.Dot(N, L) * N.z - L.z);
                    double r_dot_v = Vect3D.Dot(R, V);
                    if (r_dot_v > 0) {
                        i += light.intensity * Math.pow(r_dot_v / (Vect3D.Length(R) * Vect3D.Length(V)), s);
                    }
                }
            }
        }
        return i;
    }

   /* private Color TraceRay_Plane(Vect3D O, Vect3D D, double tMin, double tMax, Plane[] planes, Light[] lights) {
        double closest_t = Double.POSITIVE_INFINITY;
        Plane closest_plane = null;
        for (Plane plane : planes) {
            double t = IntersectRayPlane(O, D, plane);
            if (t >= tMin && t <= tMax && t < closest_t) {
                closest_t = t;
                closest_plane = plane;
            }
        }
        if (closest_plane == null) {
            return getBackground();
        }
        Vect3D P = new Vect3D(O.x + closest_t * D.x, O.y + closest_t * D.y, O.z + closest_t * D.z); //вычисление пересечения
        //вычисление нормали сферы в точке пересечения
        Vect3D N = new Vect3D(P.x - closest_plane.point.x, P.y - closest_plane.point.y, P.z - closest_plane.point.z);
        Vect3D N_real = new Vect3D(N.x / Vect3D.Length(N), N.y / Vect3D.Length(N), N.z / Vect3D.Length(N));
        int argb = closest_plane.color.getRGB();
        int alpha = (argb >> 24) & 0xff;
        int red = (argb >> 16) & 0xff;
        int green = (argb >> 8) & 0xff;
        int blue = (argb) & 0xff;
        float r, g, b;
        r = (float) red / 255;
        g = (float) green / 255;
        b = (float) blue / 255;
        Vect3D _D = new Vect3D(-D.x, -D.y, -D.z);
        float f_red = r * ComputeLightingPlane(P, N_real, lights);
        float f_green = g * ComputeLightingPlane(P, N_real, lights);
        float f_blue = b * ComputeLightingPlane(P, N_real, lights);
        if (f_red > 1)
            f_red = 1;
        if (f_green > 1)
            f_green = 1;
        if (f_blue > 1)
            f_blue = 1;
        return new Color(f_red, f_green, f_blue);
    }
*/
/*    private double IntersectRayPlane(Vect3D O, Vect3D D, Plane plane) {
        Vect3D nPlane = plane.n;
        double c = Vect3D.Dot(D, nPlane);
        if (c == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double d = Vect3D.Dot(plane.point, nPlane);
        double alpha = (d - Vect3D.Dot(O, nPlane)) / c;
        if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
        }
        return alpha;
    }*/

/*    private float ComputeLightingPlane(Vect3D P, Vect3D N, Light[] lights) {
        float i = 0;
        Vect3D L;
        for (Light light : lights) {
            if (Objects.equals(light.type, "ambient")) {
                i += light.intensity;
            } else {
                if (Objects.equals(light.type, "point")) {
                    L = new Vect3D(light.position.x - P.x, light.position.y - P.y, light.position.z - P.z);
                } else {
                    L = new Vect3D(light.direction.x, light.direction.y, light.direction.z);
                }
                double n_dot_l = Vect3D.Dot(N, L);
                if (n_dot_l > 0)
                    i += light.intensity * n_dot_l / (Vect3D.Length(N) * Vect3D.Length(L));
            }
        }
        return i;
    }*/
}
