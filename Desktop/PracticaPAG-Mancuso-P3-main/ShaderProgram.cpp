//
// Created by Mili on 02/10/2025.
//

#include "ShaderProgram.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <glad/glad.h>


namespace PAG {

    //Porque no me andaba la ruta de fs y vs
    static bool tryReadFile(const std::string& path, std::string& outText) {
        std::ifstream f(path, std::ios::in | std::ios::binary);
        if (!f.is_open()) return false;
        std::ostringstream ss;
        ss << f.rdbuf();
        outText = ss.str();
        return true;
    }

    // Devuelve el contenido del primer path que exista
    static bool readFromCandidates(const std::string& fileName, std::string& outText, std::string& foundPath) {
        // Orden de búsqueda relativo al working directory del proceso
        const char* candidates[] = {
                "",
                "shaders/",
                "../",
                "../shaders/"
        };
        // Rutas relativas
        for (size_t i = 0; i < sizeof(candidates)/sizeof(candidates[0]); ++i) {
            std::string p = std::string(candidates[i]) + fileName;
            if (tryReadFile(p, outText)) { foundPath = p; return true; }
        }

        return false;
    }


    ShaderProgram::ShaderProgram() {}
    ShaderProgram::~ShaderProgram() { destroy(); }

    void ShaderProgram::destroy() {
        if (programID != 0) {
            glDeleteProgram(programID);
            programID = 0;
        }
        if (fs != 0) {
            glDeleteShader(fs);
            fs = 0;
        }
        if (vs != 0) {
            glDeleteShader(vs);
            vs = 0;
        }
    }

    // Ahora intenta múltiples rutas conocidas
    std::string ShaderProgram::loadFileToString(const std::string &filename, bool &ok, std::string &err) {
        ok = false; err.clear();
        std::string text, found;
        if (!readFromCandidates(filename, text, found)) {
            err = "No se puede abrir el archivo en rutas conocidas: " + filename;
            return "";
        }

        ok = true;
        return text;
    }

    void ShaderProgram::checkCompileErrors(GLuint obj, const std::string &type, std::vector<std::string> &outMsgs) {
        GLint success;
        GLchar infoLog[1024];
        if (type != "PROGRAM") {
            glGetShaderiv(obj, GL_COMPILE_STATUS, &success);
            if (!success) {
                glGetShaderInfoLog(obj, 1024, nullptr, infoLog);
                outMsgs.push_back("ERROR de compilación " + type + ": " + std::string(infoLog));
            }
        } else {
            glGetProgramiv(obj, GL_LINK_STATUS, &success);
            if (!success) {
                glGetProgramInfoLog(obj, 1024, nullptr, infoLog);
                outMsgs.push_back("ERROR enlazando PROGRAM: " + std::string(infoLog));
            }
        }
    }

    GLuint ShaderProgram::loadFromBaseName(const std::string &baseName, std::vector<std::string> &outMsgs) {
        //no destruyo primero por las dudas
        std::string vsName = baseName + "-vs.glsl";
        std::string fsName = baseName + "-fs.glsl";

        // Para log: intentamos resolver y decir desde dónde leímos
        std::string vsText, vsFound;
        std::string fsText, fsFound;

        // Intentar lectura VS
        if (!readFromCandidates(vsName, vsText, vsFound)) {
            outMsgs.push_back("No se encontró VS: " + vsName );
            return 0;
        } else {
            outMsgs.push_back("VS -> " + vsFound);
        }

        // Intentar lectura FS
        if (!readFromCandidates(fsName, fsText, fsFound)) {
            outMsgs.push_back("No se encontró FS: " + fsName );
            return 0;
        } else {
            outMsgs.push_back("FS -> " + fsFound);
        }

        // compilar vertex shader
        GLuint tempVS = glCreateShader(GL_VERTEX_SHADER);
        const GLchar* vsrc = vsText.c_str();
        glShaderSource(tempVS, 1, &vsrc, nullptr);
        glCompileShader(tempVS);
        checkCompileErrors(tempVS, "VERTEX", outMsgs);

        // compilar fragment shader
        GLint vsOK = GL_FALSE;
        glGetShaderiv(tempVS, GL_COMPILE_STATUS, &vsOK);
        if (vsOK != GL_TRUE) {
            glDeleteShader(tempVS);
            return 0;
        }

        GLuint tempFS = glCreateShader(GL_FRAGMENT_SHADER);
        const GLchar* fsrc = fsText.c_str();
        glShaderSource(tempFS, 1, &fsrc, nullptr);
        glCompileShader(tempFS);
        checkCompileErrors(tempFS, "FRAGMENT", outMsgs);

        GLint fsOK = GL_FALSE;
        glGetShaderiv(tempFS, GL_COMPILE_STATUS, &fsOK);
        if (fsOK != GL_TRUE) {
            glDeleteShader(tempVS);
            glDeleteShader(tempFS);
            return 0;
        }

        // link
        GLuint tempProg = glCreateProgram();
        glAttachShader(tempProg, tempVS);
        glAttachShader(tempProg, tempFS);
        glLinkProgram(tempProg);
        checkCompileErrors(tempProg, "PROGRAM", outMsgs);

        GLint linkOK = GL_FALSE;
        glGetProgramiv(tempProg, GL_LINK_STATUS, &linkOK);

        // Desacoplar y limpiar shaders temporales
        glDetachShader(tempProg, tempVS);
        glDetachShader(tempProg, tempFS);
        glDeleteShader(tempVS);
        glDeleteShader(tempFS);

        if (linkOK != GL_TRUE) {
            glDeleteProgram(tempProg);
            return 0;
        }

        // sin anda, destruimos el anterior y uso el nuevo
        destroy();
        programID = tempProg;

        outMsgs.push_back("Shaders cargados y enlazados correctamente: " + baseName +
                          " (program= " + std::to_string(programID) + ")");
        return programID;
    }

}


