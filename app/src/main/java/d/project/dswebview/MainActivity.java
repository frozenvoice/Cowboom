package d.project.dswebview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String ADDR_HOME = "http://www.cowboom.com/";
    private final String ADDR_REQ = "http://www.cowboom.com/store/ProductBestAvailable.cfm?contentID=$contentID&requestKey=$requestKey";
    private final String ADDR_LIST = "http://www.cowboom.com/act/actCart.cfm?action=addLotToCart&ConditionID=12&key=DC3DDF5141EA44AE3656C894E4CF486A&contentID=$contentID&optionID=$optionID&lotID=$lotID";
    private WebView wv;
    private EditText etAddr;
    private FloatingActionButton fab;
    private ProgressBar progressBar;

    private final int STEP_INIT = -1;
    private final int STEP_READY = 0;
    private final int STEP_REQ = 1;
    private final int STEP_LIST = 2;
    private final int STEP_CART = 3;
    private final int STEP_RUNNING = 6;

    private int step = STEP_INIT;

    private final int GO_PRODUCT_PAGE = 55;
    private final int GO_PRODUCT_LIST_PAGE = 56;

    private boolean statusRunning = false;
    private boolean statusFinish = false;
    private int runningPosition = 0;
    private int addCnt = 0;

    private CowboomVo cowboomVo = new CowboomVo();
    private List<Map<String, String>> list = new ArrayList<>();
    private Map<String, String> map = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        step = STEP_INIT;

        if(getPrice() == null) {
            setPrice("100");
        }

        initWebView();
        initWidget();
    }

    private void initWebView() {
        wv = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);

        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);

        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setScrollbarFadingEnabled(false);

//        String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        String agent = "Mozilla/5.0";
        wv.getSettings().setUserAgentString(agent);
        wv.addJavascriptInterface(new DsJavascriptInterface(), "Android");

        wv.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            }
        });

        wv.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
                etAddr.setText(url);

                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
            }
        });

        wv.loadUrl(ADDR_HOME);
//        wv.loadUrl("javascript:window.HTMLOUT.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
    }

    private void initWidget() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                step = STEP_INIT;
                if(statusRunning) {
                    stopRunning(fab, true);
                } else {
                    startRunning(fab, true);
                    wv.reload();
                }
            }
        });

        etAddr = (EditText) findViewById(R.id.etAddr);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etAddr.getWindowToken(), 0);

        etAddr.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String url = v.getText().toString();
                    if(!url.trim().startsWith("http://")) {
                        url = "http://" + url.trim();
                    }
                    wv.loadUrl(url);
                    hideKeyboard();
                }
                return false;
            }
        });

        ImageButton btnClear = (ImageButton) findViewById(R.id.btnClear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAddr.requestFocus();
                etAddr.setText("");
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(etAddr, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etAddr.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if(statusRunning) {
            stopRunning(fab, true);
            return;
        }

        wv.stopLoading();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(wv.canGoBack()) {
            step = STEP_INIT;
            stopRunning(fab, false);
            wv.goBack();
        } else if(statusFinish) {
            super.onBackPressed();
        } else {
            Toast.makeText(MainActivity.this, "한번 더 누르면 종료", Toast.LENGTH_SHORT).show();
            statusFinish = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {statusFinish = false;}
            }, 2000);
        }
    }

    private void startRunning(View v, boolean showMsg) {
        statusRunning = true;
        runningPosition = 0;
        addCnt = 0;
        list = new ArrayList<>();
        fab.setImageResource(android.R.drawable.ic_media_pause);
        if(showMsg) {
            Snackbar.make(v, "작업시작", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void stopRunning(View v, boolean showMsg) {
        statusRunning = false;
        runningPosition = 0;
        addCnt = 0;
        fab.setImageResource(android.R.drawable.ic_media_play);
        if(showMsg) {
            Snackbar.make(v, "작업중지", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            stopRunning(fab, false);
            step = STEP_INIT;
            wv.loadUrl(ADDR_HOME);
            return true;
        } else if (id == R.id.action_finish) {
            statusRunning = false;
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_product) {   // 상품코드설정
            startActivityForResult(new Intent(MainActivity.this, Product.class), GO_PRODUCT_PAGE);
        } else if (id == R.id.nav_product_list) { // 상품목록이동
            goProductList();
        } else if (id == R.id.nav_price) {  // 금액설정
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogEdit = inflater.inflate(R.layout.dialog_edit, null);
            final EditText etPrice = (EditText)dialogEdit.findViewById(R.id.etTemp);
            etPrice.setText(getPrice());
//            etPrice.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
            etPrice.setEms(10);
            builder.setView(dialogEdit)
                    .setTitle("금액을 입력하세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPrice(etPrice.getText().toString());
                            Toast.makeText(MainActivity.this, "금액이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        } else if (id == R.id.nav_email) {  // 이메일설정
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogEdit = inflater.inflate(R.layout.dialog_edit, null);
            final EditText etEmail = (EditText)dialogEdit.findViewById(R.id.etTemp);
            etEmail.setText(getEmail());
            etEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etEmail.setEms(50);
            builder.setView(dialogEdit)
                    .setTitle("E-MAIL 을 입력하세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setEmail(etEmail.getText().toString());
                            Toast.makeText(MainActivity.this, "E-MAIL이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class DsJavascriptInterface {

        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            Log.d("frozenvoice", "step : " + step);
            setHtml(html);

            System.out.println(html);

            if(html.contains("requestKey")) {
                int pos = html.indexOf("requestKey");
                String requestKey = html.substring(pos + 11, pos + 43);
                Log.d("frozenvoice", "reqKey!  = " + requestKey);
                setRequestKey(requestKey);
            }

            if(step == STEP_INIT && html.contains("ProdPrice") && html.contains("optionID") && html.contains("lotID")) {

                addList();
            }

            if(statusRunning) {
                handler.sendEmptyMessage(STEP_RUNNING);
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case STEP_REQ:
                    wv.loadUrl(cowboomVo.getMoveAddr());
                    step = STEP_LIST;
                    break;
                case STEP_CART:
                    wv.loadUrl(cowboomVo.getMoveAddr());
                    sendEmail();
                    addCnt++;
                    break;
                case STEP_RUNNING:

                    if(list.size() == 0 && addCnt == 0) {

                        // 현재 페이지에 상품이 없을 경우 반복
                        wv.reload();
                        break;
                    } else if(runningPosition >= list.size()) {
                        stopRunning(fab, true);
                        break;
                    }

                    Map<String, String> stringStringMap = list.get(runningPosition);
                    runningPosition++;

                    String price = stringStringMap.get("price");
                    String optionID = stringStringMap.get("optionID");
                    String lotID = stringStringMap.get("lotID");
                    String moveAddr = ADDR_LIST.replace("$contentID", getProduct());
                    moveAddr = moveAddr.replace("$optionID", optionID);
                    moveAddr = moveAddr.replace("$lotID", lotID);
                    Log.d("frozenvoice", "moveAddr!  = " + moveAddr);

                    cowboomVo.setMoveAddr(moveAddr);

                    if(new BigDecimal(getPrice()).compareTo(new BigDecimal(price)) > 0) {
                        handler.sendEmptyMessage(STEP_CART);
                    } else {
                        handler.sendEmptyMessage(STEP_RUNNING);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GO_PRODUCT_PAGE) {
            if(resultCode == RESULT_OK) {
                wv.loadUrl("http://www.cowboom.com/product/" + getProduct());
            } else if(resultCode == RESULT_FIRST_USER) {
//                goProductList();
            }
        }
    }

    private void goProductList() {
        String moveAddr = ADDR_REQ.replace("$contentID", getProduct());
        moveAddr = moveAddr.replace("$requestKey", getRequestKey());
        Log.d("frozenvoice", "list page moveAddr!  = " + moveAddr);
        step = STEP_READY;
        wv.loadUrl(moveAddr);
    }

    private String getProduct() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("contentID", "");
    }

    private void setProduct(String contentID) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("contentID", contentID);
        editor.commit();
    }

    private String getRequestKey() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("requestKey", "");
    }

    private void setRequestKey(String requestKey) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("requestKey", requestKey);
        editor.commit();
    }

    private String getOptionId() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("optionId", "");
    }

    private void setOptionId(String optionId) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("optionId", optionId);
        editor.commit();
    }

    private String getLotId() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("lotId", "");
    }

    private void setLotId(String lotId) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("lotId", lotId);
        editor.commit();
    }

    private String getPrice() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("price", "0");
    }

    private void setPrice(String price) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("price", price);
        editor.commit();
    }

    private String getEmail() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("email", "");
    }

    private void setEmail(String email) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("email", email);
        editor.commit();
    }

    private String getHtml() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("html", "");
    }

    private void setHtml(String html) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("html", html);
        editor.commit();
    }

    private void addList() {
        Scanner scanner = new Scanner(getHtml());
        list = new ArrayList<>();
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if(line.contains("div class=\"ProdPrice")) {
                int pos = line.indexOf("$");
                int endPos = line.indexOf("</div>");
                String price = line.substring(pos + 1, endPos);
                map.put("price", price);
                System.out.println(price);
            }

            if(line.contains("optionID\" value=")) {
                int pos = line.indexOf("optionID");
                int endPos = line.indexOf("\">");
                String optionID = line.substring(pos + 17, endPos);
                map.put("optionID", optionID);
                System.out.println(optionID);
            }

            if(line.contains("lotID\" value=")) {
                int pos = line.indexOf("lotID");
                int endPos = line.indexOf("\">");
                String lotID = line.substring(pos + 14, endPos);
                map.put("lotID", lotID);
                System.out.println(lotID);
            }

            if(map.containsKey("price") && map.containsKey("optionID") && map.containsKey("lotID")) {

                list.add(map);
                map = new HashMap<>();
            }
        }
    }

    private class SendEmailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                Log.d("frozenvoice", "params .. " + params[0] + "," + params[1] + "," + params[2]);
                MailSender sender = new MailSender("frozenvoice83@gmail.com", "tjfgml0903"); // SUBSTITUTE ID PASSWORD
                sender.setFrom("frozenvoice83@gmail.com");
                sender.setTo(new String[]{params[0]});
                sender.setSubject(params[1]);
                sender.setBody(params[2]);
                sender.send();

            } catch (Exception e) {
                Log.e("frozenvoice", "send email error " + e.getMessage());
            }
            return null;
        }
    }

    private void sendEmail() {
        if(getEmail() != null && !getEmail().isEmpty()) {
            new SendEmailTask().execute(getEmail(),
                    "상품이 카트에 담겼습니다.",
                    "상품 : " + getProduct() + "\n주소 : " + cowboomVo.getMoveAddr());
        }
    }
}
