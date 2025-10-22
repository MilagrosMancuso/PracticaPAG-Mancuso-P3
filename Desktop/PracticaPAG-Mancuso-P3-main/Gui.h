//
// Created by Mili on 22/09/2025.
//

#ifndef PRACTICA1PAG_GUI_H
#define PRACTICA1PAG_GUI_H
#include "Renderer.h"

namespace PAG {
    class GUI {
    private:
        static GUI* instancia;
        GUI();
        char _baseName[128] = "pag05";     // nombre base para los shaders
        bool _autoLog = true;

        // para probar ColorFondo
        float _bgColor[3];

    public:
        ~GUI();
        static GUI& getInstancia();

        void dibuja();   // dibuja toda la interfaz de ImGui
    };
}

#endif //PRACTICA1PAG_GUI_H
