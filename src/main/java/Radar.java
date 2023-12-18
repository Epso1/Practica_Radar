import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Radar {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String MATRICULA_TOPIC = "matricula";
    private MqttClient client;
    private Jedis jedis;

    public Radar() throws MqttException {
        client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {}

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(MATRICULA_TOPIC)) {
                    String[] datos = new String(message.getPayload()).split(":");
                    String matricula = datos[0];
                    int velocidad = Integer.parseInt(datos[1]);
                    System.out.println("Matrícula: " + matricula + ", Velocidad: " + velocidad);
                    if (velocidad > 80) {
                        jedis.set("EXCESO:80:" + matricula, String.valueOf(velocidad));
                    } else {
                        jedis.sadd("VEHICULOS", matricula);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
        jedis = new Jedis("redis://localhost:6379");
    }

    public void iniciarRadar() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                client.subscribe(MATRICULA_TOPIC);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        try {
            Radar radar = new Radar();
            radar.iniciarRadar();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}