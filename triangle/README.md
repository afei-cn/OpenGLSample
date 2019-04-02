# Triangle

## 一、简介

- 通过这个 Sample，你将了解到 Android 中是怎么使用 OpenGL ES
- 通过绘制一个简单的静态三角形，来简单入门和了解它大致的流程（类似于 HelloWorld 工程）
- 介绍使用 `Native 层` 和 `Java 层` 两种方式来分别实现

**运行效果：**
![](https://img-blog.csdnimg.cn/20181205102218301.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2FmZWlfXw==,size_16,color_FFFFFF,t_70)

## 二、Native 实现

### 1. 头文件
由于我们使用的是 OpenGL ES 3.0，所以主要使用吃头文件：`<GLES3/gl3.h>`

### 2. Activity
最终还是要显示在 Activity 上的，所以我们先准备这样一个 Activity，它直接使用 `GLSurfaceView` 作为 contentView。

```java
public class SampleActivity extends AppCompatActivity {

    private static final String TAG = "SampleActivity";
    public static final String TYPE_NAME = "type";
    public static final int TYPE_NATIVE = 0;
    public static final int TYPE_JAVA = 1;

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkOpenGLES30()) {
            Log.e(TAG, "con't support OpenGL ES 3.0!");
            finish();
        }
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3);
        mGlSurfaceView.setRenderer(getRenderer());
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGlSurfaceView);
    }

    private GLSurfaceView.Renderer getRenderer() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_NAME, TYPE_NATIVE);
        Log.d(TAG, "type: " + type);
        GLSurfaceView.Renderer renderer;
        if (type == TYPE_NATIVE) {
            renderer = new NativeRenderer(this);
        } else {
            renderer = new JavaRenderer(this);
        }
        return renderer;
    }

    private boolean checkOpenGLES30() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onPause() {
        mGlSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGlSurfaceView.onResume();
        super.onResume();
    }
}
```

### 3. Renderer
我们先介绍 `NativeRenderer` 的实现，如下：

```java
public class NativeRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    static {
        System.loadLibrary("native-renderer");
    }

    public NativeRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        registerAssetManager(mContext.getAssets());
        glInit();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glResize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glDraw();
    }

    public native void registerAssetManager(AssetManager assetManager);
    public native void glInit();
    public native void glResize(int width, int height);
    public native void glDraw();
}
```

主要定义了4个 `native` 的方法，需要我们在 native 层实现。

### 4. ShaderUtils
说实现之前，我们先写一个工具类，负责加载和创建。工具类的作用就是可以重复使用的：

```cpp
#include "ShaderUtils.h"
#include <stdlib.h>
#include "LogUtils.h"

GLuint LoadShader(GLenum type, const char *shaderSource) {
    // 1. create shader
    GLuint shader = glCreateShader(type);
    if (shader == GL_NONE) {
        LOGE("create shader failed! type: %d", type);
        return GL_NONE;
    }
    // 2. load shader source
    glShaderSource(shader, 1, &shaderSource, NULL);
    // 3. compile shared source
    glCompileShader(shader);
    // 4. check compile status
    GLint compiled;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (compiled == GL_NONE) { // compile failed
        GLint len = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
        if (len > 1) {
            char *log = static_cast<char *>(malloc(sizeof(char) * len));
            glGetShaderInfoLog(shader, len, NULL, log);
            LOGE("Error compiling shader: %s", log);
            free(log);
        }
        glDeleteShader(shader); // delete shader
        return GL_NONE;
    }
    return shader;
}

GLuint CreateProgram(const char *vertexSource, const char *fragmentSource) {
    // 1. load shader
    GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexSource);
    if (vertexShader == GL_NONE) {
        LOGE("load vertex shader failed! ");
        return GL_NONE;
    }
    GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (vertexShader == GL_NONE) {
        LOGE("load fragment shader failed! ");
        return GL_NONE;
    }
    // 2. create gl program
    GLuint program = glCreateProgram();
    if (program == GL_NONE) {
        LOGE("create program failed! ");
        return GL_NONE;
    }
    // 3. attach shader
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    // we can delete shader after attach
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    // 4. link program
    glLinkProgram(program);
    // 5. check link status
    GLint linked;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (linked == GL_NONE) { // link failed
        GLint len = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &len);
        if (len > 1) {
            char *log = static_cast<char *>(malloc(sizeof(char) * len));
            glGetProgramInfoLog(program, len, NULL, log);
            LOGE("Error link program: %s", log);
            free(log);
        }
        glDeleteProgram(program); // delete program
        return GL_NONE;
    }
    return program;
}

char *readAssetFile(const char *filename, AAssetManager *mgr) {
    if (mgr == NULL) {
        LOGE("pAssetManager is null!");
        return NULL;
    }
    AAsset *pAsset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    off_t len = AAsset_getLength(pAsset);
    char *pBuffer = (char *) malloc(len + 1);
    pBuffer[len] = '\0';
    int numByte = AAsset_read(pAsset, pBuffer, len);
    LOGD("numByte: %d, len: %d", numByte, len);
    AAsset_close(pAsset);
    return pBuffer;
}
```

### 5. NativeRenderer.cpp
终于到了我们的渲染实现了，主要就是实现之前定义的那几个 `native` 方法：

```cpp
#include "com_afei_openglsample_NativeRenderer.h"

#include <android/asset_manager_jni.h>
#include <GLES3/gl3.h>
#include "LogUtils.h"
#include "ShaderUtils.h"

GLuint g_program;
GLint g_position_handle;
AAssetManager *g_pAssetManager = NULL;

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glInit
        (JNIEnv *env, jobject instance) {
    char *vertexShaderSource = readAssetFile("vertex.vsh", g_pAssetManager);
    char *fragmentShaderSource = readAssetFile("fragment.fsh", g_pAssetManager);
    g_program = CreateProgram(vertexShaderSource, fragmentShaderSource);
    if (g_program == GL_NONE) {
        LOGE("gl init failed!");
    }
    // vPosition 是在 'vertex.vsh' 文件中定义的
    GLint g_position_handle =glGetAttribLocation(g_program, "vPosition");
    LOGD("g_position_handle: %d", g_position_handle);
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 背景颜色设置为黑色 RGBA (range: 0.0 ~ 1.0)
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glResize
        (JNIEnv *env, jobject instance, jint width, jint height) {
    glViewport(0, 0, width, height); // 设置视距窗口
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glDraw
        (JNIEnv *env, jobject instance) {
    GLint vertexCount = 3;
    // OpenGL的世界坐标系是 [-1, -1, 1, 1]
    GLfloat vertices[] = {
            0.0f, 0.5f, 0.0f, // 第一个点（x, y, z）
            -0.5f, -0.5f, 0.0f, // 第二个点（x, y, z）
            0.5f, -0.5f, 0.0f // 第三个点（x, y, z）
    };
    glClear(GL_COLOR_BUFFER_BIT); // clear color buffer
    // 1. 选择使用的程序
    glUseProgram(g_program);
    // 2. 加载顶点数据
    glVertexAttribPointer(g_position_handle, vertexCount, GL_FLOAT, GL_FALSE, 3 * 4, vertices);
    glEnableVertexAttribArray(g_position_handle);
    // 3. 绘制
    glDrawArrays(GL_TRIANGLES, 0, vertexCount);
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_registerAssetManager
        (JNIEnv *env, jobject instance, jobject assetManager) {
    if (assetManager) {
        g_pAssetManager = AAssetManager_fromJava(env, assetManager);
    } else {
        LOGE("assetManager is null!")
    }
}
```

### 6. CMakeLists.txt
当然，它也是或不可少的，负责编译我们的 native 动态库。

```
cmake_minimum_required(VERSION 3.4.1)

include_directories( ${CMAKE_SOURCE_DIR}/src/main/cpp/inc )

add_library( native-renderer
             SHARED
             src/main/cpp/src/ShaderUtils.cpp
             src/main/cpp/src/com_afei_openglsample_NativeRenderer.cpp )

target_link_libraries( native-renderer
                       # for 'AAssetManager_fromJava'
                       android
                       # for opengl es 3.0 library
                       GLESv3
                       # for log library
                       log )
```

### 7. vertex.vsh 和 fragment.fsh
我们将顶点着色器和片元着色器的代码放在了 `assets` 目录下，实现分别为：

#### vertex.vsh
第一行是声明使用的版本，这里我们只是简单的将外面的输入，直接传给了 `gl_Position`
```
#version 300 es

layout(location = 0) in vec4 vPosition;

void main() {
    gl_Position = vPosition;
}
```

#### fragment.fsh
同样第一行是声明使用的版本，然后绘制的颜色直接使用红色，即 RGBA (range: 0.0 ~ 1.0)
```
#version 300 es

precision mediump float;
out vec4 fragColor;

void main() {
    fragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );
}
```

## 三、Java 实现

### 1. Activity
和 Native 实现的代码一样，唯一的区别是使用 `JavaRenderer` 类作为渲染。

### 2. JavaRenderer
代码来看其实和 Native 层的极其类似，毕竟只是使用 Java 包了一层。

```java
public class JavaRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "JavaRenderer";
    private Context mContext;
    private int mProgram;
    private int mPositionHandle;

    public JavaRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexSource = ShaderUtils.loadFromAssets("vertex.vsh", mContext.getResources());
        String fragmentSource = ShaderUtils.loadFromAssets("fragment.fsh", mContext.getResources());
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);
        // vPosition 是在 'vertex.vsh' 文件中定义的
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        Log.d(TAG, "mPositionHandle: " + mPositionHandle);
        // 背景颜色设置为黑色 RGBA (range: 0.0 ~ 1.0)
        GLES30.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 视距区域设置使用 GLSurfaceView 的宽高
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        int vertexCount = 3;
        // OpenGL的世界坐标系是 [-1, -1, 1, 1]
        float[] vertices = new float[]{
                0.0f, 0.5f, 0, // 第一个点（x, y, z）
                -0.5f, -0.5f, 0, // 第二个点（x, y, z）
                0.5f, -0.5f, 0 // 第三个点（x, y, z）
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); // 一个 float 是四个字节
        vbb.order(ByteOrder.nativeOrder()); // 必须要是 native order
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0); // 这一行不要漏了

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); // clear color buffer
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram);
        // 2. 加载顶点数据
        GLES30.glVertexAttribPointer(mPositionHandle, vertexCount, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // 3. 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
    }

}
```

### 3. ShaderUtils
同样，我们将公共的方法抽离为一个工具类，并且，代码也是和 Native 层的极其类似。

```java
public class ShaderUtils {

    public static final String TAG = "ShaderUtils";

    public static int loadShader(int type, String source) {
        // 1. create shader
        int shader = GLES30.glCreateShader(type);
        if (shader == GLES30.GL_NONE) {
            Log.e(TAG, "create shared failed! type: " + type);
            return GLES30.GL_NONE;
        }
        // 2. load shader source
        GLES30.glShaderSource(shader, source);
        // 3. compile shared source
        GLES30.glCompileShader(shader);
        // 4. check compile status
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GLES30.GL_NONE) { // compile failed
            Log.e(TAG, "Error compiling shader. type: " + type + ":");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader); // delete shader
            shader = GLES30.GL_NONE;
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        // 1. load shader
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == GLES30.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ");
            return GLES30.GL_NONE;
        }
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == GLES30.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ");
            return GLES30.GL_NONE;
        }
        // 2. create gl program
        int program = GLES30.glCreateProgram();
        if (program == GLES30.GL_NONE) {
            Log.e(TAG, "create program failed! ");
            return GLES30.GL_NONE;
        }
        // 3. attach shader
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        // we can delete shader after attach
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        // 4. link program
        GLES30.glLinkProgram(program);
        // 5. check link status
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES30.GL_NONE) { // link failed
            Log.e(TAG, "Error link program: ");
            Log.e(TAG, GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program); // delete program
            return GLES30.GL_NONE;
        }
        return program;
    }

    public static String loadFromAssets(String fileName, Resources resources) {
        String result = null;
        try {
            InputStream is = resources.getAssets().open(fileName);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            is.close();
            result = new String(data, "UTF-8");
            result.replace("\\r\\n", "\\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
```

### 4. vertex.vsh 和 fragment.fsh
同 Native 实现。

### 5. 运行效果
同 Native 运行效果。

## 四、总结

- Native 实现方式和 Java 实现方式原理都是一样的，包括方法名都基本是一样的
- 部分未给出的代码，详细参见工程中的代码

## 五、博客地址

[Android 为例编写一个 OpenGL ES 3.0 实例，Native & Java 两种实现](https://blog.csdn.net/afei__/article/details/84821570)
