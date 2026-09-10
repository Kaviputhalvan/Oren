package apk.oren;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class OrentTileService extends TileService {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!Settings.canDrawOverlays(this)) {
            try {
                Intent settings = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );

                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAndCollapse(settings);

            } catch (Exception ignored) {
            }

            return;
        }

        if (isBubbleServiceRunning()) {

            // Restore Android auto-rotation
            enableAutoRotation();

            stopService(
                    new Intent(
                            this,
                            SystemSettingsService.class
                    )
            );

            stopService(
                    new Intent(
                            this,
                            FloatingBubbleService.class
                    )
            );

        } else {

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

        updateTileDelayed();
    }

    private boolean isBubbleServiceRunning() {

        ActivityManager manager =
                (ActivityManager)
                        getSystemService(ACTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo service
                : manager.getRunningServices(Integer.MAX_VALUE)) {

            ComponentName component = service.service;

            if (component != null
                    && getPackageName().equals(
                            component.getPackageName())
                    && FloatingBubbleService.class.getName()
                    .equals(component.getClassName())) {

                return true;
            }
        }

        return false;
    }

    private void enableAutoRotation() {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.System.canWrite(this)) {

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName())
                );

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAndCollapse(intent);

                return;
            }

            Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    1
            );

        } catch (Exception ignored) {
        }
    }

    private void updateTileDelayed() {

        updateTile();

        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        updateTile();
                    }
                },
                300
        );
    }

    private void updateTile() {

        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        boolean active = isBubbleServiceRunning();

        if (active) {

            tile.setLabel("OREN_ON");
            tile.setState(Tile.STATE_ACTIVE);

            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.orent_active
                    )
            );

        } else {

            tile.setLabel("OREN_OFF");
            tile.setState(Tile.STATE_INACTIVE);

            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.orent_inactive
                    )
            );
        }

        tile.updateTile();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}