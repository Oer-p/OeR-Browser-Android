
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
    private final int card = Color.rgb(32, 33, 36);
    private final int green = Color.rgb(54, 226, 121);
    private final int white = Color.WHITE;
    private final int gray = Color.rgb(165, 165, 165);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(black);
        getWindow().setNavigationBarColor(black);

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

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
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
        input.setTextSize(16);
        input.setTextColor(white);
        input.setHintTextColor(gray);
        input.setHint(hint);
        input.setPadding(dp(4), 0, dp(4), 0);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        return input;
    }

    // ==================== LOGO OoeR ====================

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
                        new int[]{
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

    // ==================== IKON PENCARIAN ====================

    private class SearchIcon extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        public SearchIcon() {
            super(MainActivity.this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() * 0.42f;
            float cy = getHeight() * 0.42f;
            float radius =
                    Math.min(getWidth(), getHeight()) * 0.28f;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.rgb(180, 180, 180));

            canvas.drawCircle(cx, cy, radius, paint);

            canvas.drawLine(
                    cx + radius * 0.72f,
                    cy + radius * 0.72f,
                    cx + radius * 1.45f,
                    cy + radius * 1.45f,
                    paint
            );
        }
    }

    // ==================== IKON MIKROFON ====================

    private View createMicIcon() {
        return new View(this) {

            private final Paint paint =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f - dp(1);

                paint.setColor(Color.rgb(180, 180, 180));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setStrokeCap(Paint.Cap.ROUND);

                canvas.drawRoundRect(
                        cx - dp(5.5f),
                        cy - dp(11),
                        cx + dp(5.5f),
                        cy + dp(4),
                        dp(6),
                        dp(6),
                        paint
                );

                canvas.drawArc(
                        cx - dp(11),
                        cy - dp(5),
                        cx + dp(11),
                        cy + dp(13),
                        0,
                        180,
                        false,
                        paint
                );

                canvas.drawLine(
                        cx, cy + dp(13),
                        cx, cy + dp(18),
                        paint
                );

                canvas.drawLine(
                        cx - dp(6), cy + dp(18),
                        cx + dp(6), cy + dp(18),
                        paint
                );
            }
        };
    }

    // ==================== HEADER AKUN ====================

    private void createHeader() {
        LinearLayout header = new LinearLayout(this);

        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(
                Gravity.LEFT | Gravity.CENTER_VERTICAL
        );

        // Akun di kiri, tidak menempel tepi dan sedikit turun.
        header.setPadding(dp(16), dp(55), dp(8), 0);
        header.setBackgroundColor(black);

        Button account = makeButton("Akun");
        account.setTextColor(green);
        account.setTextSize(14);
        account.setPadding(dp(18), 0, dp(18), 0);
        account.setMinHeight(0);
        account.setMinimumHeight(0);
        account.setBackground(rounded(dark, dp(40)));

        account.setOnClickListener(v -> showAccountInfo());

        header.addView(account, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(40)
        ));

        root.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(66)
        ));
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

    // ==================== ADDRESS BAR ====================

    private void createAddressBar() {
        addressRow = new LinearLayout(this);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(dp(12), dp(6), dp(12), dp(6));
        addressRow.setBackgroundColor(black);

        addressInput = makeInput("Cari atau masukkan alamat...");

        addressRow.addView(addressInput,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        Button open = makeButton("Buka");
        open.setTextColor(green);
        addressRow.addView(open);

        open.setOnClickListener(v -> openAddress(addressInput));

        addressInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO
                            || actionId == EditorInfo.IME_ACTION_DONE) {
                        openAddress(addressInput);
                        return true;
                    }
                    return false;
                }
        );

        root.addView(addressRow);
        addressRow.setVisibility(View.GONE);
    }

    // ==================== NAVIGASI BROWSER ====================

    private void createNavigation() {
        navRow = new LinearLayout(this);
        navRow.setGravity(Gravity.CENTER);
        navRow.setBackgroundColor(black);

        Button back = makeButton("‹ Kembali");
        Button refresh = makeButton("↻ Muat ulang");
        Button home = makeButton("⌂ Beranda");
        Button editor = makeButton("</> HTML");

        navRow.addView(back,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        navRow.addView(refresh,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        navRow.addView(home,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        navRow.addView(editor,
                new LinearLayout.LayoutParams(0, dp(48), 1));

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

    // ==================== KONTEN UTAMA ====================

    private void createContent() {
        contentFrame = new FrameLayout(this);

        // ==================== BERANDA OoeR ====================

        homePanel = new LinearLayout(this);
        homePanel.setOrientation(LinearLayout.VERTICAL);
        homePanel.setGravity(Gravity.CENTER_HORIZONTAL);
        homePanel.setBackgroundColor(black);

        View topSpace = new View(this);

        homePanel.addView(topSpace,
                new LinearLayout.LayoutParams(
                        1, 0, 0.9f
                ));

        // Logo diperbesar, tetap satu.
        addLogo(homePanel, 76);

        // Slogan didekatkan ke kolom pencarian.
        TextView subtitle = makeText(
                "Jelajahi dunia dengan OoeR",
                14,
                gray
        );

        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(16));
        homePanel.addView(subtitle);

        // ==================== KOLOM PENCARIAN ====================

        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(12), 0, dp(10), 0);
        searchBar.setBackground(rounded(card, dp(32)));

        SearchIcon searchIcon = new SearchIcon();

        searchBar.addView(searchIcon,
                new LinearLayout.LayoutParams(dp(36), dp(42)));

        homeSearchInput = makeInput("Cari apa");

        searchBar.addView(homeSearchInput,
                new LinearLayout.LayoutParams(0, dp(56), 1));

        View mic = createMicIcon();

        searchBar.addView(mic,
                new LinearLayout.LayoutParams(dp(34), dp(42)));

        mic.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Fitur mikrofon akan dikembangkan",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Tombol pencarian hijau.
        Button sendBtn = new Button(this);
        sendBtn.setText("↑");
        sendBtn.setTextSize(20);
        sendBtn.setTypeface(null, Typeface.BOLD);
        sendBtn.setTextColor(Color.BLACK);
        sendBtn.setAllCaps(false);
        sendBtn.setPadding(0, 0, 0, dp(2));
        sendBtn.setMinWidth(0);
        sendBtn.setMinimumWidth(0);
        sendBtn.setBackground(rounded(green, dp(22)));

        searchBar.addView(sendBtn,
                new LinearLayout.LayoutParams(dp(42), dp(42)));

        // Kolom pencarian panjang, responsif, dan tetap di tengah.
        int screenWidth = getResources()
                .getDisplayMetrics().widthPixels;

        int sideSpace = dp(16);
        int maxSearchWidth = dp(380);

        int searchWidth = Math.min(
                maxSearchWidth,
                screenWidth - sideSpace * 2
        );

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        searchWidth,
                        dp(64)
                );

        searchParams.gravity = Gravity.CENTER_HORIZONTAL;

        homePanel.addView(searchBar, searchParams);

        sendBtn.setOnClickListener(
                v -> openAddress(homeSearchInput)
        );

        homeSearchInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO
                            || actionId == EditorInfo.IME_ACTION_DONE) {
                        openAddress(homeSearchInput);
                        return true;
                    }
                    return false;
                }
        );

        // Area bawah tetap kosong.
        View bottomSpace = new View(this);

        homePanel.addView(bottomSpace,
                new LinearLayout.LayoutParams(1, 0, 1.4f));

        // ==================== WEBVIEW ====================

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // ==================== EDITOR HTML ====================

        htmlPanel = new LinearLayout(this);
        htmlPanel.setOrientation(LinearLayout.VERTICAL);
        htmlPanel.setPadding(dp(16), dp(12), dp(16), dp(12));
        htmlPanel.setBackgroundColor(black);

        TextView htmlTitle = makeText(
                "Editor HTML OoeR",
                18,
                green
        );

        htmlPanel.addView(htmlTitle);

        htmlInput = new EditText(this);
        htmlInput.setHint("Masukkan kode HTML di sini");
        htmlInput.setHintTextColor(gray);
        htmlInput.setTextColor(white);
        htmlInput.setGravity(Gravity.TOP | Gravity.START);
        htmlInput.setMinLines(5);
        htmlInput.setPadding(dp(14), dp(14), dp(14), dp(14));
        htmlInput.setBackground(rounded(dark, dp(12)));

        htmlInput.setText(
                "<h1>Selamat datang di OoeR</h1>\n" +
                "<p>Ini adalah mesin render HTML OoeR.</p>\n" +
                "<h2>Fitur awal</h2>\n" +
                "<ul>\n" +
                "<li>Judul</li>\n" +
                "<li>Paragraf</li>\n" +
                "<li>Daftar</li>\n" +
                "</ul>\n" +
                "<hr>\n" +
                "<p>Mesin akan terus dikembangkan.</p>"
        );

        htmlPanel.addView(htmlInput,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                ));

        Button renderButton = makeButton("Tampilkan HTML");
        renderButton.setTextColor(green);
        htmlPanel.addView(renderButton);

        TextView resultTitle = makeText(
                "Hasil render:",
                15,
                white
        );

        resultTitle.setPadding(0, dp(12), 0, dp(8));
        htmlPanel.addView(resultTitle);

        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setBackgroundColor(Color.WHITE);
        resultScroll.setPadding(dp(12), dp(12), dp(12), dp(12));

        renderView = new OeRRenderView(this);
        resultScroll.addView(renderView);

        htmlPanel.addView(resultScroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                ));

        renderButton.setOnClickListener(v ->
                renderView.renderHtml(
                        htmlInput.getText().toString()
                )
        );

        // Pasang panel ke frame.
        contentFrame.addView(homePanel,
                new FrameLayout.LayoutParams(-1, -1));

        contentFrame.addView(webView,
                new FrameLayout.LayoutParams(-1, -1));

        contentFrame.addView(htmlPanel,
                new FrameLayout.LayoutParams(-1, -1));

        root.addView(contentFrame,
                new LinearLayout.LayoutParams(-1, 0, 1));
    }

    // ==================== TAMPILAN ====================

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

        renderView.renderHtml(
                htmlInput.getText().toString()
        );
    }

    // ==================== PENCARIAN / ALAMAT ====================

    private void openAddress(EditText input) {
        String query = input.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(
                    this,
                    "Masukkan alamat atau pencarian",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String url;

        if (query.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            url = query;
        } else if (query.matches(
                "(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$"
        )) {
            url = "https://" + query;
        } else {
            try {
                url = "https://www.google.com/search?q=" +
                        java.net.URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Pencarian gagal",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        addressInput.setText(url);
        showBrowser();
        webView.loadUrl(url);
    }

    // ==================== TOMBOL KEMBALI HP ====================

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

    // ==================== RENDERER HTML SEDERHANA ====================

    private static class OeRRenderView extends LinearLayout {

        public OeRRenderView(
                android.content.Context context
        ) {
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

            String[] lines = html.split("\n");

            for (String raw : lines) {
                String line = raw.trim();

                if (line.isEmpty()) {
                    continue;
                }

                TextView tv = new TextView(getContext());
                tv.setTextColor(Color.BLACK);
                tv.setPadding(0, 4, 0, 4);

                if (line.startsWith("<h1>")
                        && line.endsWith("</h1>")) {

                    tv.setText(stripTags(line));
                    tv.setTextSize(24);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 12, 0, 8);

                } else if (line.startsWith("<h2>")
                        && line.endsWith("</h2>")) {

                    tv.setText(stripTags(line));
                    tv.setTextSize(20);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 10, 0, 6);

                } else if (line.startsWith("<p>")
                        && line.endsWith("</p>")) {

                    tv.setText(stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(0, 6, 0, 6);

                } else if (line.startsWith("<li>")
                        && line.endsWith("</li>")) {

                    tv.setText("•  " + stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(16, 3, 0, 3);

                } else if (line.equals("<hr>")
                        || line.equals("<hr/>")
                        || line.equals("<hr />")) {

                    View lineView = new View(getContext());
                    lineView.setBackgroundColor(
                            Color.rgb(200, 200, 200)
                    );

                    addView(lineView,
                            new LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    2
                            ));

                    continue;

                } else if (line.startsWith("<ul>")
                        || line.startsWith("</ul>")
                        || line.startsWith("<ol>")
                        || line.startsWith("</ol>")) {

                    continue;

                } else {
                    String clean = stripTags(line);

                    if (clean.isEmpty()) {
                        continue;
                    }

                    tv.setText(clean);
                    tv.setTextSize(15);
                }

                addView(tv);
            }
        }

        private String stripTags(String input) {
            return input.replaceAll("<[^>]+>", "")
                    .replace("&nbsp;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .trim();
        }
    }
}
