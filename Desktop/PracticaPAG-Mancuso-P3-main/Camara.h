//
// Created by Mili on 09/10/2025.
//

#ifndef PRACTICA1PAG_CAMARA_H
#define PRACTICA1PAG_CAMARA_H
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <algorithm>

namespace PAG {
    enum class tipoCamara{Orbit, Pan, Dolly, Crane, Tilt, None};

    class Camara {
    private:

        glm::vec3 posCamara; //Guardo la posicion actual de lacamara y en base a ella voy cambiando (No guardo una pos inicial)

        void translateCT(const glm::vec3& delta);     // traslada cámara y target a la vez


    public:
        tipoCamara tipo = tipoCamara::Orbit;

        // Geometría de cámara
        glm::vec3 target{0, 0, 0};
        float distancia = 2;
        float yawRad   = 0;             // rotación Y
        float pitchRad = glm::radians(15.0); // rotación X

        // Proyección
        float campoVisY = 50.0;
        float zNear = 0.1, zFar = 100.0;

        //para el aspect y ratio del view port
        int vpW = 1024, vpH = 576;

        // Sensibilidades
        float sensOrbit = 0.01;
        float sensPan   = 0.002;
        float sensTilt = 0.005;
        float sensDolly = 0.05;
        float sensCrane = 0.05;
        float sensZoom  = 1.0; // grados


        glm::vec3 getPosicionCamara() const;
        void setPosicionCamara(float x, float y, float z);
        void actualizaPosCamara();

        // Matrices
        glm::mat4 matrizVision() const;     // lookAt
        glm::mat4 matrizProyeccion() const; // perspective

        // Interacción
        void onResize(int w, int h) { vpW = w; vpH = h; }
        void options(float dx, float dy);    // arrastre de ratón según modo
        void onScroll(float yoffset);       // rueda zoom

        // vectores para la camara
        glm::vec3 forward() const;
        glm::vec3 right() const;
        glm::vec3 up() const;


    };
}
#endif //PRACTICA1PAG_CAMARA_H