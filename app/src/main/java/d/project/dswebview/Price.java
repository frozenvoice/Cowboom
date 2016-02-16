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
public class Price extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_price);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.3));

        initWidget();
    }

    private void initWidget() {

        final EditText etPrice = (EditText) findViewById(R.id.etPrice);
        etPrice.setText(getPrice());

        Button btnPrice = (Button) findViewById(R.id.btnPrice);
        btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = etPrice.getText().toString();
                setPrice(price);
                Toast.makeText(Price.this, "가격 설정됨", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
}
