//
// Created by Mili on 22/09/2025.
//
#include "Gui.h"
#include "imgui/imgui.h"
#include "imgui/imgui_impl_glfw.h"
#include "imgui/imgui_impl_opengl3.h"


namespace PAG {
    GUI* GUI::instancia = nullptr;

    /**
   * Constructor
   */
    GUI::GUI() {}

    /**
     * Destructor
     */
    GUI::~GUI() {}

    GUI& GUI::getInstancia() {
        if (!instancia)
            instancia = new GUI();
        return *instancia;
    }

    /** Metodo para dibujar las ventanas de color y mensaje
     *
     */
    void GUI::dibuja() {
        auto& renderer = Renderer::getInstancia();

        // Ventana de configuración para el color del fondo
        ImGui::Begin("Configuración");
        float c[3];
        const float* actual = renderer.getColorFondo();

        c[0] = actual[0];
        c[1] = actual[1];
        c[2] = actual[2];

        if (ImGui::ColorEdit3("Color de fondo", c)) {
            renderer.setColorFondo(c);
            renderer.addMensaje("Color de fondo actualizado.");
        }

        ImGui::End();

        // Ventana de mensajes
        ImGui::Begin("Mensajes");
        for (const auto& msg : renderer.getMensaje()) {
            ImGui::Text("%s", msg.c_str());
        }
        ImGui::End();
    }
}
