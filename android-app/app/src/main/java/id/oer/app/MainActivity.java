
package id.oer.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private TextView status;
    private Button button;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.WHITE);

        TextView logo = new TextView(this);
        logo.setText("OoeR");
        logo.setTextSize(42);
        logo.setGravity(Gravity.CENTER);
        logo.setTextColor(Color.rgb(101, 163, 13));
        logo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ekosistem OoeR");
        subtitle.setTextSize(17);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Backend OoeR belum diperiksa.");
        status.setTextSize(14);
        status.setTextColor(Color.GRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(16), 0, dp(24));

        button = new Button(this);
        button.setText("Mulai OoeR");
        button.setOnClickListener(v -> checkBackend());

        root.addView(logo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(subtitle);
        root.addView(status);
        root.addView(button);

        setContentView(root);
    }

    private void checkBackend() {
        button.setEnabled(false);
        button.setText("Memeriksa...");
        status.setText("Menghubungkan ke backend OoeR...");

        new Thread(() -> {
            HttpURLConnection connection = null;
            String result;
            boolean connected = false;

            try {
                URL url = new URL("http://127.0.0.1:3000/health");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int code = connection.getResponseCode();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                code >= 200 && code < 400
                                        ? connection.getInputStream()
                                        : connection.getErrorStream()
                        )
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());

                connected = code == 200
                        && "ok".equals(json.optString("status"));

                result = connected
                        ? "Backend terhubung!\nDatabase: "
                            + json.optString("database", "tidak diketahui")
                        : "Backend merespons, tetapi status belum sehat.";

            } catch (Exception e) {
                result = "Koneksi gagal.\nPastikan backend Termux aktif.";
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            final boolean success = connected;
            final String message = result;

            handler.post(() -> {
                status.setText(message);
                status.setTextColor(success
                        ? Color.rgb(77, 124, 15)
                        : Color.RED);

                button.setEnabled(true);
                button.setText("Cek Koneksi Lagi");

                if (success) {
                    Toast.makeText(
                            MainActivity.this,
                            "Backend OoeR terhubung!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }).start();
    }
}
