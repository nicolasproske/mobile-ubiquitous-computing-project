package de.othaw.nicolasproske.mauc.object;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import de.othaw.nicolasproske.mauc.MainActivity;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @author googlearchive (The Android Open Source Project)
 * @version 20.06.2020
 */
@SuppressLint("ViewConstructor")
public final class Particle extends View {

    private final MainActivity mainActivity;

    // Generate random number between zero and one
    private float posX = (float) Math.random();
    private float posY = (float) Math.random();

    // x-/y velocity of particle/ball
    private float velX;
    private float velY;


    /**
     * Instantiates a new Particle.
     *
     * @param context      the context
     * @param mainActivity the main activity
     */
    public Particle(final Context context, final MainActivity mainActivity) {
        super(context);
        this.mainActivity = mainActivity;
    }

    /**
     * Compute physics for all balls and update their vectors.
     *
     * @param sx the mouse acceleration in x direction
     * @param sy the mouse acceleration in y direction
     * @param dT the dt
     */
    public void computePhysics(final float sx, final float sy, final float dT) {
        final float ax = -sx / 5;
        final float ay = -sy / 5;

        posX += velX * dT + ax * dT * dT / 2;
        posY += velY * dT + ay * dT * dT / 2;

        velX += ax * dT;
        velY += ay * dT;
    }

    /**
     * Resolving constraints and collisions with the Verlet integrator
     * can be very simple, we simply need to move a colliding or
     * constrained particle in such way that the constraint is
     * satisfied.
     */
    public void resolveCollisionWithBounds() {
        final float xMax = mainActivity.getSimulationView().getHorizontalBound();
        final float yMax = mainActivity.getSimulationView().getVerticalBound();
        final float x = posX;
        final float y = posY;

        if (x > xMax) {
            posX = xMax;
            velX = 0;
        } else if (x < -xMax) {
            posX = -xMax;
            velX = 0;
        }

        if (y > yMax) {
            posY = yMax;
            velY = 0;
        } else if (y < -yMax) {
            posY = -yMax;
            velY = 0;
        }
    }

    /**
     * Gets relative x position.
     *
     * @return the x position
     */
    public float getPosX() {
        return posX;
    }

    /**
     * Sets relative x position.
     *
     * @param posX the x position
     */
    public void setPosX(float posX) {
        this.posX = posX;
    }

    /**
     * Gets relative y position.
     *
     * @return the y position
     */
    public float getPosY() {
        return posY;
    }

    /**
     * Sets relative y position.
     *
     * @param posY the y position
     */
    public void setPosY(float posY) {
        this.posY = posY;
    }
}
