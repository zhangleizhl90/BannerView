package me.zhanglei.bannerview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.zhanglei.widgets.BannerView;

public class MainActivity extends AppCompatActivity {

    BannerView mBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBannerView = (BannerView) findViewById(R.id.banner_view);
        List<Bitmap> bitmapList = new ArrayList<>(2);
        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.res_1));
        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.res_2));
        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.res_3));
        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.res_4));
        mBannerView.update(bitmapList);
        mBannerView.setOnItemClickListener(new BannerView.OnItemClickListener() {
            @Override
            public void onItemClick(int index) {
                Toast.makeText(MainActivity.this, "Click " + index, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBannerView.startAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBannerView.startAutoScroll();
    }
}
