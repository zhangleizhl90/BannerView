package me.zhanglei.bannerview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.zhanglei.widgets.BannerView
import java.util.*

class MainActivity : AppCompatActivity() {

    val mBannerView: BannerView? = null
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
        val baseAdapter = object : BannerView.BaseAdapter() {
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
            bitmapList = ArrayList(4)
            bitmapList += loadBitmap(R.drawable.res_1, mBannerView.width)
            bitmapList += loadBitmap(R.drawable.res_2, mBannerView.width)
            bitmapList += loadBitmap(R.drawable.res_3, mBannerView.width)
            bitmapList += loadBitmap(R.drawable.res_4, mBannerView.width)
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

    fun loadBitmap(resId: Int, viewWidth: Int): Bitmap {
        val options = BitmapFactory.Options() // 加载和显示图片是很消耗内存的，Options 类允许我们定义图片以何种方式读到内存
        options.inJustDecodeBounds = true     // 不解析真实的位图，只是获取这个位图的边界等信息。这是关键！
        val displayMetrics = resources.displayMetrics
        options.inTargetDensity = displayMetrics.densityDpi
        options.inScaled = true
        BitmapFactory.decodeResource(resources, resId, options)
        val bitmapWidth = options.outWidth
        val bitmapHeight = options.outHeight

        options.inSampleSize = calculateInSampleSize(options, viewWidth);
        options.outWidth = viewWidth
        options.outHeight = ((1.0f * viewWidth / bitmapWidth) * bitmapHeight).toInt();
        options.inJustDecodeBounds = false // 加载图片
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int): Int {
        // 原始图片的宽高
        val width = options.outWidth
        var inSampleSize = 1
        if (width > reqWidth) {
            val halfWidth = width / 2;
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
