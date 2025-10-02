#include <iostream>
#include <glad/glad.h>
#include <GLFW/glfw3.h>
#include "Renderer.h"
#include "GUI.h"
#include "imgui/imgui.h"
#include "imgui/imgui_impl_glfw.h"
#include "imgui/imgui_impl_opengl3.h"

float colorf[3] = {0.6, 0.6, 0.6}; //gris de fondo

//Callbacks:
// Si GLFW produce algún error
void error_callback ( int error, const char* desc ){
    std::string aux (desc);
    std::cout << "Error de GLFW número " << error << ": " << aux << std::endl;
}

// Cada vez que el área de dibujo OpenGL deba ser redibujada.
void window_refresh_callback ( GLFWwindow *window ){
    //esto ahora lo hacemos desde Renderer
    PAG::Renderer::getInstancia().refrescar();
    glfwSwapBuffers(window);

    std::cout << "Refresh callback called" << std::endl;
}

// Cada vez que se cambie el tamaño del área de dibujo OpenGL.
void framebuffer_size_callback ( GLFWwindow *window, int width, int height ){
    PAG::Renderer::getInstancia().redimencionar(width,height);
    std::cout << "Resize callback called" << std::endl;
}

// Cada vez que se pulse una tecla dirigida al área de dibujo OpenGL.
void key_callback ( GLFWwindow *window, int key, int scancode, int action, int mods ){
    if ( key == GLFW_KEY_ESCAPE && action == GLFW_PRESS ){
        glfwSetWindowShouldClose(window, GLFW_TRUE);
    }
    std::cout << "Key callback called" << std::endl;
}

// Cada vez que se pulse algún botón del ratón sobre el área de dibujo OpenGL.
void mouse_button_callback ( GLFWwindow *window, int button, int action, int mods ){
    if ( action == GLFW_PRESS ){
        // Si es necesario hacer algo con este evento, indicarlo aquí
        // Finalmente, comunica el evento de ratón a ImGui
        ImGuiIO& io = ImGui::GetIO ();
        io.AddMouseButtonEvent ( button, true );
    }
    else if ( action == GLFW_RELEASE ){
        // Si es necesario hacer algo con este evento, indicarlo aquí
        // Finalmente, comunica el evento de ratón a ImGui
        ImGuiIO& io = ImGui::GetIO ();
        io.AddMouseButtonEvent ( button, false );
    }

}

// Cada vez que se mueva la rueda del ratón sobre el área de dibujo OpenGL.
void scroll_callback ( GLFWwindow *window, double xoffset, double yoffset ){
    //@todo EJERCICIO 1:
    auto& renderer = PAG::Renderer::getInstancia();
    // tomo el color actual del Renderer
    const float* actual = renderer.getColorFondo();
    float nuevoC[3] = {actual[0], actual[1], actual[2] };

    for(int i=0; i < 3; i++){
        colorf[i] += 0.05 * yoffset;
        //limitamos para que no se pase del rango
        if (nuevoC[i] < 0.0) {nuevoC[i] = 0.0;}
        if (nuevoC[i] > 1.0) {nuevoC[i] = 1.0;}
    }

    /*std::cout << "Movida la rueda del raton " << xoffset
              << " Unidades en horizontal y " << yoffset
              << " unidades en vertical" << std::endl;
              */
}


int main() {
    std::cout << "Starting application PAG" << std::endl;

    // Callback de errores ANTES de inicializar GLFW
    glfwSetErrorCallback(error_callback);

    // Inicializamos GLFW. Ojo, solo lo hacemos una vez en toda la app
    if ( glfwInit () != GLFW_TRUE ){
        std::cout << "Failed to initialize GLFW" << std::endl;
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
        std::cout << "Failed to open GLFW window" << std::endl;
        glfwTerminate (); // Liberamos los recursos que ocupaba GLFW
        return -2;
    }

    // Hace que el contexto OpenGL asociado a la ventana que acabamos de crear pase a
    // ser el contexto actual de OpenGL para las siguientes llamadas a la biblioteca
    glfwMakeContextCurrent ( window );

    // inicializamos GLAD.
    if ( !gladLoadGLLoader ( (GLADloadproc) glfwGetProcAddress ) ){
        std::cout << "GLAD initialization failed" << std::endl;
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

        //Arreglamos el viewport
        int anchoV, altoV;
        glfwGetFramebufferSize(window, &anchoV, &altoV);

        //vemos si se redimenciono desde renderer
        PAG::Renderer::getInstancia().redimencionar(anchoV, altoV);

        // Usamos el color almacenado en Renderer para la pantalla
        PAG::Renderer::getInstancia().refrescar();

        //Renderizar ImGui
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

        glfwSwapBuffers(window);
        glfwPollEvents ();
    }

    //liberar recursos una vez terminado el ciclo de eventos.
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplGlfw_Shutdown();
    ImGui::DestroyContext();

    std::cout << "Finishing application pag prueba" << std::endl;
    glfwDestroyWindow ( window ); // Cerramos y destruimos la ventana de la aplicación.
    window = nullptr;
    glfwTerminate (); // Liberamos los recursos que ocupaba GLFW.

}