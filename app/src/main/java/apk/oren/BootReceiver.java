package apk.oren;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Intent logger = new Intent(context, LoggerService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(logger);
        } else {
            context.startService(logger);
        }

        if (Settings.canDrawOverlays(context)) {

            Intent bubble =
                    new Intent(context, FloatingBubbleService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(bubble);
            } else {
                context.startService(bubble);
            }
        }
    }
}