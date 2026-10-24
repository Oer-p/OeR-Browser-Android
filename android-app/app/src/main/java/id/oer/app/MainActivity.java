package id.oer.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

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
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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

        TextView status = new TextView(this);
        status.setText("Fondasi aplikasi OoeR siap dikembangkan.");
        status.setTextSize(14);
        status.setTextColor(Color.GRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(16), 0, dp(24));

        Button button = new Button(this);
        button.setText("Mulai OoeR");
        button.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Selamat datang di OoeR!",
                        Toast.LENGTH_SHORT
                ).show()
        );

        root.addView(logo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(subtitle);
        root.addView(status);
        root.addView(button);

        setContentView(root);
    }
}
