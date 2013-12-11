package com.brainydroid.daydreaming.background;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manage global application status (like first launch) and collect
 * information on the device's status.
 *
 * @author Sébastien Lerique
 * @author Vincent Adam
 */
@Singleton
public class StatusManager {

    private static String TAG = "StatusManager";

    /** Preference key storing the first launch status */
    private static String EXP_STATUS_FL_COMPLETED =
            "expStatusFlCompleted";

    /** Preference key storing the Tipi questionnaire completion status */
    private static String EXP_STATUS_TIPI_COMPLETED =
            "expStatusTipiCompleted";

    /** Preference key storing the status of initial questions update */
    private static String EXP_STATUS_QUESTIONS_UPDATED =
            "expStatusQuestionsUpdated";

    /** Preference key storing timestamp of the last sync operation */
    private static String LAST_SYNC_TIMESTAMP = "lastSyncTimestamp";

    /** Preference key storing number of slots per poll */
    private static String QUESTIONS_N_SLOTS_PER_POLL =
            "questionsNSlotsPerPoll";

    /** Preference key storing timestamp of beginning of experiment */
    @SuppressWarnings("FieldCanBeLocal")
    private static String EXP_START_TIMESTAMP = "expStartTimestamp";

    /** Preference key storing latest retrieved ntp timestamp */
    @SuppressWarnings("FieldCanBeLocal")
    private static String LATEST_NTP_TIMESTAMP = "latestNtpTimestamp";

    /** Preference key storing the current mode */
    private static String EXP_CURRENT_MODE = "expCurrentMode";

    private static String TEST_MODE = "test";
    private static String PROD_MODE = "prod";
    private static String DEFAULT_MODE = PROD_MODE;


    /**
     * Interval below which we don't need to re-sync data to servers (in
     * milliseconds)
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static int SYNC_INTERVAL = 15 * 1000;

    @Inject LocationManager locationManager;
    @Inject ConnectivityManager connectivityManager;
    @Inject ActivityManager activityManager;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor eSharedPreferences;

    /**
     * Initialize the {@link SharedPreferences} editor.
     */
    @Inject
    public StatusManager(SharedPreferences sharedPreferences) {
        Logger.d(TAG, "StatusManager created");
        this.sharedPreferences = sharedPreferences;
        eSharedPreferences = sharedPreferences.edit();
    }

    /**
     * Check if first launch is completed.
     *
     * @return {@code true} if first launch has completed,
     *         {@code false} otherwise
     */
    public synchronized boolean isFirstLaunchCompleted() {
        if (sharedPreferences.getBoolean(EXP_STATUS_FL_COMPLETED, false)) {
            Logger.d(TAG, "First launch is completed");
            return true;
        } else {
            Logger.d(TAG, "First launch not completed yet");
            return false;
        }
    }

    /**
     * Set the first launch flag to completed.
     */
    public synchronized void setFirstLaunchCompleted() {
        Logger.d(TAG, "Setting first launch to completed");

        if (!isTipiQuestionnaireCompleted()) {
            throw new RuntimeException("Setting first launch to completed can" +
                    " only be done if Tipi questionnaire is also completed");
        }

        eSharedPreferences.putBoolean(EXP_STATUS_FL_COMPLETED, true);
        eSharedPreferences.commit();
    }


    /**
     * Set the first launch flag to not completed.
     */
    public synchronized void setFirstLaunchNotCompleted() {
        Logger.d(TAG, "Setting first launch to not completed");

        eSharedPreferences.putBoolean(EXP_STATUS_FL_COMPLETED, false);
        eSharedPreferences.commit();
    }

    public synchronized boolean isTipiQuestionnaireCompleted() {
        if (sharedPreferences.getBoolean(EXP_STATUS_TIPI_COMPLETED, false)) {
            Logger.d(TAG, "Tipi questionnaire is completed");
            return true;
        } else {
            Logger.d(TAG, "Tipi questionnaire not completed yet");
            return false;
        }
    }

    public synchronized void setTipiQuestionnaireCompleted() {
        Logger.d(TAG, "Setting Tipi questionnaire to completed");

        eSharedPreferences.putBoolean(EXP_STATUS_TIPI_COMPLETED, true);
        eSharedPreferences.commit();
    }

    public synchronized void setTipiQuestionnaireNotCompleted() {
        Logger.d(TAG, "Setting Tipi questionnaire to not completed");

        eSharedPreferences.putBoolean(EXP_STATUS_TIPI_COMPLETED, false);
        eSharedPreferences.commit();
    }


    /**
     * Check if the questions have been updated.
     *
     * @return {@code true} if the questions have been updated,
     *         {@code false} otherwise
     */
    public synchronized boolean areQuestionsUpdated() {
        if (sharedPreferences.getBoolean(EXP_STATUS_QUESTIONS_UPDATED,
                false)) {
            Logger.d(TAG, "Questions are updated");
            return true;
        } else {
            Logger.d(TAG, "Questions not updated yet");
            return false;
        }
    }

    /**
     * Set the updated questions flag to completed.
     */
    public synchronized void setQuestionsUpdated() {
        Logger.d(TAG, "Setting questions to updated");

        eSharedPreferences.putBoolean(EXP_STATUS_QUESTIONS_UPDATED, true);
        eSharedPreferences.commit();
    }

    /**
     * Set the updated questions flag to completed.
     */
    public synchronized void setQuestionsNotUpdated() {
        Logger.d(TAG, "Setting questions to updated");

        eSharedPreferences.putBoolean(EXP_STATUS_QUESTIONS_UPDATED, false);
        eSharedPreferences.commit();
    }

    /**
     * Get the number of slots per poll
     */
    public synchronized int getNSlotsPerPoll() {
        int nSlotsPerPoll = sharedPreferences.getInt(
                QUESTIONS_N_SLOTS_PER_POLL, -1);
        Logger.v(TAG, "nSlotsPerPoll is {}", nSlotsPerPoll);
        return nSlotsPerPoll;
    }

    public synchronized void setNSlotsPerPoll(int nSlotsPerPoll) {
        Logger.d(TAG, "Setting nSlotsPerPoll to {}", nSlotsPerPoll);
        eSharedPreferences.putInt(QUESTIONS_N_SLOTS_PER_POLL, nSlotsPerPoll);
        eSharedPreferences.commit();
    }

    /**
     * Check if {@link LocationService} is running.
     *
     * @return {@code true} if {@link LocationService} is running,
     *         {@code false} otherwise
     */
    public synchronized boolean isLocationServiceRunning() {
        // This hack was found on StackOverflow
        for (RunningServiceInfo service :
                activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(
                    service.service.getClassName())) {
                Logger.d(TAG, "LocationService is running");
                return true;
            }
        }

        Logger.d(TAG, "LocationService is not running");
        return false;
    }

    /**
     * Check if the network location provider is enabled.
     *
     * @return {@code true} if the network location provider is enabled,
     *         {@code false} otherwise
     */
    public synchronized boolean isNetworkLocEnabled() {
        if (locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)) {
            Logger.d(TAG, "Network location is enabled");
            return true;
        } else {
            Logger.d(TAG, "Network location is disabled");
            return false;
        }
    }

    /**
     * Check if data connection is enabled (or connecting).
     *
     * @return {@code true} if data connection is enabled or connecting,
     *         {@code false} otherwise
     */
    public synchronized boolean isDataEnabled() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Logger.d(TAG, "Data is enabled");
            return true;
        } else {
            Logger.d(TAG, "Data is disabled");
            return false;
        }
    }

    /**
     * Check if both data connection and network location provider are
     * enabled (data connection can also be still connecting only).
     *
     * @return {@code true} if the network location provider is enabled and
     *         data connection is enabled or connecting,
     *         {@code false} otherwise
     */
    public synchronized boolean isDataAndLocationEnabled() {
        if (isNetworkLocEnabled() && isDataEnabled()) {
            Logger.d(TAG, "Data and network location are enabled");
            return true;
        } else {
            Logger.d(TAG, "Either data or network location is disabled");
            return false;
        }
    }

    /**
     * Set the timestamp of the last sync operation to now.
     */
    public synchronized void setLastSyncToNow() {
        long now = SystemClock.elapsedRealtime();
        Logger.d(TAG, "Setting last sync timestamp to now");
        eSharedPreferences.putLong(LAST_SYNC_TIMESTAMP, now);
        eSharedPreferences.commit();
    }

    /**
     * Check if a sync operation was made not long ago.
     * <p/>
     * If a sync operation was made less than {@link #SYNC_INTERVAL}
     * milliseconds ago, this method will return false. Otherwise it will
     * return true. Use {@link #setLastSyncToNow} to set the timestamp of the
     * last sync operation when syncing.
     *
     * @return {@code boolean} indicating if the last sync operation was long
     *         ago or not
     */
    public synchronized boolean isLastSyncLongAgo() {
        // If last sync timestamp is present, make sure now is after the
        // threshold to force a sync.
        long threshold = sharedPreferences.getLong(LAST_SYNC_TIMESTAMP,
                - SYNC_INTERVAL) + SYNC_INTERVAL;
        if (threshold < SystemClock.elapsedRealtime()) {
            Logger.d(TAG, "Last sync was long ago");
            return true;
        } else {
            Logger.d(TAG, "Last sync was not long ago");
            return false;
        }
    }

    public synchronized void setExperimentStartTimestamp(long timestamp) {
        Logger.d(TAG, "Setting experiment start timestamp to {}", timestamp);
        eSharedPreferences.putLong(EXP_START_TIMESTAMP, timestamp);
        eSharedPreferences.commit();
    }

    public synchronized long getExperimentStartTimestamp() {
        return sharedPreferences.getLong(EXP_START_TIMESTAMP, -1);
    }

    public synchronized void setLatestNtpTime(long timestamp) {
        Logger.d(TAG, "Setting latest ntp requested time to {}", timestamp);
        eSharedPreferences.putLong(LATEST_NTP_TIMESTAMP, timestamp);
        eSharedPreferences.commit();

        if (!sharedPreferences.contains(EXP_START_TIMESTAMP)) {
            Logger.w(TAG, "expStartTimestamp doesn't seem to have been set. " +
                    "Setting it to this latest (and probably first) NTP " +
                    "timestamp");
            setExperimentStartTimestamp(timestamp);
        }
    }

    public synchronized long getLatestNtpTime() {
        return sharedPreferences.getLong(LATEST_NTP_TIMESTAMP, -1);
    }


   // ------------- Mode related functions
    /**
     * get current mode (test or prod)
     * @return
     */
    public synchronized String getCurrentMode() {
        String mode = sharedPreferences.getString(EXP_CURRENT_MODE, DEFAULT_MODE);
            Logger.d(TAG, "Current mode is " + mode);
            return mode;
    }

    public synchronized String getProfileName() {
        String profileName =  getCurrentMode();
        Logger.v(TAG, "Current profile name is " + profileName);
        return profileName;
    }

    /**
     * Set current mode
     * @param mode
     */
    private synchronized void setCurrentMode(String mode) {
        Logger.d(TAG, "Setting current mode to " + mode);
        eSharedPreferences.putString(EXP_CURRENT_MODE, mode);
        eSharedPreferences.commit();
    }

    public synchronized boolean isCurrentModeTest() {
        if (getCurrentMode().equals(TEST_MODE)) {
            Logger.d(TAG, "Current mode is test");
            return true;
        } else {
            Logger.d(TAG, "Current mode is not test");
            return false;
        }
    }

    public synchronized boolean isCurrentModeProd() {
        if (getCurrentMode().equals(PROD_MODE)) {
            Logger.d(TAG, "Current mode is prod");
            return true;
        } else {
            Logger.d(TAG, "Current mode is not prod");
            return false;
        }
    }

    public synchronized void setCurrentModeToTest() {
        Logger.d(TAG, "Setting current mode to test");
        setCurrentMode(TEST_MODE);
    }
    public synchronized void setCurrentModeToProd() {
        Logger.d(TAG, "Setting current mode to prod");
        setCurrentMode(PROD_MODE);
    }

}
