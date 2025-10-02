//
// Created by Mili on 17/09/2025.
//
#include <glad/glad.h>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>

#ifndef PRACTICA1PAG_RENDERER_H
#define PRACTICA1PAG_RENDERER_H
namespace PAG {
    class Renderer {
    private:
        static Renderer* instancia;
        float colorFondo[3] = {0.1, 0.1, 0.1};
        float _colorBorrado[4] = {1.0, 1.0, 1.0, 1.0};
        std::vector<std::string> _mensaje; //buffer para guardar los mensajes que quiero mostrar

        GLuint idVS = 0; // Identificador del vertex shader
        GLuint idFS = 0; // Identificador del fragment shader
        GLuint idSP = 0; // Identificador del shader program
        GLuint idVAO = 0; // Identificador del vertex array object
        GLuint idVBO = 0; // Identificador del vertex buffer object
        GLuint idIBO = 0; // Identificador del index buffer object

        Renderer();

        // función auxiliar para leer shaders desde archivo
        std::string loadShader(const std::string& filename);

        // función auxiliar para comprobar errores
        void checkCompilaError(GLuint shader, std::string type);


    public:
        static Renderer& getInstancia();
        virtual ~Renderer();

        void refrescar();
        void inicializar();
        void redimencionar(int ancho, int alto);

        // colores
        void setColorFondo(const float color[3]);
        const float* getColorFondo() const;


        // mensajes
        void addMensaje(const std::string& msm);
        const std::vector<std::string>& getMensaje() const;

        //Shader
        void creaShaderProgram();

        //Crear la geometria basica
        void creaModelo();

        //Inicializacion de OpenGL
        void inicializaOpenGL();
    };
}
#endif //PRACTICA1PAG_RENDERER_H