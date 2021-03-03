package de.othaw.nicolasproske.mauc.manager;

import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

import de.othaw.nicolasproske.mauc.MainActivity;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @author Prof. Dr.-Ing. Ulrich Schäfer
 * @version 20.06.2020
 */
public final class MQTTManager {

    private final MainActivity mainActivity;

    private MqttClient client;
    private final MemoryPersistence persistence;

    private final String tag;
    private String broker;
    private String sub_topic;
    private String pub_topic;

    /**
     * Instantiates a new Mqtt manager.
     * Get saved SharedPreferences from settings.
     *
     * @param mainActivity the main activity
     */
    public MQTTManager(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        /*
         * Set broker with value which is saved in a shared preference of the settings menu.
         * If there is no shared preference set predefined default values
         */
        this.broker = "tcp://" + mainActivity.getSharedPreferences().getString("broker_ip", "192.168.2.76") + ":1883";
        this.sub_topic = mainActivity.getSharedPreferences().getString("broker_sub_topic", "StA/data");
        this.pub_topic = mainActivity.getSharedPreferences().getString("broker_pub_topic", "StA/message");

        // Set memory persistence
        this.persistence = new MemoryPersistence();

        this.tag = mainActivity.getClass().getSimpleName();
    }

    /**
     * Connect to the broker.
     */
    public void connect() {
        // Update broker-ip and sub-topic with values set in the settings menu
        this.broker = "tcp://" + mainActivity.getSharedPreferences().getString("broker_ip", "192.168.2.76") + ":1883";
        this.sub_topic = mainActivity.getSharedPreferences().getString("broker_sub_topic", "StA/data");
        this.pub_topic = mainActivity.getSharedPreferences().getString("broker_pub_topic", "StA/message");

        try {
            // Generate unique clientId
            final String clientId = MqttClient.generateClientId();

            // Create new MQTT client with generated clientId
            client = new MqttClient(broker, clientId, persistence);

            // Set timeout if can not connect to broker (throws IllegalArgumentException)
            client.setTimeToWait(5 * 1000);

            final MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            Log.d(tag, "Connecting to broker: " + broker);
            client.connect(connectOptions); // Connect to broker
            Log.d(tag, "Connected to broker: " + broker);

            // Display successful connected to the user
            Snackbar.make(mainActivity.getSimulationView(), "Connected to broker: " + broker, Snackbar.LENGTH_LONG).show();
        } catch (final MqttException e) {
            Snackbar.make(mainActivity.getSimulationView(), "Could not connect to broker with ip " + broker, Snackbar.LENGTH_LONG).show();
            Log.e(tag, "Reason: " + e.getReasonCode());
            Log.e(tag, "Message: " + e.getMessage());
            Log.e(tag, "LocalizedMsg: " + e.getLocalizedMessage());
            Log.e(tag, "Cause: " + e.getCause());
            Log.e(tag, "Exception: " + e);
        }
    }

    /**
     * Subscribe to specific sub-topic.
     */
    public void subscribe() {
        // MQTT quality of service level
        final int qos = 0;

        try {
            // Subscribe to sub-topic and convert got message with help of the split method to the x-/y mouse acceleration
            client.subscribe(sub_topic, qos, (topic, msg) -> {
                mainActivity.setMouseXAcceleration(Float.parseFloat(new String(msg.getPayload()).split(",")[0]));
                mainActivity.setMouseYAcceleration(Float.parseFloat(new String(msg.getPayload()).split(",")[1]));
            });
        } catch (final MqttException e) {
            e.printStackTrace();
        }

        // Display successful connected to the user
        Toast.makeText(mainActivity, "Subscribed to topic " + sub_topic, Toast.LENGTH_SHORT).show();
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
        try {
            // Unsubscribe from sub-topic
            client.unsubscribe(sub_topic);
            Log.d(tag, "Unsubscribed from topic " + sub_topic);
            client.disconnect();
            Log.d(tag, "Disconnected from broker " + broker);
        } catch (final MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publish data to specific sub-topic.
     *
     * @param payload the payload
     *                This is the message to be published
     */
    public void publish(final String payload) {
        final byte[] encodedPayload;
        try {
            // Get bytes and convert them to UTF-8 standard charset
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);

            // Convert bytes to a MqttMessage which will be sent to the broker
            final MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);

            // Send message to the broker with specific sub-topic
            client.publish(pub_topic, message);

            Log.d(tag, "Published to " + pub_topic + ": " + message);
        } catch (final MqttException e) {
            e.printStackTrace();
        }
    }
}
