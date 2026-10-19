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
        input.setPadding(10, 4, 10, 4);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        return input;
    }

    // Huruf O dengan warna pelangi
    private class RainbowTextView extends TextView {
        public RainbowTextView(String text, int size) {
            super(MainActivity.this);
            setText(text);
            setTextSize(size);
            setTypeface(null, Typeface.BOLD);
            setGravity(Gravity.CENTER);
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
        logo.addView(oe);

        TextView r = makeText("R", size, green);
        r.setTypeface(null, Typeface.BOLD);
        logo.addView(r);

        parent.addView(logo);
    }

    // Ikon O pelangi untuk kolom pencarian
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

    private void createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        header.setPadding(12, 8, 14, 8);
        header.setBackgroundColor(black);

        Button account = makeButton("♙ Akun");
        account.setTextColor(green);
        account.setBackground(
            rounded(dark, 40)
        );

        account.setOnClickListener(v -> showAccountInfo());

        header.addView(account);
        root.addView(header,
            new LinearLayout.LayoutParams(-1, 54));
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
        addressRow.addView(addressInput,
            new LinearLayout.LayoutParams(0, 48, 1));

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

    private void createNavigation() {
        navRow = new LinearLayout(this);
        navRow.setGravity(Gravity.CENTER);
        navRow.setBackgroundColor(black);

        Button back = makeButton("‹ Kembali");
        Button refresh = makeButton("↻ Muat ulang");
        Button home = makeButton("⌂ Beranda");
        Button editor = makeButton("</> HTML");

        navRow.addView(back,
            new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(refresh,
            new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(home,
            new LinearLayout.LayoutParams(0, 46, 1));
        navRow.addView(editor,
            new LinearLayout.LayoutParams(0, 46, 1));

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

        // HOME
        homePanel = new LinearLayout(this);
        homePanel.setOrientation(LinearLayout.VERTICAL);
        homePanel.setGravity(Gravity.CENTER_HORIZONTAL);
        homePanel.setPadding(22, 20, 22, 20);
        homePanel.setBackgroundColor(black);

        View topSpace = new View(this);
        homePanel.addView(topSpace,
            new LinearLayout.LayoutParams(1, 0, 1));

        addLogo(homePanel, 48);

        TextView subtitle = makeText(
            "Jelajahi dunia dengan OoeR", 14, gray);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 30);
        homePanel.addView(subtitle);

        // Kolom pencarian
        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(8, 3, 5, 3);
        searchBar.setBackground(rounded(dark, 28));

        RainbowSearchIcon searchIcon = new RainbowSearchIcon();
        searchBar.addView(searchIcon,
            new LinearLayout.LayoutParams(38, 38));

        homeSearchInput = makeInput("Cari apa saja...");
        searchBar.addView(homeSearchInput,
            new LinearLayout.LayoutParams(0, 48, 1));

        Button mic = makeButton("🎙");
        mic.setTextSize(18);
        mic.setPadding(4, 0, 4, 0);
        mic.setBackgroundColor(Color.TRANSPARENT);
        searchBar.addView(mic,
            new LinearLayout.LayoutParams(42, 44));

        Button search = makeButton("↑");
        search.setTextSize(23);
        search.setTypeface(null, Typeface.BOLD);
        search.setTextColor(Color.BLACK);
        search.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(green)
        );
        search.setPadding(0, 0, 0, 2);

        searchBar.addView(search,
            new LinearLayout.LayoutParams(42, 42));

        LinearLayout.LayoutParams searchParams =
            new LinearLayout.LayoutParams(-1, 56);
        searchParams.setMargins(0, 0, 0, 0);
        homePanel.addView(searchBar, searchParams);

        search.setOnClickListener(v ->
            openAddress(homeSearchInput));

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

        mic.setOnClickListener(v ->
            Toast.makeText(
                this,
                "Fitur mikrofon akan dikembangkan",
                Toast.LENGTH_SHORT
            ).show()
        );

        // Bagian bawah tetap kosong
        View bottomSpace = new View(this);
        homePanel.addView(bottomSpace,
            new LinearLayout.LayoutParams(1, 0, 2));

        // BROWSER
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // EDITOR HTML
        htmlPanel = new LinearLayout(this);
        htmlPanel.setOrientation(LinearLayout.VERTICAL);
        htmlPanel.setPadding(12, 8, 12, 12);
        htmlPanel.setBackgroundColor(black);

        TextView htmlTitle = makeText(
            "Editor HTML OoeR", 18, green);
        htmlPanel.addView(htmlTitle);

        htmlInput = new EditText(this);
        htmlInput.setHint("Masukkan kode HTML di sini");
        htmlInput.setHintTextColor(gray);
        htmlInput.setTextColor(white);
        htmlInput.setGravity(Gravity.TOP | Gravity.START);
        htmlInput.setMinLines(4);
        htmlInput.setPadding(12, 12, 12, 12);
        htmlInput.setBackground(rounded(dark, 12));
        htmlInput.setText(
            "<h1>Selamat datang di OoeR</h1>" +
            "<p>Ini adalah mesin render HTML OoeR.</p>" +
            "<h2>Fitur awal</h2>" +
            "<ul><li>Judul</li><li>Paragraf</li>" +
            "<li>Daftar</li></ul><hr>" +
            "<p>Mesin akan terus dikembangkan.</p>"
        );

        htmlPanel.addView(htmlInput,
            new LinearLayout.LayoutParams(-1, 0, 1));

        Button renderButton = makeButton("Tampilkan HTML");
        renderButton.setTextColor(green);
        htmlPanel.addView(renderButton);

        TextView resultTitle = makeText(
            "Hasil render:", 15, white);
        resultTitle.setPadding(0, 8, 0, 6);
        htmlPanel.addView(resultTitle);

        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setBackgroundColor(Color.WHITE);

        renderView = new OeRRenderView(this);
        resultScroll.addView(renderView);

        htmlPanel.addView(resultScroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        renderButton.setOnClickListener(v ->
            renderView.renderHtml(
                htmlInput.getText().toString()
            )
        );

        contentFrame.addView(homePanel,
            new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(webView,
            new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(htmlPanel,
            new FrameLayout.LayoutParams(-1, -1));

        root.addView(contentFrame,
            new LinearLayout.LayoutParams(-1, 0, 1));
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

    private void loadWebsite(String url) {
        addressInput.setText(url);
        showBrowser();
        webView.loadUrl(url);
    }

    private void openAddress(EditText input) {
        String query = input.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this,
                "Masukkan alamat atau pencarian",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String url;

        if (query.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            url = query;
        } else if (query.matches(
                "(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$")) {
            url = "https://" + query;
        } else {
            try {
                url = "https://www.google.com/search?q="
                    + java.net.URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
                Toast.makeText(this, "Pencarian gagal",
                    Toast.LENGTH_SHORT).show();
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
                        }
            
