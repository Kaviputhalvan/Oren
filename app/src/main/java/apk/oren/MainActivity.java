package apk.oren;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends Activity {

    private static final String CHANNEL_ID = "oren_control";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQ_NOTIFICATION = 10;

    private ImageButton popupToggle;
    private Button logsButton;
    private Button aboutButton;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        popupToggle = findViewById(R.id.popupToggle);
        logsButton = findViewById(R.id.logsButton);
        aboutButton = findViewById(R.id.aboutButton);
        helpButton = findViewById(R.id.helpButton);

        requestPermissions();

        startLogger();

        if (popupToggle != null) {
            popupToggle.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            launchPopup();
                        }
                    }
            );
        }

        if (logsButton != null) {
            logsButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openLogs();
                        }
                    }
            );
        }

        if (aboutButton != null) {
            aboutButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openAbout();
                        }
                    }
            );
        }

        if (helpButton != null) {
            helpButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openHelp();
                        }
                    }
            );
        }

        createControlNotification();
    }

    @Override
    protected void onStart() {
        super.onStart();

        startLogger();
        createControlNotification();
    }

    private void openLogs() {
        Intent intent = new Intent(
                MainActivity.this,
                WebLoggerActivity.class
        );

        startActivity(intent);
    }

    private void openAbout() {
        Intent intent = new Intent(
                MainActivity.this,
                AboutActivity.class
        );

        startActivity(intent);
    }

    private void openHelp() {
        Intent intent = new Intent(
                MainActivity.this,
                HelpActivity.class
        );

        startActivity(intent);
    }

    private void launchPopup() {

        if (!Settings.canDrawOverlays(this)) {

            try {
                Intent settings = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse(
                                "package:" + getPackageName()
                        )
                );

                startActivity(settings);

            } catch (Exception ignored) {
            }

            return;
        }

        Intent intent = new Intent(
                this,
                FloatingBubbleService.class
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void startLogger() {

        Intent intent = new Intent(
                this,
                LoggerService.class
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void createControlNotification() {

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Oren Control",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Oren background control"
            );

            channel.setShowBadge(false);

            manager.createNotificationChannel(channel);
        }

        Intent popupIntent =
                new Intent(
                        this,
                        FloatingBubbleService.class
                );

        PendingIntent popupPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            popupPendingIntent =
                    PendingIntent.getForegroundService(
                            this,
                            2001,
                            popupIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

        } else {

            popupPendingIntent =
                    PendingIntent.getService(
                            this,
                            2001,
                            popupIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );
        }

        Intent logIntent =
                new Intent(
                        this,
                        WebLoggerActivity.class
                );

        PendingIntent logPendingIntent =
                PendingIntent.getActivity(
                        this,
                        2002,
                        logIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                .setSmallIcon(R.drawable.iconcfg)
                .setContentTitle("Oren")
                .setContentText(
                        "Logger running in background"
                )
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(
                        NotificationCompat.PRIORITY_LOW
                )
                .addAction(
                        R.drawable.bubble_base,
                        "Pop-up",
                        popupPendingIntent
                )
                .addAction(
                        R.drawable.iconcfg,
                        "Logs",
                        logPendingIntent
                );

        if (Build.VERSION.SDK_INT < 33
                || checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCompat
                    .from(this)
                    .notify(
                            NOTIFICATION_ID,
                            builder.build()
                    );
        }
    }

    private void requestPermissions() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        REQ_NOTIFICATION
                );
            }
        }

        if (!Settings.canDrawOverlays(this)) {

            try {

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse(
                                "package:" + getPackageName()
                        )
                );

                startActivity(intent);

            } catch (Exception ignored) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.System.canWrite(this)) {

            try {

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse(
                                "package:" + getPackageName()
                        )
                );

                startActivity(intent);

            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}