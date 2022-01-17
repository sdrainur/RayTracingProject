class Light {
    String type;
    float intensity;//яркость, суммируется в 1
    Vect3D position;//точка в пространстве, позиция точечного источника света
    Vect3D direction;//направление направленного источника света

    public Light(String type, float intensity, Vect3D position, Vect3D direction) {
        this.type = type;
        this.intensity = intensity;
        this.position = position;
        this.direction = direction;
    }
}