package de.othaw.nicolasproske.mauc.manager;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.othaw.nicolasproske.mauc.MainActivity;
import de.othaw.nicolasproske.mauc.R;
import de.othaw.nicolasproske.mauc.object.Particle;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @author googlearchive (The Android Open Source Project)
 * @version 20.06.2020
 */
public final class ParticleManager {

    private final static int NUM_PARTICLES = 15;
    private final MainActivity mainActivity;
    private final List<Particle> balls = new CopyOnWriteArrayList<>();

    /**
     * Instantiates a new Particle manager.
     *
     * @param mainActivity the main activity
     */
    public ParticleManager(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        /*
         * Initially our particles have no speed or acceleration
         */
        Particle[] tempBalls = new Particle[NUM_PARTICLES];

        for (int i = 0; i < tempBalls.length; i++) {
            // Create particle
            tempBalls[i] = new Particle(mainActivity.getSimulationView().getContext(), mainActivity);
            tempBalls[i].setBackgroundResource(R.drawable.ball);
            tempBalls[i].setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Add created particle to array-list
            balls.add(tempBalls[i]);

            // Add ball to the simulation view so the ball will be displayed
            mainActivity.getSimulationView().addView(tempBalls[i], new ViewGroup.LayoutParams(mainActivity.getSimulationView().getParticleWidth(), mainActivity.getSimulationView().getParticleHeight()));
        }
    }

    /**
     * Update the position of each particle in the system using the Verlet integrator.
     * The Verlet algorithm is a method for the numerical solution of Newton's equations of motion.
     * Check if one of the balls displayed on the screen met the circle - if so, remove them
     *
     * @param x         the new x position of the ball
     * @param y         the new y position of the ball
     * @param timestamp the current timestamp
     */
    public void updatePositions(final float x, final float y, final long timestamp) {
        // Update only if the last update timestamp is not zero
        if (mainActivity.getSimulationView().getLastTimeStamp() != 0) {
            final float dateTime = (float) (timestamp - mainActivity.getSimulationView().getLastTimeStamp()) / 1000.f;

            for (int i = 0; i < balls.size(); i++) {
                final Particle ball = balls.get(i);

                // Recompute the position of each ball
                ball.computePhysics(x, y, dateTime);

                // Get x and y coordinate of current ball to check, if the ball is in inner circle
                final double ballDx = Math.pow(ball.getX() - mainActivity.getSimulationView().getPaintCircleX(), 2);
                final double ballDy = Math.pow(ball.getY() - mainActivity.getSimulationView().getPaintCircleY(), 2);

                // Check if ball is in inner circle
                if ((ballDx + ballDy) < Math.pow(mainActivity.getSimulationView().getPaintCircleRadius(), 2)) {
                    // * Ball is in inner circle *
                    // Remove ball from view
                    mainActivity.getSimulationView().removeView(ball);

                    // Remove ball from ArrayList
                    balls.remove(i);

                    // Update count of scored balls by adding one to the score
                    mainActivity.getSimulationView().setScore(mainActivity.getSimulationView().getScore() + 1);

                    mainActivity.getMqttManager().publish("Scored, " + mainActivity.getSimulationView().getScore());
                    break;
                }
            }
        }
        mainActivity.getSimulationView().setLastTimeStamp(timestamp);
    }

    /**
     * Performs one iteration of the simulation. First updating the
     * position of all the particles and resolving the constraints and
     * collisions.
     *
     * @param x         the new x position of the ball
     * @param y         the new y position of the ball
     * @param timestamp the current timestamp
     */
    public void update(final float x, final float y, long timestamp) {
        // update the system's positions
        updatePositions(x, y, timestamp);

        // We do no more than a limited number of iterations
        final int NUM_MAX_ITERATIONS = 10;

        /*
         * Resolve collisions, each particle is tested against every
         * other particle for collision. If a collision is detected the
         * particle is moved away using a virtual spring of infinite
         * stiffness.
         */
        boolean more = true;
        final int count = balls.size();

        for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
            more = false;

            // Iterate over all balls
            for (int i = 0; i < count; i++) {
                final Particle curr = balls.get(i);

                for (int j = i + 1; j < count; j++) {
                    final Particle ball = balls.get(j);
                    float dx = ball.getPosX() - curr.getPosX();
                    float dy = ball.getPosY() - curr.getPosY();
                    // dd is to detect ball by ball collision
                    float dd = dx * dx + dy * dy;

                    // Check for collisions
                    if (dd <= mainActivity.getSimulationView().getsBallDiameter2()) {
                        /*
                         * add a little bit of entropy, after nothing is
                         * perfect in the universe.
                         */
                        dx += ((float) Math.random() - 0.5f) * 0.0001f;
                        dy += ((float) Math.random() - 0.5f) * 0.0001f;
                        dd = dx * dx + dy * dy;

                        // simulate the spring
                        final float d = (float) Math.sqrt(dd);
                        final float c = (0.5f * (mainActivity.getSimulationView().getsBallDiameter() - d)) / d;
                        final float effectX = dx * c;
                        final float effectY = dy * c;

                        // Update ball position with calculated effect so the balls don't overlap each other
                        curr.setPosX(curr.getPosX() - effectX);
                        curr.setPosY(curr.getPosY() - effectY);
                        ball.setPosX(ball.getPosX() + effectX);
                        ball.setPosX(ball.getPosX() + effectX);
                        ball.setPosY(ball.getPosY() + effectY);

                        more = true;
                    }
                }
                // Calculate the new ball positions if collision with phone bounds was detected
                curr.resolveCollisionWithBounds();
            }
        }
    }


    /**
     * Gets particle count.
     *
     * @return the particle count
     */
    public int getParticleCount() {
        return balls.size();
    }

    /**
     * Gets balls.
     *
     * @return the balls
     */
    public List<Particle> getBalls() {
        return balls;
    }

    /**
     * Gets x position of a specific ball.
     *
     * @param i the i-th ball
     * @return the x position of the i-th ball
     */
    public float getPosX(int i) {
        return balls.get(i).getPosX();
    }

    /**
     * Gets y position of a specific ball.
     *
     * @param i the i-th ball
     * @return the y position of the i-th ball
     */
    public float getPosY(int i) {
        return balls.get(i).getPosY();
    }
}
