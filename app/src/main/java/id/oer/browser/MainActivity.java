
package id.oer.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText htmlInput;
    private EditText addressInput;
    private OeRRenderView renderView;
    private WebView webView;
    private LinearLayout root;
    private LinearLayout htmlPanel;
    private boolean showingWebsite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 247, 250));
        root.setPadding(12, 12, 12, 12);

        TextView title = new TextView(this);
        title.setText("OeR Browser");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(20, 65, 120));
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(0, 6, 0, 10);
        root.addView(title);

        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setOrientation(LinearLayout.HORIZONTAL);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);

        addressInput = new EditText(this);
        addressInput.setSingleLine(true);
        addressInput.setTextSize(14);
        addressInput.setHint("Cari atau masukkan alamat website");
        addressInput.setImeOptions(EditorInfo.IME_ACTION_GO);

        addressRow.addView(addressInput,
            new LinearLayout.LayoutParams(0, 52, 1));

        Button openButton = new Button(this);
        openButton.setText("Buka");
        addressRow.addView(openButton);

        root.addView(addressRow);

        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);

        Button backButton = new Button(this);
        backButton.setText("Kembali");
        navRow.addView(backButton,
            new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button homeButton = new Button(this);
        homeButton.setText("Beranda");
        navRow.addView(homeButton,
            new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button htmlButton = new Button(this);
        htmlButton.setText("Editor HTML");
        navRow.addView(htmlButton,
            new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(navRow);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        root.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        htmlPanel = new LinearLayout(this);
        htmlPanel.setOrientation(LinearLayout.VERTICAL);
        htmlPanel.setVisibility(View.GONE);

        TextView subtitle = new TextView(this);
        subtitle.setText("Mesin render HTML sederhana OeR");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.DKGRAY);
        htmlPanel.addView(subtitle);

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

        htmlPanel.addView(htmlInput, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        Button renderButton = new Button(this);
        renderButton.setText("Tampilkan HTML");
        htmlPanel.addView(renderButton);

        TextView resultTitle = new TextView(this);
        resultTitle.setText("Hasil render OeR:");
        resultTitle.setTextSize(16);
        resultTitle.setTextColor(Color.rgb(20, 65, 120));
        resultTitle.setPadding(0, 8, 0, 6);
        htmlPanel.addView(resultTitle);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        renderView = new OeRRenderView(this);
        scrollView.addView(renderView);

        htmlPanel.addView(scrollView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(htmlPanel, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));

        openButton.setOnClickListener(v -> openAddress());
        addressInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                openAddress();
                return true;
            }
            return false;
        });

        backButton.setOnClickListener(v -> {
            if (showingWebsite && webView.canGoBack()) {
                webView.goBack();
            } else {
                Toast.makeText(this, "Belum ada halaman sebelumnya",
                    Toast.LENGTH_SHORT).show();
            }
        });

        homeButton.setOnClickListener(v -> {
            showingWebsite = true;
            htmlPanel.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            addressInput.setText("");
            webView.loadData(
                "<html><body style='font-family:sans-serif;padding:24px'>" +
                "<h1 style='color:#144178'>OeR Browser</h1>" +
                "<p>Selamat datang!</p>" +
                "<p>Ketik alamat website atau kata pencarian di atas.</p>" +
                "</body></html>",
                "text/html", "UTF-8");
        });

        htmlButton.setOnClickListener(v -> {
            showingWebsite = false;
            webView.setVisibility(View.GONE);
            htmlPanel.setVisibility(View.VISIBLE);
        });

        renderButton.setOnClickListener(v ->
            renderView.renderHtml(htmlInput.getText().toString()));

        setContentView(root);

        renderView.renderHtml(htmlInput.getText().toString());

        webView.loadData(
            "<html><body style='font-family:sans-serif;padding:24px'>" +
            "<h1 style='color:#144178'>OeR Browser</h1>" +
            "<p>Ketik alamat website atau kata pencarian di atas.</p>" +
            "</body></html>",
            "text/html", "UTF-8");
    }

    private void openAddress() {
        String input = addressInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Masukkan alamat atau kata pencarian",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String url;

        if (input.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            url = input;
        } else if (input.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$")) {
            url = "https://" + input;
        } else {
            try {
                url = "https://www.google.com/search?q="
                    + java.net.URLEncoder.encode(input, "UTF-8");
            } catch (Exception e) {
                Toast.makeText(this, "Pencarian gagal",
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showingWebsite = true;
        htmlPanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (showingWebsite && webView.canGoBack()) {
            webView.goBack();
        } else if (htmlPanel.getVisibility() == View.VISIBLE) {
            htmlPanel.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            showingWebsite = true;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
