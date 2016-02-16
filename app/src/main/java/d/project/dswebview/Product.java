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
public class Product extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_product);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.5));

        initWidget();
    }

    private void initWidget() {

        final EditText etContentID = (EditText) findViewById(R.id.etContentID);
        etContentID.setText(getProduct());

        Button btnSetContentID = (Button) findViewById(R.id.btnSetContentID);
        btnSetContentID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentID = etContentID.getText().toString();
                if(contentID == null || contentID.isEmpty() || contentID.length() != 7) {
                    Toast.makeText(Product.this, "상품ID가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    setProduct(contentID);
                    Toast.makeText(Product.this, "상품ID 설정됨", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 상품페이지 이동
        Button btnGoProductPage = (Button) findViewById(R.id.btnGoProductPage);
        btnGoProductPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // 현재 페이지에서 상품ID 가져오기
        Button btnGetContentID = (Button) findViewById(R.id.btnGetContentID);
        btnGetContentID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String html = getHtml();

                if(html.isEmpty()) {
                    Toast.makeText(Product.this, "HTML 소스 불러오기 오류", Toast.LENGTH_SHORT).show();
                } else {
                    int pos = html.indexOf("contentID");
                    if(pos == -1) {
                        Toast.makeText(Product.this, "현재페이지에 상품ID가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String contentID = html.substring(pos+10, pos+17);
                    etContentID.setText(contentID);
                }
            }
        });
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

    private String getHtml() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("html", "");
    }
}
