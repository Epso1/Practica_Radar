import org.eclipse.paho.client.mqttv3.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CarSimulator {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String MATRICULA_TOPIC = "matricula";
    private static final String MULTA_TOPIC = "multa";
    private MqttClient client;

    public CarSimulator() throws MqttException {
        client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {}

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(MULTA_TOPIC)) {
                    String[] datos = new String(message.getPayload()).split(":");
                    String matricula = datos[0];
                    int multa = Integer.parseInt(datos[1]);
                    System.out.println("Multa recibida. Matrícula: " + matricula + ", Multa: " + multa);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
    }

    public void enviarVelocidad() throws MqttException {
        Random rand = new Random();
        int velocidad = rand.nextInt(81) + 60; // Velocidad entre 60 y 140
        String matricula = generarMatricula();
        String payload = matricula + ":" + velocidad;
        MqttMessage message = new MqttMessage(payload.getBytes());
        client.publish(MATRICULA_TOPIC, message);
    }

    private String generarMatricula() {
        Random rand = new Random();
        int numPart = rand.nextInt(9000) + 1000; // 4 digitos
        char[] letras = new char[3];
        for (int i = 0; i < 3; i++) {
            letras[i] = (char) (rand.nextInt(26) + 'A'); // 3 letras
        }
        return numPart + new String(letras);
    }

    public void iniciarSimulacion() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                enviarVelocidad();
                client.subscribe(MULTA_TOPIC);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        try {
            CarSimulator carSimulator = new CarSimulator();
            carSimulator.iniciarSimulacion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}