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
    GUI::GUI(){
        // Color inicial alineado con Renderer (gris medio)
        _bgColor[0] = 0.6f;
        _bgColor[1] = 0.6f;
        _bgColor[2] = 0.6f;
    }

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

        // --- Ventana: Shaders ---
        ImGui::Begin("Shaders");

        // Campo de texto: usa std::string* + imgui_stdlib.h para evitar el error
        ImGui::InputText("Base name##shader", _baseName, ImGuiInputTextFlags_AutoSelectAll);

        ImGui::SameLine();
        if (ImGui::Button("Load")) {
            renderer.loadShaderProgramFromBase(_baseName);
        }

        // Ayuda de rutas
        ImGui::TextWrapped("Coloca '%s-vs.glsl' y '%s-fs.glsl' en el directorio de ejecucion, ./shaders/, ../ o ../shaders/ (o define SHADER_DIR).",
                           _baseName, _baseName);

        ImGui::Separator();

        // Color de fondo (opcional para cumplir el cambio de color en runtime)
        ImGui::TextUnformatted("Color de fondo");
        if (ImGui::ColorEdit3("##bg", _bgColor, ImGuiColorEditFlags_NoInputs)) {
            renderer.setColorFondo(_bgColor);
        }

        ImGui::End();

        // --- Ventana: Log ---
        ImGui::Begin("Log");
        ImGui::Checkbox("Auto-scroll", &_autoLog);
        ImGui::Separator();

        const auto& mensajes = renderer.getMensaje();
        ImGui::BeginChild("LogRegion", ImVec2(0, 0), true, ImGuiWindowFlags_HorizontalScrollbar);
        for (const auto& m : mensajes) {
            ImGui::TextUnformatted(m.c_str());
        }
        if (_autoLog && ImGui::GetScrollY() >= ImGui::GetScrollMaxY()) {
            ImGui::SetScrollHereY(1.0f);
        }
        ImGui::EndChild();

        ImGui::End();
    }
}
