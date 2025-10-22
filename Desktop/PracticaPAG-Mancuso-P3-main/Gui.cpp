//
// Created by Mili on 22/09/2025.
//
#include "Gui.h"
#include "imgui/imgui.h"
#include "imgui/imgui_impl_glfw.h"
#include "imgui/imgui_impl_opengl3.h"
#include "Camara.h"


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
        auto& cam = renderer.getCamara();

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

        ImGui::TextUnformatted("Color de fondo");
        if (ImGui::ColorEdit3("##bg", _bgColor, ImGuiColorEditFlags_NoInputs)) {
            renderer.setColorFondo(_bgColor);
        }

        ImGui::End();

        //ventana camara
        if (ImGui::Begin("Camara", nullptr, ImGuiWindowFlags_AlwaysAutoResize)) {
            ImGui::SetWindowFontScale(1.0f); // Cambia a 2.0f para letra doble si quieres

            static int active = 6;
            const char* items =
                    "None\0"
                    "Zoom\0"
                    "Crane\0"
                    "Dolly\0"
                    "Pan\0"
                    "Tilt\0"
                    "Orbit\0\0";
            bool cambiaT = ImGui::Combo("##MovType", &active, items);
            if (cambiaT) {
                switch (active) {
                    case 1: cam.tipo = tipoCamara::Tilt;  break; // Zoom usa onScroll; no necesita modo exclusivo
                    case 2: cam.tipo = tipoCamara::Crane; break;
                    case 3: cam.tipo = tipoCamara::Dolly; break;
                    case 4: cam.tipo = tipoCamara::Pan;   break;
                    case 5: cam.tipo = tipoCamara::Tilt;  break;
                    case 6: cam.tipo = tipoCamara::Orbit; break;
                    default: cam.tipo = tipoCamara::None; break;
                }
            }

            ImGui::Separator();

            // Botoneras por modo
            const float dx = 5;   // paso horizontal simulado
            const float dy = 5;   // paso vertical simulado
            const float rueda = 1; // paso de "rueda"

            switch (active) {
                case 1: { // Zoom
                   ImGui::TextUnformatted("Zoom ");
                    if (ImGui::Button("+")) { cam.onScroll(+ rueda); } ImGui::SameLine();
                    if (ImGui::Button("-")) { cam.onScroll(- rueda); }

                    ImGui::SliderFloat("##Angle", &cam.campoVisY, 15.f, 90.f, "", ImGuiSliderFlags_AlwaysClamp);
                    ImGui::SameLine();
                    ImGui::SetNextItemWidth(60);
                    ImGui::InputFloat("##AngleBox", &cam.campoVisY, 0, 0, "%.0f");
                    cam.campoVisY = std::clamp(cam.campoVisY, 15.0f, 90.0f);
                } break;

                case 2: { // Crane
                    cam.tipo = tipoCamara::Crane;
                    ImGui::TextUnformatted("Crane (Up/Down)");
                    if (ImGui::Button("Up"))   { cam.options(0, +dy); } ImGui::SameLine();
                    if (ImGui::Button("Down")) { cam.options(0, -dy); }
                } break;

                case 3: { // Dolly
                    cam.tipo = PAG::tipoCamara::Dolly;
                    ImGui::TextUnformatted("Direction");

                    // Fila superior: Back
                    if (ImGui::Button("^ Back ^")) { cam.options(0.f, +dy); }  // +dy => alejar

                    // Fila central: Left / Right
                    ImGui::Dummy(ImVec2(0, 4));
                    if (ImGui::Button("<- Left")) { cam.options(-dx, 0.f); }   // usa Pan lateral para sensación lateral
                    ImGui::SameLine();
                    if (ImGui::Button("Right ->")) { cam.options(+dx, 0.f); }

                    // Fila inferior: Front
                    ImGui::Dummy(ImVec2(0, 4));
                    if (ImGui::Button("v Front v")) { cam.options(0.f, -dy); } // -dy => acercar
                } break;

                case 4: { // Pan
                    cam.tipo = PAG::tipoCamara::Pan;
                    ImGui::TextUnformatted("Direction");
                    if (ImGui::Button("<- Left")) { cam.options(-dx, 0.f); }
                    ImGui::SameLine();
                    if (ImGui::Button("Right ->")) { cam.options(+dx, 0.f); }
                } break;

                case 5: { // Tilt
                    cam.tipo = PAG::tipoCamara::Tilt;
                    ImGui::TextUnformatted("Direction");
                    if (ImGui::Button("^ Up ^")) { cam.options(0.f, -dy); }
                    ImGui::SameLine();
                    if (ImGui::Button("v Down v")) { cam.options(0.f, +dy); }
                } break;

                case 6: { // Orbit
                    cam.tipo = PAG::tipoCamara::Orbit;

                    ImGui::TextUnformatted("Latitude");
                    if (ImGui::Button("^ North ^")) { cam.options(0.f, -dy); }  // pitch -
                    ImGui::SameLine();
                    if (ImGui::Button("v South v")) { cam.options(0.f, +dy); }  // pitch +

                    ImGui::Dummy(ImVec2(0, 4));
                    ImGui::TextUnformatted("Longitude");
                    if (ImGui::Button("<- West")) { cam.options(+dx, 0.f); }    // yaw -
                    ImGui::SameLine();
                    if (ImGui::Button("East ->")) { cam.options(-dx, 0.f); }    // yaw +
                } break;

                default: {
                    cam.tipo = tipoCamara::None;
                    ImGui::TextUnformatted("Seleccione un modo para habilitar controles.");
                } break;
            }

         /* //porq queria verlo yo
          * ImGui::Separator();
            ImGui::DragFloat3("Target", &cam.target[0], 0.01f);
            ImGui::SliderFloat("Near", &cam.zNear, 0.01f, 1.f, "%.2f");
            ImGui::SliderFloat("Far",  &cam.zFar,  10.f, 500.f, "%.0f");
            */
        }
        ImGui::End();


        //Ventana Log
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
