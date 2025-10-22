//
// Created by Mili on 09/10/2025.
//

#include "Camara.h"

using namespace PAG;

glm::vec3 Camara::getPosicionCamara() const {
    return posCamara;
}

void Camara::setPosicionCamara(float x, float y, float z) {
    posCamara = glm::vec3{x, y, z};
    // al fijar posición abs reajustamos yaw/pitch/distancia manteniendo target
    glm::vec3 d = target - posCamara;
    distancia = glm::length(d);

    if (distancia < 1e-6) distancia = 1e-6; //para usar un numero chiquito sin reprecentarlo

    glm::vec3 nd = d / distancia;
    pitchRad = asinf(nd.y);
    yawRad   = atan2f(nd.x, nd.z);
}

void Camara::actualizaPosCamara() {
    float cp = cosf(pitchRad), sp = sinf(pitchRad);
    float cy = cosf(yawRad),   sy = sinf(yawRad);
    glm::vec3 offset{
        distancia * cp * sy,   // X
        distancia * sp,        // Y
        distancia * cp * cy    // Z
    };
    posCamara = target + offset;
}

glm::mat4 Camara::matrizVision() const {
    return glm::lookAt(posCamara, target, glm::vec3(0,1,0));
}

glm::mat4 Camara::matrizProyeccion() const {
    //Si el alto del viewport es mayor que cero, aspect = ancho/alto, si no usa 1
    float aspect = (vpH > 0) ? float(vpW) / float(vpH) : 1.0;
    return glm::perspective(glm::radians(campoVisY), aspect, zNear, zFar);
}


glm::vec3 Camara::forward() const {
    return glm::normalize(target - posCamara);
}
glm::vec3 Camara::right() const {
    return glm::normalize(glm::cross(forward(), glm::vec3(0,1,0)));
}
glm::vec3 Camara::up() const {
    return glm::normalize(glm::cross(right(), forward()));
}


void Camara::options(float dx, float dy) {
    switch (tipo) {
        case tipoCamara::Orbit: {
            yawRad   -= dx * sensOrbit;
            pitchRad += dy * sensOrbit;
            pitchRad = std::clamp(pitchRad, glm::radians(-89.0f), glm::radians(89.0f));
            actualizaPosCamara();
        } break;

        case tipoCamara::Pan: {
            // desplaza el target en el plano de la vista
            target -= right() * (dx * sensPan * distancia);
            target += up()    * (dy * sensPan * distancia);
            actualizaPosCamara();


        } break;

        case tipoCamara::Dolly: {
            if (dx != 0.0f) {
                const float s = dx * sensPan * distancia;  //para mantener la distancia
                const glm::vec3 r = right();

                target    -= r * s;
                posCamara -= r * s;

            }

            // Adelante/Atras
            if (dy != 0.0f) {
                distancia *= (1.0f + dy * sensDolly);
                distancia = std::max(0.05f, distancia);
                actualizaPosCamara();
            }
        } break;

        case tipoCamara::Crane: {//tengo mis dudas con estos dos. crane y tilt
            target += glm::vec3(0, -dy * sensCrane * distancia, 0);
            actualizaPosCamara();

        } break;

        case tipoCamara::Tilt: {
            const float sy = dy * sensTilt * std::max(distancia, 0.01f);
            glm::vec3 delta ={0.0f, -sy, 0.0f};
            posCamara += delta;
            target    += delta;
        } break;
        default: break;
    }
}



void Camara::onScroll(float yoffset) {
    campoVisY -= yoffset * sensZoom;
    campoVisY = std::clamp(campoVisY, 15.0f, 90.0f);
}


void Camara::translateCT(const glm::vec3& delta) {
    posCamara += delta;
    target    += delta;
}