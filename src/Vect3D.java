class Vect3D {
    double x, y, z;

    public Vect3D() {
        x = y = z = 0;
    }

    public Vect3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static double Dot(Vect3D v1, Vect3D v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static double Length(Vect3D v) {
        return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
    }

    public static Vect3D MultMatrixVect(Vect3D v, double[][] matrixRot) {
        return new Vect3D(matrixRot[0][0] * v.x + matrixRot[0][1] * v.y + matrixRot[0][2] * v.z,
                matrixRot[1][0] * v.x + matrixRot[1][1] * v.y + matrixRot[1][2] * v.z,
                matrixRot[2][0] * v.x + matrixRot[2][1] * v.y + matrixRot[2][2] * v.z);
    }
}