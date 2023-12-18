
Practica_Radar

Descripción
Practica_Radar es una aplicación Java que simula un sistema de radar de tráfico. El sistema consta de tres componentes principales: un simulador de coches, un radar y una estación de policía. El simulador de coches genera coches con velocidades aleatorias y matrículas, el radar detecta los coches y registra si están excediendo el límite de velocidad, y la estación de policía emite multas a los coches que exceden el límite de velocidad.

Requisitos
Java 18 o superior
Maven
Servidor MQTT (como Mosquitto)
Servidor Redis

Instalación
Verifique que tiene instalado Java 18 o superior en su sistema con el comando java -version.
Verifique que tiene instalado Maven en su sistema con el comando mvn -version.
Asegúrese de tener instalado un servidor MQTT como Mosquitto y un servidor Redis en su sistema.
Clone el repositorio de Practica_Radar en su sistema local con el comando git clone <url_del_repositorio>.
Navegue al directorio del proyecto con el comando cd Practica_Radar.
Compile el proyecto con el comando mvn clean install.

Ejecución
Asegúrese de que los servidores MQTT y Redis están en ejecución.
Ejecute las clases CarSimulator, Radar y PoliceStation en tres terminales diferentes desde el botón "run" del IDE o con los siguientes comandos:
java -cp target/Practica_Radar-1.0-SNAPSHOT.jar org.example.CarSimulator
java -cp target/Practica_Radar-1.0-SNAPSHOT.jar org.example.Radar
java -cp target/Practica_Radar-1.0-SNAPSHOT.jar org.example.PoliceStation
Asegúrese de reemplazar Practica_Radar-1.0-SNAPSHOT.jar con el nombre del archivo JAR generado en su proyecto.
Observe la salida en cada terminal para verificar que las aplicaciones están funcionando correctamente.

Funcionamiento
CarSimulator: Genera coches con velocidades aleatorias entre 60 y 140 y matrículas aleatorias (4 dígitos y 3 letras). Envía esta información a través de MQTT cada segundo. También recibe multas de PoliceStation y las muestra en la pantalla.
Radar: Lee los mensajes MQTT con las velocidades de los coches. Si un coche supera el límite de velocidad de 80, crea una entrada en Redis con la clave "EXCESO:80: " y la velocidad como valor. Si un coche no supera el límite de velocidad, lo añade a un grupo de Redis llamado "VEHICULOS".
PoliceStation: Lee las claves de Redis que comienzan con "EXCESO:*", calcula la multa basándose en la velocidad, y envía la multa a través de MQTT. Una vez enviada la multa, borra la clave de Redis y añade la matrícula a un grupo de Redis llamado "VEHICULOSDENUNCIADOS". También muestra estadísticas cada segundo, incluyendo el número total de vehículos y el porcentaje de vehículos que han sido multados.
