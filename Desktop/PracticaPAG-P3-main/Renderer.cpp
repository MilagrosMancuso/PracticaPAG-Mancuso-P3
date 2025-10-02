//
// Created by Mili on 17/09/2025.
//

#include "Renderer.h"

namespace PAG {
    Renderer* PAG::Renderer::instancia = nullptr;

    Renderer::Renderer() {}


    //@todo ojo que en el guion era &. por si da problema
    /**
     * Consulta del objeto unico de la clase
     * @return
     */
    Renderer& Renderer::getInstancia() {
        if (!instancia) {
            instancia = new Renderer();
        }
        return *instancia;
    }

    /**
     * Destructor
     */
    Renderer::~Renderer() {
        if (idVS != 0) {
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
        }
    }


    // Método auxiliar para leer ficheros de shader
    std::string Renderer::loadShader(const std::string& filename) {
        std::ifstream file(filename);
        if (!file.is_open()) {
            addMensaje("No se puede abrir el archivo: " + filename);
            return "";
        }
        std::stringstream buffer;
        buffer << file.rdbuf();
        return buffer.str();
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


    void Renderer::inicializar() {
        //Establecemos un tono gris medio como color con el que se borrara el frame buffer.
        glClearColor(colorFondo[0], colorFondo[1], colorFondo[2], 1.0);
        // Establecemos al profundidad a la hora de dibujar
        glEnable( GL_DEPTH_TEST);
    }

    void Renderer::refrescar() {
        glClearColor(colorFondo[0], colorFondo[1], colorFondo[2], 1.0); //esto es necesario si quiero que cambie el color de fondo
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
    }

    const float* Renderer::getColorFondo() const {
        return colorFondo;
    }

    void Renderer::addMensaje(const std::string& msm) {
        _mensaje.push_back(msm);
    }

    const std::vector<std::string>& Renderer::getMensaje() const {
        return _mensaje;
    }

    /**
     * Metodo para cerar, compilar y enlazar el shader program.
     */
    void PAG::Renderer::creaShaderProgram() {

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
    }

    void PAG::Renderer::creaModelo() {
     /*   GLfloat vertices[] = {-0.5, -0.5, 0,
                              0.5, -0.5, 0,
                              0, 0.5, 0};

        GLuint indices[] = {0, 1, 2};

        glGenVertexArrays(1, &idVAO);
        glBindVertexArray(idVAO);
        glGenBuffers(1, &idVBO);
        glBindBuffer(GL_ARRAY_BUFFER, idVBO);
        glBufferData(GL_ARRAY_BUFFER, 9*sizeof(GLfloat), vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3*sizeof(GLfloat), NULL);
        glEnableVertexAttribArray(0);
        glGenBuffers(1, &idIBO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 3*sizeof(GLuint), indices, GL_STATIC_DRAW);
     */
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
                -0.5, -0.5, 0,   0, 1.0, 0.5,
                0.5, -0.5, 0,   0.8, 1.0, 0.7,
                0,  0.5, 0,   0, 1.0, 0.9
        };

        GLuint indices[] = {0, 1, 2};

        glGenVertexArrays(1, &idVAO);
        glBindVertexArray(idVAO);

        glGenBuffers(1, &idVBO);
        glBindBuffer(GL_ARRAY_BUFFER, idVBO);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);


        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (void*)0);
        glEnableVertexAttribArray(0);


        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (void*)(3 * sizeof(GLfloat)));
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

}