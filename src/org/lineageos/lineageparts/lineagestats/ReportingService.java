/*
 * SPDX-FileCopyrightText: 2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.lineagestats;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

public class ReportingService extends IntentService {
    static final String TAG = "crDroidStats";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public ReportingService() {
        super(ReportingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JobScheduler js = getSystemService(JobScheduler.class);

        Context context = getApplicationContext();

        String deviceId = Utilities.getUniqueID(context);
        String deviceName = Utilities.getDevice();
        String deviceLospVersion = Utilities.getModVersion();
        String deviceBuildDate = Utilities.getBuildDate();
        String deviceAndroidVersion = Utilities.getAndroidVersion();
        String deviceTag = Utilities.getTag();
        String deviceCountry = Utilities.getCountryCode(context);
        String deviceCarrier = Utilities.getCarrier(context);
        String deviceCarrierId = Utilities.getCarrierId(context);

        final int lospOldJobId = AnonymousStats.getLastJobId(context);
        final int lospJobId = AnonymousStats.getNextJobId(context);

        if (DEBUG) Log.d(TAG, "scheduling job id: " + lospJobId);

        PersistableBundle lospBundle = new PersistableBundle();
        lospBundle.putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName);
        lospBundle.putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId);
        lospBundle.putString(StatsUploadJobService.KEY_CR_VERSION, deviceLospVersion);
        lospBundle.putString(StatsUploadJobService.KEY_BUILD_DATE, deviceBuildDate);
        lospBundle.putString(StatsUploadJobService.KEY_ANDROID_VERSION, deviceAndroidVersion);
        lospBundle.putString(StatsUploadJobService.KEY_TAG, deviceTag);
        lospBundle.putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry);
        lospBundle.putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier);
        lospBundle.putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId);
        lospBundle.putLong(StatsUploadJobService.KEY_TIMESTAMP, System.currentTimeMillis());

        // set job types
        lospBundle.putInt(StatsUploadJobService.KEY_JOB_TYPE,
                StatsUploadJobService.JOB_TYPE_CRDROID);

        // schedule LOSP stats upload
        js.schedule(new JobInfo.Builder(lospJobId, new ComponentName(getPackageName(),
                StatsUploadJobService.class.getName()))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1000)
                .setExtras(lospBundle)
                .setPersisted(true)
                .build());

        // cancel old job in case it didn't run yet
        js.cancel(lospOldJobId);

        // reschedule
        AnonymousStats.updateLastSynced(this);
        ReportingServiceManager.setAlarm(this);
    }
}
