package me.zhanglei.bannerview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.zhanglei.widgets.BannerView
import java.util.*

class MainActivity : AppCompatActivity() {

    val mBannerView:BannerView? = null
    var bitmapList: List<Bitmap> = ArrayList(4)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mBannerView = findViewById(R.id.banner_view) as BannerView
//        bitmapList += (BitmapFactory.decodeResource(getResources(), R.drawable.res_1));
//        bitmapList += (BitmapFactory.decodeResource(getResources(), R.drawable.res_2));
//        bitmapList += (BitmapFactory.decodeResource(getResources(), R.drawable.res_3));
//        bitmapList += (BitmapFactory.decodeResource(getResources(), R.drawable.res_4));
//        val baseAdapter = object:BannerView.BaseAdapter() {
//            override val count: Int
//                get() = bitmapList.size
//
//            override fun getItemAt(index: Int): Bitmap {
//                return bitmapList[index]
//            }
//        }
        val baseAdapter = object:BannerView.BaseAdapter() {
            override fun getCount(): Int {
                return bitmapList.size;
            }

            override fun getItemAt(index: Int): Bitmap {
                return bitmapList[index]
            }
        }
        mBannerView.setAdapter(baseAdapter)
        mBannerView.startAutoScroll()
        mBannerView.setOnItemClickListener { index: Int -> Toast.makeText(this@MainActivity, "Click " + index, Toast.LENGTH_SHORT).show() }

        findViewById(R.id.button).setOnClickListener { view ->
            bitmapList += (BitmapFactory.decodeResource(resources, R.drawable.res_1))
            bitmapList += (BitmapFactory.decodeResource(resources, R.drawable.res_2))
            bitmapList += (BitmapFactory.decodeResource(resources, R.drawable.res_3))
            bitmapList += (BitmapFactory.decodeResource(resources, R.drawable.res_4))
            baseAdapter.notifyDataSetChanged()
        }

    }

    override fun onResume() {
        super.onResume()

        mBannerView?.startAutoScroll()
    }

    override fun onPause() {
        super.onPause()

        mBannerView?.startAutoScroll()
    }
}
