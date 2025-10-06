//
// Created by Mili on 17/09/2025.
//

#include "Renderer.h"
#include <fstream>
#include <sstream>
#include <glad/glad.h>

namespace PAG {
    Renderer *PAG::Renderer::instancia = nullptr;

    //Renderer::Renderer() {}
    Renderer::Renderer()
            : shaderProg(nullptr),
              idSP(0),
              idVAO(0),
              idVBO(0),
              idIBO(0) {
        // colorFondo por defecto (gris)
        colorFondo[0] = 0.6f;
        colorFondo[1] = 0.6f;
        colorFondo[2] = 0.6f;
        _colorBorrado[0] = 0.6f;
        _colorBorrado[1] = 0.6f;
        _colorBorrado[2] = 0.6f;
        _colorBorrado[3] = 1.0f;
        _mensaje.reserve(64);
    }

    //@todo ojo que en el guion era &. por si da problema
    /**
     * Consulta del objeto unico de la clase
     * @return
     */
    Renderer &Renderer::getInstancia() {

        if (!instancia) {
            instancia = new Renderer();
        }
        return *instancia;

    }

    /**
     * Destructor
     */
    /* Renderer::~Renderer() {
         if (shaderProg) {
             delete shaderProg;
             shaderProg = nullptr;
         }
         /*  if (idVS != 0) {
               glDeleteShader(idVS);
           }
           if (idFS != 0) {
               glDeleteShader(idFS);
           }
           if (idSP != 0) {
               glDeleteShader(idSP);
           }
           if (idVBO != 0) {
               glDeleteBuffers(1, &idVBO);
           }
           if (idIBO != 0) {
               glDeleteBuffers(1, &idIBO);
           }
           if (idVAO != 0) {
               glDeleteVertexArrays(1, &idVAO);
           }*/
    /*  }*/
    Renderer::~Renderer() {
        // liberar GL
        if (idIBO) {
            glDeleteBuffers(1, &idIBO);
            idIBO = 0;
        }
        if (idVBO) {
            glDeleteBuffers(1, &idVBO);
            idVBO = 0;
        }
        if (idVAO) {
            glDeleteVertexArrays(1, &idVAO);
            idVAO = 0;
        }

        // liberar ShaderProgram
        delete shaderProg;
        shaderProg = nullptr;

    }


    void Renderer::inicializar() {
        //Establecemos un tono gris medio como color con el que se borrara el frame buffer.
        glClearColor(colorFondo[0], colorFondo[1], colorFondo[2], 1.0);
        // Establecemos al profundidad a la hora de dibujar
        glEnable(GL_DEPTH_TEST);
    }

    void Renderer::refrescar() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (idSP == 0 || idVAO == 0 || idIBO == 0) return;
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glUseProgram(idSP);
        glBindVertexArray(idVAO); //vinculamos el vao en la funcionalidad de refrescar
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idIBO);

        glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, nullptr);


    }

    void Renderer::redimencionar(int ancho, int alto) {
        glViewport(0, 0, ancho, alto);
    }

    void Renderer::setColorFondo(const float color[3]) {
        colorFondo[0] = color[0];
        colorFondo[1] = color[1];
        colorFondo[2] = color[2];
        glClearColor(colorFondo[0], colorFondo[1], colorFondo[2],
                     1.0); //esto es necesario si quiero que cambie el color de fondo
    }

    const float *Renderer::getColorFondo() const {
        return colorFondo;
    }

    void Renderer::addMensaje(const std::string &msm) {
        _mensaje.push_back(msm);
    }

    const std::vector<std::string> &Renderer::getMensaje() const {
        return _mensaje;
    }


    void PAG::Renderer::creaModelo() {
        /**
         * OPCIONAL CON VBO NO ENTRELAZADOS
         */
        /* GLfloat vertices[] = {-0.5, -0.5, 0,
                               0.5, -0.5, 0,
                               0, 0.5, 0};


         GLfloat colores[] = {
                 1.0, 0, 0.5,
                 1.0, 0.4, 0.7,
                 1.0, 0.7, 0.9
         };

         GLuint indices[] = {0, 1, 2};

         glGenVertexArrays(1, &idVAO);
         glBindVertexArray(idVAO);

         // VBO para posicion
         glGenBuffers(1, &idVBO);
         glBindBuffer(GL_ARRAY_BUFFER, idVBO);
         glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
         glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, nullptr);
         glEnableVertexAttribArray(0);

         // VBO para los colores
         GLuint idColorVBO;
         glGenBuffers(1, &idColorVBO);
         glBindBuffer(GL_ARRAY_BUFFER, idColorVBO);
         glBufferData(GL_ARRAY_BUFFER, sizeof(colores), colores, GL_STATIC_DRAW);
         glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 0, nullptr);
         glEnableVertexAttribArray(1);

         // el IBO
         glGenBuffers(1, &idIBO);
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idIBO);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);
         */

        /**
         * OPCIONAL CON VBO ENTRELAZADOS
         */
        GLfloat vertices[] = {
                // posiciones                    // colores
                -0.5, -0.5, 0, 0, 1.0, 0.5,
                0.5, -0.5, 0, 0.8, 0, 0.7,
                0, 0.5, 0, 0, 1.0, 0.9
        };

        GLuint indices[] = {0, 1, 2};

        glGenVertexArrays(1, &idVAO);
        glBindVertexArray(idVAO);

        glGenBuffers(1, &idVBO);
        glBindBuffer(GL_ARRAY_BUFFER, idVBO);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);


        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (void *) 0);
        glEnableVertexAttribArray(0);


        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (void *) (3 * sizeof(GLfloat)));
        glEnableVertexAttribArray(1);

        glGenBuffers(1, &idIBO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    }


    /**
     * Metodo para inicializar los parametros globales de OpenGL
     */

    void PAG::Renderer::inicializaOpenGL() {
        glClearColor(_colorBorrado[0], _colorBorrado[1], _colorBorrado[2], _colorBorrado[3]);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);
    }


// Método auxiliar para leer ficheros de shader
    std::string Renderer::loadShader(const std::string &filename) {
        std::ifstream file(filename);
        if (!file.is_open()) {
            addMensaje("No se puede abrir el archivo: " + filename);
            return "";
        }
        std::stringstream buffer;
        buffer << file.rdbuf();
        return buffer.str();
    }

    void Renderer::creaShaderProgram() {
        // por compatibilidad cargamos el shader por defecto "pag03"
        loadShaderProgramFromBase("pag03");
    }

/**
 * Metodo para cerar, compilar y enlazar el shader program.
 */
/*  void PAG::Renderer::creaShaderProgram() {

      std::string miVertexShader = loadShader("../pag03-vs.glsl");
      std::string miFragmentShader = loadShader("../pag03-fs.glsl");

      idVS = glCreateShader(GL_VERTEX_SHADER);
      const GLchar* fuenteVS = miVertexShader.c_str();
      glShaderSource(idVS, 1, &fuenteVS, NULL);
      glCompileShader(idVS);
      checkCompilaError(idVS, "VERTEX");

      idFS = glCreateShader(GL_FRAGMENT_SHADER);
      const GLchar* fuenteFS = miFragmentShader.c_str();
      glShaderSource(idFS, 1, &fuenteFS, NULL);
      glCompileShader(idFS);
      checkCompilaError(idFS, "FRAGMENT");

      idSP = glCreateProgram();
      glAttachShader(idSP, idVS);
      glAttachShader(idSP, idFS);
      glLinkProgram(idSP);
      checkCompilaError(idSP, "PROGRAMA");
  }*/

    void Renderer::loadShaderProgramFromBase(const std::string &baseName) {

      /*  if (!shaderProg) shaderProg = new ShaderProgram();

        std::vector<std::string> msgs;
        GLuint newProgram = shaderProg->loadFromBaseName(baseName, msgs);
        if (newProgram != 0) {
            idSP = newProgram;
            addMensaje("Programa shader activo: " + baseName);
        }
        // volcar todos los mensajes generados por el ShaderProgram al buffer de mensajes del Renderer
        for (const auto &m: msgs) addMensaje(m);
*/
        auto* nuevo = new ShaderProgram(); //hago uno temporal por si falla algo, no toco el actual

        std::vector<std::string> msgs;
        GLuint nuevoID = nuevo->loadFromBaseName(baseName, msgs);

        for (const auto& m : msgs) addMensaje(m);

        // Si falla, no tocamos el programa actual
        if (nuevoID == 0) {
            addMensaje(" Error al cargar shaders '" + baseName + "'. Se mantiene el programa anterior.");
            delete nuevo;
            return;
        }

        //si anda bien, libero el anterior y me quedo con el nuevo
        if (shaderProg) {
            delete shaderProg;
            shaderProg = nullptr;
            idSP = 0;
        }
        shaderProg = nuevo;
        idSP = nuevoID;

        addMensaje("Programa shader: " + baseName + " (program=" + std::to_string(idSP) + ")");

    }

/**
 * comprobar errores de compilación/linkado
 * @param shader
 * @param type
 */
    void Renderer::checkCompilaError(GLuint shader, std::string type) {
        GLint success;
        GLchar infoLog[1024];
        if (type != "PROGRAMA") {
            glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
            if (!success) {
                glGetShaderInfoLog(shader, 1024, nullptr, infoLog);
                addMensaje("ERROR de compilacion " + type + ": " + std::string(infoLog));
            }
        } else {
            glGetProgramiv(shader, GL_LINK_STATUS, &success);
            if (!success) {
                glGetProgramInfoLog(shader, 1024, nullptr, infoLog);
                addMensaje("ERROR enlazando PROGRAMA: " + std::string(infoLog));
            }
        }
    }
}