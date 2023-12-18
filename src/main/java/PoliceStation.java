import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PoliceStation {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String MULTA_TOPIC = "multa";
    private MqttClient client;
    private Jedis jedis;

    public PoliceStation() throws MqttException {
        client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
        jedis = new Jedis("redis://localhost:6379");
    }

    public void enviarMultas() throws MqttException {
        Set<String> claves = jedis.keys("EXCESO:*");
        for (String clave : claves) {
            int velocidad = Integer.parseInt(jedis.get(clave));
            int multa = calcularMulta(velocidad);
            String matricula = clave.split(":")[2];
            String payload = matricula + ":" + multa;
            MqttMessage message = new MqttMessage(payload.getBytes());
            client.publish(MULTA_TOPIC, message);
            System.out.println("Multa enviada. Matrícula: " + matricula + ", Multa: " + multa);
            jedis.del(clave);
            jedis.sadd("VEHICULOSDENUNCIADOS", matricula);
        }
    }

    private int calcularMulta(int velocidad) {
        int exceso = velocidad - 80;
        if (exceso <= 8) { // 10% - 20%
            return 100;
        } else if (exceso <= 16) { // 20% - 30%
            return 200;
        } else { // > 30%
            return 500;
        }
    }

    public void mostrarEstadisticas() {
        long totalVehiculos = jedis.scard("VEHICULOS");
        long totalDenunciados = jedis.scard("VEHICULOSDENUNCIADOS");
        double porcentajeDenunciados = (double) totalDenunciados / totalVehiculos * 100;
        System.out.println("Total vehículos: " + totalVehiculos);
        System.out.println("Porcentaje denunciados: " + porcentajeDenunciados + "%");
    }

    public void iniciarEstadisticas() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                enviarMultas();
                mostrarEstadisticas();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        try {
            PoliceStation policeStation = new PoliceStation();
            policeStation.iniciarEstadisticas();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}