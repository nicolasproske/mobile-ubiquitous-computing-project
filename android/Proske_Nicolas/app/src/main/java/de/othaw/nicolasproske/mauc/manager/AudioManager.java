package de.othaw.nicolasproske.mauc.manager;

import android.content.Context;

import de.othaw.nicolasproske.mauc.MainActivity;

/**
 * Mobile & Ubiquitous Computing - Student research project
 *
 * @author Nicolas Proske
 * @version 20.06.2020
 */
public final class AudioManager {

    private final MainActivity mainActivity;

    /**
     * Instantiates a new Audio manager.
     *
     * @param mainActivity the main activity
     */
    public AudioManager(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Mute the application.
     */
    public void mute() {
        final android.media.AudioManager audioManager = (android.media.AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, true);
    }

    /**
     * Unmute the application.
     */
    public void unmute() {
        final android.media.AudioManager audioManager = (android.media.AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, false);
    }
}
