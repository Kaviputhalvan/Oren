package apk.oren;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        TextView github = findViewById(R.id.githubLink);

        if (github != null) {
            github.setOnClickListener(v ->
                    new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(
                                    "https://github.com/Kaviputhalvan/Oren"
                            )
                    )
            );
        }
    }
}