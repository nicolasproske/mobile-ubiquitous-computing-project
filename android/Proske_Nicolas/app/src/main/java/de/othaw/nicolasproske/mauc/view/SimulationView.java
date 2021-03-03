package de.othaw.nicolasproske.mauc.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import de.othaw.nicolasproske.mauc.MainActivity;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @author googlearchive (The Android Open Source Project)
 * @version 20.06.2020
 */
@SuppressLint("ViewConstructor")
public final class SimulationView extends FrameLayout implements SensorEventListener {

    // Diameter of the balls in meters
    private final static float S_BALL_DIAMETER = 0.0025f;
    private final MainActivity mainActivity;
    private final static float S_BALL_DIAMETER_2 = S_BALL_DIAMETER * S_BALL_DIAMETER;
    // Last update time
    private long lastTimeStamp;

    // Current count of scored balls
    private int score = 0;

    // Drawed circle where the balls have to get in
    private Paint paintCircle;
    private float paintCircleX, paintCircleY;
    private float paintCircleRadius = 80f;

    // Width and height of the ball
    private int particleWidth;
    private int particleHeight;

    private float metersToPixelsX;
    private float metersToPixelsY;

    // Origin of the screen relative to the origin of the bitmap
    private float originX;
    private float originY;
    private float horizontalBound;
    private float verticalBound;

    /**
     * Instantiates a new Simulation view and set the default values.
     *
     * @param context      the context
     * @param mainActivity the main activity
     */
    public SimulationView(final @NonNull Context context, final MainActivity mainActivity) {
        super(context);
        this.mainActivity = mainActivity;

        // Set values of circle
        paintCircle = new Paint();
        paintCircle.setAntiAlias(true); // Let the circle look smoother
        paintCircle.setColor(Color.parseColor("#2b2b2b")); // Color of the circle

        // Display values of current phone
        final DisplayMetrics metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float dpiX = metrics.xdpi;
        final float dpiY = metrics.ydpi;

        // Convert meters to pixels
        metersToPixelsX = dpiX / 0.0254f;
        metersToPixelsY = dpiY / 0.0254f;

        // Rescale the ball so it's about 0.5 cm on screen
        particleWidth = (int) (S_BALL_DIAMETER * metersToPixelsX + 0.5f);
        particleHeight = (int) (S_BALL_DIAMETER * metersToPixelsY + 0.5f);
    }

    /**
     * Stop simulation.
     */
    public void stopSimulation() {
        mainActivity.getSensorManager().unregisterListener(this);
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        // compute the origin of the screen relative to the origin of the bitmap
        originX = (width - particleWidth) * 0.5f;
        originY = (height - particleHeight) * 0.5f;
        horizontalBound = ((width / metersToPixelsX - S_BALL_DIAMETER) * 0.5f);
        verticalBound = ((height / metersToPixelsY - S_BALL_DIAMETER) * 0.5f);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int i) {

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        /*
         * Compute the new position of our object, based on accelerometer
         * data and present time.
         */
        final long now = System.currentTimeMillis();

        // Get mouse acceleration
        final float sx = mainActivity.getMouseXAcceleration();
        final float sy = mainActivity.getMouseYAcceleration();

        // Update balls with current timestamp and given mouse acceleration
        mainActivity.getParticleManager().update(sx, sy, now);

        final float xc = originX;
        final float yc = originY;
        final float xs = metersToPixelsX;
        final float ys = metersToPixelsY;

        final int particleCount = mainActivity.getParticleManager().getParticleCount();

        for (int i = 0; i < particleCount; i++) {
            /*
             * We transform the canvas so that the coordinate system matches
             * the sensors coordinate system with the origin in the center
             * of the screen and the unit is the meter.
             */
            final float x = xc + mainActivity.getParticleManager().getPosX(i) * xs;
            final float y = yc - mainActivity.getParticleManager().getPosY(i) * ys;

            mainActivity.getParticleManager().getBalls().get(i).setTranslationX(x);
            mainActivity.getParticleManager().getBalls().get(i).setTranslationY(y);
        }

        // If current circle coordinated aren't set, set them in the middle of the screen
        if (paintCircleX == 0f || paintCircleY == 0f) {
            paintCircleX = getWidth() / 2f;
            paintCircleY = getHeight() / 2f;
        }

        // Draw circle where balls have to get in
        canvas.drawCircle(paintCircleX, paintCircleY, paintCircleRadius, paintCircle);

        // Make sure to redraw
        postInvalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final boolean value = super.onTouchEvent(event);

        switch (event.getAction()) {

            // Check if mouse key was pressed, if so return true so event will not be canceled
            case MotionEvent.ACTION_DOWN: {
                return true;
            }

            // Re-move circle to the position of the cursor if mouse key was pressed in inner circle
            case MotionEvent.ACTION_MOVE: {

                // x-/y-coordinate of cursor
                final float x = event.getX();
                final float y = event.getY();

                final double dx = Math.pow(x - paintCircleX, 2);
                final double dy = Math.pow(y - paintCircleY, 2);

                // Check if mouse key was pressed in inner circle
                if (dx + dy < Math.pow(paintCircleRadius, 2)) {
                    // Update positions of circle to cursor position
                    paintCircleX = x;
                    paintCircleY = y;

                    // Make sure to redraw
                    postInvalidate();
                }

                return true;
            }
        }
        return value;
    }


    /**
     * Gets ball diameter.
     *
     * @return the ball diameter
     */
    public float getsBallDiameter() {
        return S_BALL_DIAMETER;
    }

    /**
     * Gets ball diameter 2.
     *
     * @return the ball diameter 2
     */
    public float getsBallDiameter2() {
        return S_BALL_DIAMETER_2;
    }

    /**
     * Gets particle width.
     *
     * @return the particle width
     */
    public int getParticleWidth() {
        return particleWidth;
    }

    /**
     * Gets particle height.
     *
     * @return the particle height
     */
    public int getParticleHeight() {
        return particleHeight;
    }

    /**
     * Gets last time stamp.
     *
     * @return the last time stamp
     */
    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    /**
     * Sets last time stamp.
     *
     * @param lastTimeStamp the last time stamp
     */
    public void setLastTimeStamp(final long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    /**
     * Gets horizontal bound.
     *
     * @return the horizontal bound
     */
    public float getHorizontalBound() {
        return horizontalBound;
    }

    /**
     * Gets vertical bound.
     *
     * @return the vertical bound
     */
    public float getVerticalBound() {
        return verticalBound;
    }

    /**
     * Gets circle radius.
     *
     * @return the circle radius
     */
    public float getPaintCircleRadius() {
        return paintCircleRadius;
    }

    /**
     * Sets circle radius.
     *
     * @param paintCircleRadius the circle radius
     */
    public void setPaintCircleRadius(float paintCircleRadius) {
        this.paintCircleRadius = paintCircleRadius;
    }

    /**
     * Gets circle x position.
     *
     * @return the circle x position
     */
    public float getPaintCircleX() {
        return paintCircleX;
    }

    /**
     * Gets circle y position.
     *
     * @return the circle y position
     */
    public float getPaintCircleY() {
        return paintCircleY;
    }

    /**
     * Gets current count of scored balls.
     *
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets current count of scored balls.
     *
     * @param score the score
     */
    public void setScore(int score) {
        this.score = score;
    }
}
