package d.project.dswebview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import d.project.dswebview.db.ContentVO;
import d.project.dswebview.db.DBManager;
import d.project.dswebview.widget.SwipeDismissListViewTouchListener;

/**
 * Created by JeongKuk on 2016-02-14.
 */
public class Product extends Activity {

    private EditText etContentID;
    private EditText etDesc;
    private ListView lvProduct;
    private ContentAdapter mAdapter;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_product);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.9), (int)(height*.9));

        dbManager = new DBManager(getApplicationContext(), "content.db", null, 1);

        initWidget();
        initList();
    }

    private List<ContentVO> selectContentList() {
        List<ContentVO> list = dbManager.selectList();
        return list;
    }

    private void initWidget() {

        etContentID = (EditText) findViewById(R.id.etContentID);
        etDesc = (EditText) findViewById(R.id.etDesc);
        etContentID.setText(getProduct());

        Button btnSetContentID = (Button) findViewById(R.id.btnSetContentID);
        btnSetContentID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentID = etContentID.getText().toString();
                if(contentID == null || contentID.isEmpty() || contentID.length() != 7) {
                    Toast.makeText(Product.this, "상품ID가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    setProduct(contentID, etDesc.getText().toString());
                    Toast.makeText(Product.this, "상품ID가 설정되었습니다.", Toast.LENGTH_SHORT).show();
//                    setResult(RESULT_FIRST_USER);
//                    finish();
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

                    pos = html.indexOf("title=");
                    if(pos == -1) return;

                    String title = "";
                    try {
                        title = html.substring(pos+6, html.indexOf("&ProductType"));
                        etDesc.setText(title);

                    } catch (Exception e) {
                        Log.e("frozenvoice", e.getMessage());
                    }
                }
            }
        });
    }

    private void initList() {
        lvProduct = (ListView) findViewById(R.id.lvProduct);

        List<ContentVO> list = selectContentList();
        for(ContentVO vo : list) {
            if(vo.getContentId().equals(etContentID.getText().toString())) {
                etDesc.setText(vo.getDesc());
            }
        }
        //새로운 ArrayAdapter를 생성한다.
        mAdapter = new ContentAdapter(this, R.layout.content_item, list);
        lvProduct.setAdapter(mAdapter);

        lvProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etContentID.setText(mAdapter.getItem(position).getContentId());
                etDesc.setText(mAdapter.getItem(position).getDesc());
            }
        });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        lvProduct,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    dbManager.delete(mAdapter.getItem(position).getContentId());
                                }
                                mAdapter = new ContentAdapter(Product.this, R.layout.content_item, selectContentList());
                                lvProduct.setAdapter(mAdapter);
                            }
                        });
        lvProduct.setOnTouchListener(touchListener);
        lvProduct.setOnScrollListener(touchListener.makeScrollListener());
    }

    private String getProduct() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("contentID", "");
    }

    private void setProduct(String contentID, String desc) {

        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("contentID", contentID);
        editor.commit();

        List<ContentVO> list = selectContentList();

        boolean hasValue = false;
        for(ContentVO vo : list) {
            if(contentID.equals(vo.getContentId())) {
                dbManager.update(contentID, desc);
                hasValue = true;
                break;
            }
        }

        if(!hasValue) {
            dbManager.insert(contentID, desc);
        }

        mAdapter = new ContentAdapter(Product.this, R.layout.content_item, selectContentList());
        lvProduct.setAdapter(mAdapter);
    }

    private String getHtml() {
        SharedPreferences preferences = getSharedPreferences("cowboom", 0);
        return preferences.getString("html", "");
    }
}
