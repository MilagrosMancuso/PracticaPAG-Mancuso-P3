
//
// Created by Mili on 02/10/2025.
//

#ifndef PRACTICA1PAG_SHARDERPROGRAM_H
#define PRACTICA1PAG_SHARDERPROGRAM_H
#include <string>
#include <vector>
#include <glad/glad.h>

namespace PAG {
    class ShaderProgram {
    public:
        ShaderProgram();
        ~ShaderProgram();

        GLuint loadFromBaseName(const std::string &baseName, std::vector<std::string> &outMsgs);

        void destroy();

        std::string loadFileToString(const std::string &filename, bool &ok, std::string &err);
        void checkCompileErrors(GLuint obj, const std::string &type, std::vector<std::string> &outMsgs);

    private:
        std::string loadSharderSource(const std::string &baseName);
        void checkShaderError(GLuint obj, const std::string &type, std::vector<std::string> &outMsgs);

        GLuint programID = 0;
        GLuint vs = 0;
        GLuint fs = 0;
    };
}


#endif //PRACTICA1PAG_SHARDERPROGRAM_H


