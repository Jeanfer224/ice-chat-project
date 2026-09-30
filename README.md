# ice-chat-project
Sistema de chat multiusuario por consola construido con ZeroC Ice 3.7 y Gradle multimódulo. El servidor expone una sala de chat mediante RPC y los clientes se conectan a ella desde la terminal.

Módulos
Módulo	Descripción
common	Contrato Slice (Chat.ice) y código Java generado con slice2java (interfaces, structs, excepciones)
server	Servant ChatRoomI (thread-safe) y lanzador ServerMain, que escucha en el puerto TCP 10000
client	Cliente interactivo de consola con un hilo demonio que consulta mensajes nuevos cada 500 ms
Estructura del proyecto
ice-chat-project/
├── settings.gradle
├── build.gradle
├── common/
│   ├── build.gradle
│   └── src/main/slice/Chat.ice
├── server/
│   ├── build.gradle
│   └── src/main/java/chat/server/
│       ├── ChatRoomI.java
│       └── ServerMain.java
└── client/
    ├── build.gradle
    └── src/main/java/chat/client/
        └── ClientMain.java
Requisitos previos
JDK 17 o superior. Gradle 8.14 no funciona con Java 11.
Verifica con java --version.
Si tienes varios JDK instalados, configura JAVA_HOME apuntando al 17.
ZeroC Ice 3.7.11 para Windows, que incluye el compilador slice2java.
Descarga: https://download.zeroc.com/ice/3.7/Ice-3.7.11.msi
Instálalo con las opciones por defecto.
Verifica que la carpeta bin de la instalación esté en el Path de Windows.
Comprueba en una terminal nueva: slice2java --version (debe mostrar 3.7.11).
Conexión a internet la primera vez, para que Gradle descargue com.zeroc:ice:3.7.11 desde Maven Central.

La versión del compilador (slice2java) y la de la librería en build.gradle deben coincidir (3.7.11).

Compilación

Desde la raíz del proyecto:

bash
./gradlew build

En CMD o PowerShell: gradlew.bat build.

La tarea compileSlice genera el código Java en common/src/main/java/ChatApp/ a partir de Chat.ice, y luego se compilan los tres módulos. Debe terminar en BUILD SUCCESSFUL.

common/src/main/java/ChatApp/ es código generado y está en .gitignore. Se recrea en cada compilación, por eso slice2java debe estar instalado.

Ejecución

Abre tres terminales en la raíz del proyecto.

Terminal 1: servidor

bash
./gradlew :server:run --console=plain

Debe mostrar SERVIDOR ZEROC ICE INICIADO EXITOSAMENTE en el puerto 10000.

Terminal 2: primer cliente

bash
./gradlew :client:run --console=plain

Terminal 3: segundo cliente

bash
./gradlew :client:run --console=plain

Cada cliente pide un nickname al iniciar. El servidor debe estar encendido antes de abrir los clientes.
