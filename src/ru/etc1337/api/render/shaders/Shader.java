package ru.etc1337.api.render.shaders;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import ru.etc1337.api.interfaces.QuickImports;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL20.*;

public class Shader implements QuickImports {

    public static boolean FLAT_RENDERER;
    private final int id;
    private boolean isValid = false;

    public Shader() {
        int program = glCreateProgram();

        int fragmentShader = getShader();
        int vertexShader = getVertex();

        if (fragmentShader == 0 || vertexShader == 0) {
            System.err.println("Failed to create shaders");
            this.id = 0;
            return;
        }

        glAttachShader(program, fragmentShader);
        glAttachShader(program, vertexShader);
        glLinkProgram(program);

        // Проверка линковки программы
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program linking failed: " + glGetProgramInfoLog(program));
            glDeleteShader(fragmentShader);
            glDeleteShader(vertexShader);
            glDeleteProgram(program);
            this.id = 0;
            return;
        }

        // Валидация программы
        glValidateProgram(program);
        if (glGetProgrami(program, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println("Shader program validation failed: " + glGetProgramInfoLog(program));
        }

        // Очистка шейдеров после линковки
        glDetachShader(program, fragmentShader);
        glDetachShader(program, vertexShader);
        glDeleteShader(fragmentShader);
        glDeleteShader(vertexShader);

        this.id = program;
        this.isValid = true;

        checkGLError("Shader constructor");
    }

    public String getCode() {
        return "";
    }

    public void start() {
        if (!isValid || id == 0) {
            System.err.println("Attempting to use invalid shader program");
            return;
        }
        glUseProgram(id);
        checkGLError("glUseProgram");
    }

    public void finish() {
        glUseProgram(0);
        checkGLError("glUseProgram(0)");
    }

    public int getShader() {
        String code = getCode();
        if (code.isEmpty()) {
            System.err.println("Fragment shader code is empty");
            return 0;
        }
        return compile(new ByteArrayInputStream(code.getBytes()), GL_FRAGMENT_SHADER);
    }

    private int compile(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        String shaderSource = readInputStream(inputStream);

        if (shaderSource.isEmpty()) {
            System.err.println("Shader source is empty");
            glDeleteShader(shader);
            return 0;
        }

        glShaderSource(shader, shaderSource);
        glCompileShader(shader);

        // Проверка компиляции
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String errorLog = glGetShaderInfoLog(shader);
            String shaderTypeStr = (shaderType == GL_VERTEX_SHADER) ? "vertex" : "fragment";
            System.err.println("Failed to compile " + shaderTypeStr + " shader: " + errorLog);
            System.err.println("Shader source:\n" + shaderSource);
            glDeleteShader(shader);
            return 0;
        }

        checkGLError("compile shader");
        return shader;
    }

    public int getVertex() {
        return compile(new ByteArrayInputStream(new String(
                "#version 120\r\n"
                        + "\r\n"
                        + "void main() {\r\n"
                        + "    gl_TexCoord[0] = gl_MultiTexCoord0;\r\n"
                        + "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\r\n"
                        + "}"
        ).getBytes()), GL_VERTEX_SHADER);
    }

    public static void drawQuads(MatrixStack matrixStack, float width, float height) {
        drawQuads(matrixStack, 0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrixStack) {
        drawQuads(matrixStack, window.getScaledWidth(), window.getScaledHeight());
    }

    public static void drawQuads() {
        float width = (float) window.getScaledWidth();
        float height = (float) window.getScaledHeight();
        GL11.glBegin(GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(width, 0);
        GL11.glEnd();
    }

    public static void drawQuads(MatrixStack matrixStack, float x, float y, float width, float height) {
        if (!FLAT_RENDERER) {
            builder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }

        builder.pos(matrixStack.getLast().getMatrix(), x, y, 0).tex(0, 0).endVertex();
        builder.pos(matrixStack.getLast().getMatrix(), x, y + height, 0).tex(0, 1).endVertex();
        builder.pos(matrixStack.getLast().getMatrix(), x+width, y + height, 0).tex(1, 1).endVertex();
        builder.pos(matrixStack.getLast().getMatrix(), x+width, y, 0).tex(1, 0).endVertex();

        if (!FLAT_RENDERER) {
            tessellator.draw();
        }
    }

    public static void drawScaledQuads() {
        drawQuadsESP(0.0, 0.0, window.getScaledWidth(), window.getScaledHeight());
    }

    public static void drawQuadsESP(final double x, final double y, final double width, final double height) {
        GL11.glBegin(GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F);
        if (mc.getGameSettings().fullscreen || window.getHeight() < 847) {
            GL11.glVertex2d(x, y + height);
            GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2d(x + width, y + height);
        }else {
            GL11.glVertex2d(x, y + height - 0.5f);
            GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2d(x + width, y + height - 0.5f);
        }
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public void setInt(String name, int... args) {
        if (!isValid || id == 0) return;

        int loc = glGetUniformLocation(id, name);
        if (loc == -1) {
            // Uniform не найден - это нормально для оптимизированных шейдеров
            return;
        }

        try {
            if (args.length > 1) {
                GL20.glUniform2i(loc, args[0], args[1]);
            } else {
                GL20.glUniform1i(loc, args[0]);
            }
            checkGLError("setInt " + name);
        } catch (Exception e) {
            System.err.println("Error setting int uniform " + name + ": " + e.getMessage());
        }
    }

    public void setFloat(String name, float... args) {
        if (!isValid || id == 0) return;

        int loc = glGetUniformLocation(id, name);
        if (loc == -1) {
            // Uniform не найден - это нормально для оптимизированных шейдеров
            return;
        }

        try {
            switch (args.length) {
                case 1:
                    glUniform1f(loc, args[0]);
                    break;
                case 2:
                    glUniform2f(loc, args[0], args[1]);
                    break;
                case 3:
                    glUniform3f(loc, args[0], args[1], args[2]);
                    break;
                case 4:
                    glUniform4f(loc, args[0], args[1], args[2], args[3]);
                    break;
                default:
                    System.err.println("Unsupported number of float arguments: " + args.length);
                    return;
            }
            checkGLError("setFloat " + name);
        } catch (Exception e) {
            System.err.println("Error setting float uniform " + name + ": " + e.getMessage());
        }
    }

    public int getInt(String name) {
        if (!isValid || id == 0) return -1;
        return glGetUniformLocation(id, name);
    }

    public boolean isValid() {
        return isValid && id != 0;
    }

    public void cleanup() {
        if (id != 0) {
            glDeleteProgram(id);
            checkGLError("cleanup shader program");
        }
        isValid = false;
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (framebuffer == null)
            return new Framebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), depth);

        if (needsNewFramebuffer(framebuffer)) {
            framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer.framebufferWidth != window.getFramebufferWidth() || framebuffer.framebufferHeight != window.getFramebufferHeight();
    }

    public String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    // Утилита для проверки ошибок OpenGL
    public static void checkGLError(String operation) {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM: errorString = "GL_INVALID_ENUM"; break;
                case GL_INVALID_VALUE: errorString = "GL_INVALID_VALUE"; break;
                case GL_INVALID_OPERATION: errorString = "GL_INVALID_OPERATION"; break;
                case GL_OUT_OF_MEMORY: errorString = "GL_OUT_OF_MEMORY"; break;
                default: errorString = "Unknown error " + error; break;
            }
            System.err.println("OpenGL error during " + operation + ": " + errorString + " (" + error + ")");

            // Для AMD карт полезно вывести дополнительную информацию
            if (error == GL_INVALID_OPERATION) {
                System.err.println("GL_INVALID_OPERATION often occurs on AMD cards due to:");
                System.err.println("- Invalid shader program state");
                System.err.println("- Incorrect uniform usage");
                System.err.println("- Context issues");
            }
        }
    }
}