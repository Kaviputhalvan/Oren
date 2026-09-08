package apk.oren;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggerService extends Service {

    public static final String TAG = "OrenLogger";

    private static final String CHANNEL_ID =
            "oren_logger_service";

    private static final int NOTIFICATION_ID = 1001;

    private File logFile;

    /*
     * ---------------------------------------------------------
     * SERVICE
     * ---------------------------------------------------------
     */

    @Override
    public void onCreate() {
        super.onCreate();

        logFile = new File(
                getFilesDir(),
                "oren.log"
        );

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                    NOTIFICATION_ID,
                    createNotification()
            );
        }

        log(
                this,
                "INFO",
                "SERVICE",
                "LoggerService created"
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        log(
                this,
                "OK",
                "SERVICE",
                "LoggerService started"
        );

        /*
         * Do NOT keep the service alive forever.
         *
         * If the service has no actual background work,
         * stop it after startup.
         *
         * Logging itself does NOT depend on this service.
         */

        stopSelf(startId);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        /*
         * Log the service stopping before destruction.
         */
        log(
                this,
                "WARNING",
                "SERVICE",
                "LoggerService stopped"
        );

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*
     * ---------------------------------------------------------
     * PUBLIC LOGGER
     * ---------------------------------------------------------
     *
     * Any Activity / Service / class can call:
     *
     * LoggerService.log(
     *     context,
     *     "INFO",
     *     "MainActivity",
     *     "Button clicked"
     * );
     *
     * The LoggerService does NOT need to be running.
     */

    public static synchronized void log(
            Context context,
            String tag,
            String source,
            String text
    ) {

        if (context == null)
            return;

        try {

            Context appContext =
                    context.getApplicationContext();

            File logFile =
                    new File(
                            appContext.getFilesDir(),
                            "oren.log"
                    );

            /*
             * TAG
             */
            if (tag == null)
                tag = "INFO";

            tag = tag
                    .toUpperCase(Locale.ROOT)
                    .replace("|", "")
                    .replace("\n", "")
                    .replace("\r", "");

            /*
             * SOURCE
             */
            if (source == null)
                source = "UNKNOWN";

            source = source
                    .replace("|", "")
                    .replace("\n", "")
                    .replace("\r", "");

            /*
             * TEXT
             */
            if (text == null)
                text = "null";

            text = String.valueOf(text)
                    .replace("\\", "\\\\")
                    .replace("|", "\\|")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            /*
             * LINE NUMBER
             */
            int number =
                    getNextLineNumber(logFile);

            /*
             * TIME
             */
            String time =
                    new SimpleDateFormat(
                            "HH:mm:ss.SSS",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    );

            /*
             * FORMAT
             *
             * 0001|13:41:22.123|INFO|MainActivity|Started
             */
            String line =
                    String.format(
                            Locale.US,
                            "%04d|%s|%s|%s|%s%n",
                            number,
                            time,
                            tag,
                            source,
                            text
                    );

            /*
             * WRITE
             */
            try (
                    FileOutputStream out =
                            new FileOutputStream(
                                    logFile,
                                    true
                            )
            ) {

                out.write(
                        line.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                out.flush();
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to write log",
                    e
            );
        }
    }


    /*
     * ---------------------------------------------------------
     * CONVENIENCE METHODS
     * ---------------------------------------------------------
     */

    public static void info(
            Context context,
            String source,
            String text
    ) {

        log(
                context,
                "INFO",
                source,
                text
        );
    }


    public static void ok(
            Context context,
            String source,
            String text
    ) {

        log(
                context,
                "OK",
                source,
                text
        );
    }


    public static void warning(
            Context context,
            String source,
            String text
    ) {

        log(
                context,
                "WARNING",
                source,
                text
        );
    }


    public static void error(
            Context context,
            String source,
            String text
    ) {

        log(
                context,
                "ERROR",
                source,
                text
        );
    }


    public static void debug(
            Context context,
            String source,
            String text
    ) {

        log(
                context,
                "DEBUG",
                source,
                text
        );
    }


    /*
     * ---------------------------------------------------------
     * LINE NUMBER
     * ---------------------------------------------------------
     */

    private static int getNextLineNumber(
            File logFile
    ) {

        if (!logFile.exists())
            return 1;

        int count = 0;

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(
                                        logFile
                                )
                        )
        ) {

            while (
                    reader.readLine() != null
            ) {
                count++;
            }

        } catch (IOException e) {

            Log.e(
                    TAG,
                    "Failed to count logs",
                    e
            );
        }

        return count + 1;
    }


    /*
     * ---------------------------------------------------------
     * NOTIFICATION
     * ---------------------------------------------------------
     */

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            return new Notification.Builder(
                    this,
                    CHANNEL_ID
            )
                    .setContentTitle(
                            "Oren Logger"
                    )
                    .setContentText(
                            "Logger service running"
                    )
                    .setSmallIcon(
                            android.R.drawable
                                    .ic_menu_info_details
                    )
                    .setOngoing(true)
                    .build();

        } else {

            return new Notification.Builder(this)
                    .setContentTitle(
                            "Oren Logger"
                    )
                    .setContentText(
                            "Logger service running"
                    )
                    .setSmallIcon(
                            android.R.drawable
                                    .ic_menu_info_details
                    )
                    .setOngoing(true)
                    .build();
        }
    }


    /*
     * ---------------------------------------------------------
     * NOTIFICATION CHANNEL
     * ---------------------------------------------------------
     */

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Oren Logger",
                            NotificationManager
                                    .IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Oren logger service"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }
}