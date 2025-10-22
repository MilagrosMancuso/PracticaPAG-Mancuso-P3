
#include <iostream>
#include <glad/glad.h>
#include <GLFW/glfw3.h>
#include "Renderer.h"
#include "GUI.h"
#include "imgui/imgui.h"
#include "imgui/imgui_impl_glfw.h"
#include "imgui/imgui_impl_opengl3.h"
#include "Camara.h"

float colorf[3] = {0.6, 0.6, 0.6}; //gris de fondo

static bool   botonIzqPulse = false;   // ¿LMB pulsado?
static double ultimaX = 0.0;       // última X del cursor
static double ultimaY = 0.0;       // última Y del cursor


//Callbacks:
void error_callback ( int error, const char* desc ){
    std::string aux (desc);
    PAG::Renderer::getInstancia().addMensaje(
            "Error de GLFW número " + std::to_string(error) + ": " + aux);
}

// Cada vez que el área de dibujo OpenGL deba ser redibujada.
void window_refresh_callback ( GLFWwindow *window ){
    PAG::Renderer::getInstancia().refrescar();
    glfwSwapBuffers(window);
    PAG::Renderer::getInstancia().addMensaje(
            "Refresh callback called " );
}

// Cada vez que se cambie el tamaño del área de dibujo OpenGL.
void framebuffer_size_callback ( GLFWwindow *window, int width, int height ){
    auto& r = PAG::Renderer::getInstancia();
    r.redimencionar(width,height);
    r.addMensaje("Resize callback called ");
}

// Cada vez que se pulse una tecla dirigida al área de dibujo OpenGL.
void key_callback ( GLFWwindow *window, int key, int scancode, int action, int mods ){
    //CON ESTO DEBERIA PODER USAR EL TAB PARA CAMBIAR DE COSAS, EN VEZ DE TENER QUE USAR SIEMPRE EL MOUSE.
    //ImGuiIO& io = ImGui::GetIO();
    //io.AddMouseButtonEvent(button, pressed);

    if ( key == GLFW_KEY_ESCAPE && action == GLFW_PRESS ){
        glfwSetWindowShouldClose(window, GLFW_TRUE);
    }
    PAG::Renderer::getInstancia().addMensaje(
            "Key callback called" );
}

// Cada vez que se pulse algún botón del ratón sobre el área de dibujo OpenGL.
void mouse_button_callback ( GLFWwindow *window, int button, int action, int mods ){
    /*ImGuiIO& io = ImGui::GetIO();
    if ( action == GLFW_PRESS ) {
        io.AddMouseButtonEvent(button, true);

    } else if (action == GLFW_RELEASE) {
        io.AddMouseButtonEvent ( button, false );

    }
*/
    ImGuiIO& io = ImGui::GetIO();

    // Propagar a ImGui (para widgets)
    if ( action == GLFW_PRESS )  io.AddMouseButtonEvent ( button, true );
    if ( action == GLFW_RELEASE ) io.AddMouseButtonEvent ( button, false );

    // Nuestro estado para arrastre con LMB
    if (button == GLFW_MOUSE_BUTTON_LEFT) {
        if (action == GLFW_PRESS) {
            botonIzqPulse = true;
            // guardamos posición al presionar
            glfwGetCursorPos(window, &ultimaX, &ultimaY);
        } else if (action == GLFW_RELEASE) {
            botonIzqPulse = false;
        }
    }
}

// Cada vez que se mueva la rueda del ratón sobre el área de dibujo OpenGL.
void scroll_callback ( GLFWwindow *window, double xoffset, double yoffset ){
  /*  //@todo EJERCICIO 1:
    auto& renderer = PAG::Renderer::getInstancia();
    const float* actual = renderer.getColorFondo();
    float nuevoC[3] = {actual[0], actual[1], actual[2] };

    for(int i=0; i < 3; i++){
        colorf[i] += 0.05 * yoffset;
        //limitamos para que no se pase del rango
        if (nuevoC[i] < 0.0) {nuevoC[i] = 0.0;}
        if (nuevoC[i] > 1.0) {nuevoC[i] = 1.0;}
    }*/
    ImGuiIO& io = ImGui::GetIO();
    // Si ImGui NO quiere el mouse, lo usamos para zoom de la cámara
    if (!io.WantCaptureMouse) {
        auto& cam = PAG::Renderer::getInstancia().getCamara();
        cam.onScroll((float)yoffset);
    }

    //renderer.setColorFondo(nuevoC);

}

//este es para tener el movimiento del cursor
void cursor_pos_callback(GLFWwindow* window, double xpos, double ypos) {
    ImGuiIO& io = ImGui::GetIO();

    // Si ImGui quiere el mouse no tocamos la cámara
    if (io.WantCaptureMouse) {
        // Actualizamos referencia para no pegar saltos cuando vuelva el control
        ultimaX = xpos;
        ultimaY = ypos;
        return;
    }

    if (!botonIzqPulse) {
        // Si no está pulsado, sólo actualizamos la referencia
        ultimaX = xpos;
        ultimaY = ypos;
        return;
    }

    // Delta (en píxeles) desde el último evento
    float dx = static_cast<float>(xpos - ultimaX);
    float dy = static_cast<float>(ypos - ultimaY);

    // Comunicar al Renderer → Camara (usa el modo actual: Pan/Dolly/Crane/Tilt/Orbit)
    auto& cam = PAG::Renderer::getInstancia().getCamara();
    cam.options(dx, dy);   // tus sensibilidades internas se encargan de la escala

    // Avanzar la referencia
    ultimaX = xpos;
    ultimaY = ypos;
}


int main() {
    // Callback de errores ANTES de inicializar GLFW
    glfwSetErrorCallback(error_callback);

    // Inicializamos GLFW. Ojo, solo lo hacemos una vez en toda la app
    if ( glfwInit () != GLFW_TRUE ){
        PAG::Renderer::getInstancia().addMensaje(
                "Failed to initialize GLFW" );
        return -1;
    }

    // Definimos las caracteristicas del contexto grafico OpenGl de la ventana que crearemos.
    glfwWindowHint( GLFW_SAMPLES, 4); // Activa antialiasing con 4 muestras
    glfwWindowHint ( GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE );
    glfwWindowHint ( GLFW_CONTEXT_VERSION_MAJOR, 4 );
    glfwWindowHint ( GLFW_CONTEXT_VERSION_MINOR, 1 );

    // Definimos el puntero para guardar la dirección de la ventana de la aplicación y la creamos
    GLFWwindow *window;
    //Establecemos el tamaño de la ventana / sin compartir recursos con otras ventanas.
    window = glfwCreateWindow ( 1024, 576, "PAG Introduction", nullptr, nullptr );

    // Vemos si se creo correctamente la ventana
    if ( window == nullptr ){
        PAG::Renderer::getInstancia().addMensaje(
                "Failed to open GLFW window" );
        glfwTerminate (); // Liberamos los recursos que ocupaba GLFW
        return -2;
    }

    glfwMakeContextCurrent ( window );

    // inicializamos GLAD.
    if ( !gladLoadGLLoader ( (GLADloadproc) glfwGetProcAddress ) ){
        PAG::Renderer::getInstancia().addMensaje(
                "GLAD initialization failed" );
        glfwDestroyWindow ( window ); // Liberamos los recursos que ocupaba GLFW.
        window = nullptr;
        glfwTerminate ();
        return -3;
    }

    //Vemos el contexto 3D construido
    std::cout << glGetString ( GL_RENDERER ) << std::endl
              << glGetString ( GL_VENDOR ) << std::endl
              << glGetString ( GL_VERSION ) << std::endl
              << glGetString ( GL_SHADING_LANGUAGE_VERSION ) << std::endl;

    //Registramos tdos los callbacks
    glfwSetWindowRefreshCallback(window, window_refresh_callback);
    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);
    glfwSetKeyCallback(window, key_callback);

    glfwSetCursorPosCallback(window, cursor_pos_callback);   // <--- NUEVO
    glfwSetMouseButtonCallback(window, mouse_button_callback);
    glfwSetScrollCallback(window, scroll_callback);

    // Inicialización
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;

    ImGui_ImplGlfw_InitForOpenGL ( window, true );
    ImGui_ImplOpenGL3_Init ();

    //Instrancias
    auto& renderer = PAG::Renderer::getInstancia();
    auto& gui = PAG::GUI::getInstancia();

    PAG::Renderer::getInstancia().creaShaderProgram();
    PAG::Renderer::getInstancia().creaModelo();

    renderer.inicializaOpenGL();
    renderer.creaShaderProgram();
    renderer.creaModelo();

    // sincronizar cámara con tamaño inicial
    renderer.redimencionar(1024, 576);

    // Ciclo de eventos de la aplicación. condición de parada = ventana principal deba cerrarse.
    while ( !glfwWindowShouldClose ( window ) ){
        // refrescamos con Renderer para dibujar
        renderer.refrescar();

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplGlfw_NewFrame();
        ImGui::NewFrame();


        //dibujamos
        gui.dibuja();
        ImGui::Render();
        ImGui_ImplOpenGL3_RenderDrawData (ImGui::GetDrawData());

        // MATRICES: enviar view/proj desde la cámara
        const auto& cam = renderer.getCamara();
        renderer.setMatrices(cam.matrizVision(), cam.matrizProyeccion());

        // Dibujar escena
        renderer.refrescar();

        //Renderizar ImGui
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

        glfwSwapBuffers(window);
        glfwPollEvents ();
    }

    //liberar recursos una vez terminado el ciclo de eventos.
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplGlfw_Shutdown();
    ImGui::DestroyContext();

    glfwDestroyWindow ( window ); // Cerramos y destruimos la ventana de la aplicación.
    window = nullptr;
    glfwTerminate (); // Liberamos los recursos que ocupaba GLFW.

}
