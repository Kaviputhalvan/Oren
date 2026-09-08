package apk.oren;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

public class HelpActivity extends Activity {

    private static final String VIDEO_URL =
            "https://www.youtube.com/watch?v=9QUOmEB4dDE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        openYouTube();

        finish();
    }

    private void openYouTube() {

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(VIDEO_URL)
        );

        try {
            intent.setPackage(
                    "com.google.android.youtube"
            );

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            intent.setPackage(null);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }
}