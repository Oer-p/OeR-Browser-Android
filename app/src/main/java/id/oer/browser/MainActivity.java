
package id.oer.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private EditText htmlInput;
    private OeRRenderView renderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 247, 250));
        root.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText("OeR Browser");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(20, 65, 120));
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(0, 8, 0, 14);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Mesin render HTML sederhana OeR");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.DKGRAY);
        root.addView(subtitle);

        htmlInput = new EditText(this);
        htmlInput.setHint("Masukkan kode HTML di sini");
        htmlInput.setTextSize(14);
        htmlInput.setGravity(Gravity.TOP | Gravity.START);
        htmlInput.setMinLines(4);
        htmlInput.setMaxLines(7);
        htmlInput.setText(
            "<h1>Selamat datang di OeR</h1>" +
            "<p>Ini adalah percobaan mesin render OeR.</p>" +
            "<h2>Fitur awal</h2>" +
            "<ul><li>Judul</li><li>Paragraf</li>" +
            "<li>Daftar</li></ul><hr>" +
            "<p>Mesin akan terus dikembangkan.</p>"
        );

        LinearLayout.LayoutParams inputParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
            );
        inputParams.setMargins(0, 16, 0, 10);
        root.addView(htmlInput, inputParams);

        Button renderButton = new Button(this);
        renderButton.setText("Tampilkan HTML");
        root.addView(renderButton);

        TextView resultTitle = new TextView(this);
        resultTitle.setText("Hasil render:");
        resultTitle.setTextSize(16);
        resultTitle.setTextColor(Color.rgb(20, 65, 120));
        resultTitle.setPadding(0, 14, 0, 8);
        root.addView(resultTitle);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        renderView = new OeRRenderView(this);
        scrollView.addView(renderView);

        LinearLayout.LayoutParams resultParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 2
            );
        root.addView(scrollView, resultParams);

        renderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderView.renderHtml(htmlInput.getText().toString());
            }
        });

        setContentView(root);
        renderView.renderHtml(htmlInput.getText().toString());
    }
}
