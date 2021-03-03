package de.othaw.nicolasproske.mauc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.othaw.nicolasproske.mauc.manager.AudioManager;
import de.othaw.nicolasproske.mauc.manager.MQTTManager;
import de.othaw.nicolasproske.mauc.manager.ParticleManager;
import de.othaw.nicolasproske.mauc.object.Particle;
import de.othaw.nicolasproske.mauc.view.SimulationView;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @author googlearchive (The Android Open Source Project)
 * @version 20.06.2020
 */
public final class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private SensorManager sensorManager;
    private WindowManager windowManager;
    private PowerManager powerManager;

    private AudioManager audioManager;
    private MQTTManager mqttManager;
    private ParticleManager particleManager;

    private SimulationView simulationView;
    private Particle particle;

    private Display display;
    private PowerManager.WakeLock wakeLock;

    private float mouseXAcceleration, mouseYAcceleration;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Add toolbar to view
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /*
         * VIEWS
         */
        this.simulationView = new SimulationView(this, this);
        this.particle = new Particle(this, this);

        simulationView.setBackgroundResource(R.drawable.wood);

        /*
         * MANAGER
         */
        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        this.powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        this.audioManager = new AudioManager(this);
        this.mqttManager = new MQTTManager(this);
        this.particleManager = new ParticleManager(this);

        display = windowManager.getDefaultDisplay();
        wakeLock = powerManager.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, getClass().getName());

        // Layouts for simulation view
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 155, 0, 0);

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.content_main);
        coordinatorLayout.addView(simulationView, layoutParams);

        // Floating action button to resize hole of simulation view
        final FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view -> {
            // By clicking resize the radius of the circle by added 10.0
            simulationView.setPaintCircleRadius(simulationView.getPaintCircleRadius() + 10f);
            view.postInvalidate();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable/Disable sound
        if (sharedPreferences.getBoolean("sound", true)) {
            audioManager.unmute();
        } else {
            audioManager.mute();
        }

        // Connect to MQTT broker
        mqttManager.connect();

        // Subscribe to connected MQTT broker
        mqttManager.subscribe();

        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
        wakeLock.acquire(10 * 60 * 1000L /* 10 minutes timeout */);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disconnect from broker
        mqttManager.disconnect();

        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation
        simulationView.stopSimulation();

        // and release our wake-lock
        wakeLock.release();
    }

    /**
     * Gets mqtt manager.
     *
     * @return the mqtt manager
     */
    public MQTTManager getMqttManager() {
        return mqttManager;
    }

    /**
     * Gets particle manager.
     *
     * @return the particle manager
     */
    public ParticleManager getParticleManager() {
        return particleManager;
    }

    /**
     * Gets sensor manager.
     *
     * @return the sensor manager
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    /**
     * Gets simulation view.
     *
     * @return the simulation view
     */
    public SimulationView getSimulationView() {
        return simulationView;
    }

    /**
     * Gets mouse x acceleration.
     *
     * @return the mouse x acceleration
     */
    public float getMouseXAcceleration() {
        return mouseXAcceleration;
    }

    /**
     * Sets mouse x acceleration.
     *
     * @param mouseXAcceleration the mouse x acceleration
     */
    public void setMouseXAcceleration(float mouseXAcceleration) {
        this.mouseXAcceleration = mouseXAcceleration;
    }

    /**
     * Gets mouse y acceleration.
     *
     * @return the mouse y acceleration
     */
    public float getMouseYAcceleration() {
        return mouseYAcceleration;
    }

    /**
     * Sets mouse y acceleration.
     *
     * @param mouseYAcceleration the mouse y acceleration
     */
    public void setMouseYAcceleration(float mouseYAcceleration) {
        this.mouseYAcceleration = mouseYAcceleration;
    }

    /**
     * Gets shared preferences.
     *
     * @return the shared preferences
     */
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}