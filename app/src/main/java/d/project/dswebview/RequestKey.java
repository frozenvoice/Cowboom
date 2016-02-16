package d.project.dswebview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by JeongKuk on 2016-02-14.
 */
public class RequestKey extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_requestkey);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.5));

        initWidget();
    }

    private void initWidget() {

        final EditText etRequestKey = (EditText) findViewById(R.id.etRequestKey);
        etRequestKey.setText(getRequestKey());

        Button btnSetRequestKey = (Button) findViewById(R.id.btnSetRequestKey);
        btnSetRequestKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestKey = etRequestKey.getText().toString();
                if(requestKey == null || requestKey.isEmpty() || requestKey.length() != 32) {
                    Toast.makeText(RequestKey.this, "요청키가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    setRequestKey(requestKey);
                    Toast.makeText(RequestKey.this, "요청키 설정됨", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 상품목록페이지 이동
        Button btnGoProductPage = (Button) findViewById(R.id.btnGoProductListPage);
        btnGoProductPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // 현재 페이지에서 상품ID 가져오기
        Button btnGetRequestKey = (Button) findViewById(R.id.btnGetRequestKey);
        btnGetRequestKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String html = getHtml();

                if(html.isEmpty()) {
                    Toast.makeText(RequestKey.this, "HTML 소스 불러오기 오류", Toast.LENGTH_SHORT).show();
                } else {
                    int pos = html.indexOf("requestKey");
                    if(pos == -1) {
                        Toast.makeText(RequestKey.this, "현재페이지에 요청키가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String requestKey = html.substring(pos + 11, pos + 43);
                    etRequestKey.setText(requestKey);
                }
            }
        });
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

    private String getHtml() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("html", "");
    }
}
