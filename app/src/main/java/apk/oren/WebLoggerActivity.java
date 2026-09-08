package apk.oren;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WebLoggerActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(
                new LoggerBridge(),
                "Android"
        );

        // Log.html is inside app/src/main/assets/
        webView.loadUrl("file:///android_asset/Log.html");
    }

    public class LoggerBridge {

        @JavascriptInterface
        public String readLog() {
            try {
                File file = new File(
                        getFilesDir(),
                        "oren.log"
                );

                if (!file.exists())
                    return "";

                return new String(
                        Files.readAllBytes(file.toPath()),
                        StandardCharsets.UTF_8
                );

            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public String getLogPath() {
            return new File(
                    getFilesDir(),
                    "oren.log"
            ).getAbsolutePath();
        }

        @JavascriptInterface
        public String getHtmlPath() {
            return new File(
                    getFilesDir(),
                    "Log.html"
            ).getAbsolutePath();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("Android");
            webView.destroy();
        }

        super.onDestroy();
    }
}