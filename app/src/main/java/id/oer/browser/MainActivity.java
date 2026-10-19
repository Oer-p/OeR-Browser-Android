package id.oer.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText addressInput;
    private EditText homeSearchInput;
    private EditText htmlInput;

    private OeRRenderView renderView;
    private WebView webView;

    private LinearLayout root;
    private LinearLayout addressRow;
    private LinearLayout navRow;
    private LinearLayout homePanel;
    private LinearLayout htmlPanel;
    private FrameLayout contentFrame;

    private final int black = Color.rgb(5, 5, 5);
    private final int dark = Color.rgb(23, 23, 23);
    private final int green = Color.rgb(54, 226, 121);
    private final int white = Color.WHITE;
    private final int gray = Color.rgb(165, 165, 165);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(black);

        createHeader();
        createAddressBar();
        createNavigation();
        createContent();

        setContentView(root);
        showHome();
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        shape.setStroke(1, Color.rgb(48, 48, 48));
        return shape;
    }

    private TextView makeText(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(white);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(dark)
        );
        return button;
    }

    private EditText makeInput(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setTextColor(white);
        input.setHintTextColor(gray);
        input.setHint(hint);
        input.setPadding(8, 4, 8, 4);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        return input;
    }

    // Logo O pelangi
    private class RainbowTextView extends TextView {

        public RainbowTextView(String text, int size) {
            super(MainActivity.this);
            setText(text);
            setTextSize(size);
            setTypeface(null, Typeface.BOLD);
            setGravity(Gravity.CENTER);
            setIncludeFontPadding(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (getWidth() > 0) {
                getPaint().setShader(new LinearGradient(
                    0, 0, getWidth(), 0,
                    new int[] {
                        Color.rgb(255, 65, 75),
                        Color.rgb(255, 180, 45),
                        Color.rgb(255, 230, 65),
                        Color.rgb(54, 226, 121),
                        Color.rgb(55, 140, 255),
                        Color.rgb(190, 90, 255)
                    },
                    null,
                    Shader.TileMode.CLAMP
                ));
            }
            super.onDraw(canvas);
            getPaint().setShader(null);
        }
    }

    private void addLogo(LinearLayout parent, int size) {
        LinearLayout logo = new LinearLayout(this);
        logo.setOrientation(LinearLayout.HORIZONTAL);
        logo.setGravity(Gravity.CENTER);

        logo.addView(new RainbowTextView("O", size));

        TextView oe = makeText("oe", size, white);
        oe.setTypeface(null, Typeface.BOLD);
        oe.setIncludeFontPadding(false);
        logo.addView(oe);

        TextView r = makeText("R", size, green);
        r.setTypeface(null, Typeface.BOLD);
        r.setIncludeFontPadding(false);
        logo.addView(r);

        parent.addView(logo);
    }

    // Ikon pencarian pelangi
    private class RainbowSearchIcon extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public RainbowSearchIcon() {
            super(MainActivity.this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() * 0.43f;
            float cy = getHeight() * 0.40f;
            float radius = Math.min(getWidth(), getHeight()) * 0.29f;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.5f);
            paint.setStrokeCap(Paint.Cap.ROUND);

            paint.setShader(new LinearGradient(
                0, 0, getWidth(), getHeight(),
                new int[] {
                    Color.RED,
                    Color.rgb(255, 180, 30),
                    Color.YELLOW,
                    green,
                    Color.BLUE,
                    Color.MAGENTA
                },
                null,
                Shader.TileMode.CLAMP
            ));

            canvas.drawCircle(cx, cy, radius, paint);

            paint.setShader(null);
            paint.setColor(green);

            canvas.drawLine(
                cx + radius * 0.7f,
                cy + radius * 0.7f,
                cx + radius * 1.55f,
                cy + radius * 1.55f,
                paint
            );
        }
    }

    // Ikon mikrofon
    private View createMicIcon() {
        return new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;

                paint.setColor(white);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.2f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);

                canvas.drawRoundRect(cx - 5, cy - 12, cx + 5, cy + 5, 5, 5, paint);
                canvas.drawArc(cx - 10, cy - 7, cx + 10, cy + 12, 0, 180, false, paint);
                canvas.drawLine(cx, cy + 12, cx, cy + 17, paint);
                canvas.drawLine(cx - 5, cy + 17, cx + 5, cy + 17, paint);
            }
        };
    }

    private void createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        header.setPadding(0, 28, 16, 0);
        header.setBackgroundColor(black);

        Button account = makeButton("Akun");
        account.setTextColor(green);
        account.setTextSize(14);
        account.setPadding(16, 0, 16, 0);
        account.setMinWidth(0);
        account.setMinimumWidth(0);
        account.setMinHeight(0);
        account.setMinimumHeight(0);
        account.setBackground(rounded(dark, 40));
        account.setOnClickListener(v -> showAccountInfo());

        header.addView(account, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 42
        ));

        root.addView(header, new LinearLayout.LayoutParams(-1, 66));
    }

    private void showAccountInfo() {
        new AlertDialog.Builder(this)
            .setTitle("Akun OoeR")
            .setMessage(
                "Fitur akun OoeR sedang disiapkan.\n\n" +
                "Login dan pendaftaran akan dihubungkan " +
                "setelah konfigurasi Supabase dipastikan."
            )
            .setPositiveButton("Mengerti", null)
            .show();
    }

    private void createAddressBar() {
        addressRow = new LinearLayout(this);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(8, 3, 8, 3);
        addressRow.setBackgroundColor(black);

        addressInput = makeInput("Cari atau masukkan alamat...");
        addressRow.addView(addressInput, new LinearLayout.LayoutParams(0, 48, 1));

        Button open = makeButton("Buka");
        open.setTextColor(green);
        addressRow.addView(open);

        open.setOnClickListener(v -> openAddress(addressInput));

        addressInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openAddress(addressInput);
                return true;
            }
            return false;
        });

        root.addView(addressRow);
        addressRow.setVisibility(View.GONE);
    }

    private void createNavigation() {
        navRow = new LinearLayout(this);
        navRow.setGravity(Gravity.CENTER);
        navRow.setBackgroundColor(black);

        Button back = makeButton("‹ Kembali");
        Button refresh = makeButton("↻ Muat ulang");
        Button home = makeButton("⌂ Beranda");
        Button editor = makeButton("</> HTML");

        navRow.addView(back, new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(refresh, new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(home, new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(editor, new LinearLayout.LayoutParams(0, 46, 1));

        back.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                showHome();
            }
        });

        refresh.setOnClickListener(v -> webView.reload());
        home.setOnClickListener(v -> showHome());
        editor.setOnClickListener(v -> showEditor());

        root.addView(navRow);
        navRow.setVisibility(View.GONE);
    }

    private void createContent() {
        contentFrame = new FrameLayout(this);

        // ===== HOME =====
        homePanel = new LinearLayout(this);
        homePanel.setOrientation(LinearLayout.VERTICAL);
        homePanel.setGravity(Gravity.CENTER_HORIZONTAL);
        homePanel.setPadding(24, 0, 24, 0);
        homePanel.setBackgroundColor(black);

        // Spacer atas
        View topSpace = new View(this);
        homePanel.addView(topSpace, new LinearLayout.LayoutParams(1, 0, 1.2f));

        // Logo
        addLogo(homePanel, 52);

        // Subtitle
        TextView subtitle = makeText("Jelajahi dunia dengan OoeR", 14, gray);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 36);
        homePanel.addView(subtitle);

        // Search Bar
        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(10, 4, 6, 4);
        searchBar.setBackground(rounded(dark, 30));

        RainbowSearchIcon searchIcon = new RainbowSearchIcon();
        searchBar.addView(searchIcon, new LinearLayout.LayoutParams(40, 48));

        homeSearchInput = makeInput("Cari apa");
        homeSearchInput.setTextSize(15);
        searchBar.addView(homeSearchInput, new LinearLayout.LayoutParams(0, 56, 1));

        View mic = createMicIcon();
        searchBar.addView(mic, new LinearLayout.LayoutParams(40, 48));
        mic.setOnClickListener(v ->
            Toast.makeText(this, "Fitur mikrofon akan dikembangkan", Toast.LENGTH_SHORT).show()
        );

        Button searchBtn = makeButton("↑");
        searchBtn.setTextSize(22);
        searchBtn.setTypeface(null, Typeface.BOLD);
        searchBtn.setTextColor(Color.BLACK);
        searchBtn.setGravity(Gravity.CENTER);
        searchBtn.setPadding(0, 0, 0, 2);
        searchBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
        searchBar.addView(searchBtn, new LinearLayout.LayoutParams(48, 52));

        // Search bar full width dengan margin
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 72
        );
        searchParams.setMargins(8, 0, 8, 0);
        homePanel.addView(searchBar, searchParams);

        searchBtn.setOnClickListener(v -> openAddress(homeSearchInput));
        homeSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openAddress(homeSearchInput);
                return true;
            }
            return false;
        });

        // Spacer bawah
        View bottomSpace = new View(this);
        homePanel.addView(bottomSpace, new LinearLayout.LayoutParams(1, 0, 1.8f));

        // ===== WEBVIEW =====
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // ===== HTML EDITOR =====
        htmlPanel = new LinearLayout(this);
        htmlPanel.setOrientation(LinearLayout.VERTICAL);
        htmlPanel.setPadding(16, 12, 16, 12);
        htmlPanel.setBackgroundColor(black);

        TextView htmlTitle = makeText("Editor HTML OoeR", 18, green);
        htmlPanel.addView(htmlTitle);

        htmlInput = new EditText(this);
        htmlInput.setHint("Masukkan kode HTML di sini");
        htmlInput.setHintTextColor(gray);
        htmlInput.setTextColor(white);
        htmlInput.setGravity(Gravity.TOP | Gravity.START);
        htmlInput.setMinLines(5);
        htmlInput.setPadding(14, 14, 14, 14);
        htmlInput.setBackground(rounded(dark, 12));
        htmlInput.setText(
            "<h1>Selamat datang di OoeR</h1>\n" +
            "<p>Ini adalah mesin render HTML OoeR.</p>\n" +
            "<h2>Fitur awal</h2>\n" +
            "<ul>\n" +
            "  <li>Judul</li>\n" +
            "  <li>Paragraf</li>\n" +
            "  <li>Daftar</li>\n" +
            "</ul>\n" +
            "<hr>\n" +
            "<p>Mesin akan terus dikembangkan.</p>"
        );

        htmlPanel.addView(htmlInput, new LinearLayout.LayoutParams(-1, 0, 1));

        Button renderButton = makeButton("Tampilkan HTML");
        renderButton.setTextColor(green);
        htmlPanel.addView(renderButton);

        TextView resultTitle = makeText("Hasil render:", 15, white);
        resultTitle.setPadding(0, 12, 0, 8);
        htmlPanel.addView(resultTitle);

        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setBackgroundColor(Color.WHITE);
        resultScroll.setPadding(12, 12, 12, 12);

        renderView = new OeRRenderView(this);
        resultScroll.addView(renderView);

        htmlPanel.addView(resultScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        renderButton.setOnClickListener(v ->
            renderView.renderHtml(htmlInput.getText().toString())
        );

        // Tambahkan ke contentFrame
        contentFrame.addView(homePanel, new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(htmlPanel, new FrameLayout.LayoutParams(-1, -1));

        root.addView(contentFrame, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void showHome() {
        homePanel.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        htmlPanel.setVisibility(View.GONE);
        addressRow.setVisibility(View.GONE);
        navRow.setVisibility(View.GONE);
    }

    private void showBrowser() {
        homePanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        htmlPanel.setVisibility(View.GONE);
        addressRow.setVisibility(View.VISIBLE);
        navRow.setVisibility(View.VISIBLE);
    }

    private void showEditor() {
        homePanel.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        htmlPanel.setVisibility(View.VISIBLE);
        addressRow.setVisibility(View.GONE);
        navRow.setVisibility(View.GONE);
        renderView.renderHtml(htmlInput.getText().toString());
    }

    private void openAddress(EditText input) {
        String query = input.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Masukkan alamat atau pencarian", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;

        if (query.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            url = query;
        } else if (query.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$")) {
            url = "https://" + query;
        } else {
            try {
                url = "https://www.google.com/search?q=" +
                    java.net.URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
                Toast.makeText(this, "Pencarian gagal", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        addressInput.setText(url);
        showBrowser();
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (htmlPanel.getVisibility() == View.VISIBLE) {
            showHome();
        } else if (webView.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                showHome();
            }
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

    // ============================================================
    // Class OeRRenderView - Simple HTML Renderer
    // ============================================================
    private static class OeRRenderView extends LinearLayout {

        public OeRRenderView(android.content.Context context) {
            super(context);
            setOrientation(VERTICAL);
            setPadding(8, 8, 8, 8);
        }

        public void renderHtml(String html) {
            removeAllViews();

            if (html == null || html.trim().isEmpty()) {
                TextView empty = new TextView(getContext());
                empty.setText("Tidak ada konten");
                empty.setTextColor(Color.GRAY);
                addView(empty);
                return;
            }

            // Parsing sangat sederhana (bukan full HTML parser)
            String[] lines = html.split("\n");

            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;

                TextView tv = new TextView(getContext());
                tv.setTextColor(Color.BLACK);
                tv.setPadding(0, 4, 0, 4);

                if (line.startsWith("<h1>") && line.endsWith("</h1>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(24);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 12, 0, 8);
                } else if (line.startsWith("<h2>") && line.endsWith("</h2>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(20);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 10, 0, 6);
                } else if (line.startsWith("<h3>") && line.endsWith("</h3>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(17);
                    tv.setTypeface(null, Typeface.BOLD);
                } else if (line.startsWith("<p>") && line.endsWith("</p>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(0, 6, 0, 6);
                } else if (line.startsWith("<li>") && line.endsWith("</li>")) {
                    tv.setText("•  " + stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(16, 3, 0, 3);
                } else if (line.equals("<hr>") || line.equals("<hr/>") || line.equals("<hr />")) {
                    View lineView = new View(getContext());
                    lineView.setBackgroundColor(Color.rgb(200, 200, 200));
                    addView(lineView, new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2
                    ));
                    continue;
                } else if (line.startsWith("<ul>") || line.startsWith("</ul>") ||
                           line.startsWith("<ol>") || line.startsWith("</ol>")) {
                    continue; // skip container tags
                } else {
                    // fallback: tampilkan teks biasa (tanpa tag)
                    String clean = stripTags(line);
                    if (clean.isEmpty()) continue;
                    tv.setText(clean);
                    tv.setTextSize(15);
                }

                addView(tv);
            }
        }

        private String stripTags(String input) {
            return input
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
        }
    }
}
