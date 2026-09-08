package apk.oren;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class OrentTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setLabel("ORENT");
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
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

        Intent intent = new Intent(
                this,
                FloatingBubbleService.class
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }
}