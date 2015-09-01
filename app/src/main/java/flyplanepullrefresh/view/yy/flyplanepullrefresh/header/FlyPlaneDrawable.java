package flyplanepullrefresh.view.yy.flyplanepullrefresh.header;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author yangyang
 * Date 15/8/31
 * Package in.srain.cube.views.ptr.demo.ui.flyplane
 * Version 1.0.0
 * TODO
 */
public class FlyPlaneDrawable extends Drawable implements Animatable {
    private static final int ANIMATION_DURATION = 2000;

    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private View mParent;
    private Animation mAnimation;
    private int mTop;
    private Paint mPaint;
    private Context mContext;
    private List<Bitmap> bitmaps;

    private int mTotalDragDistance;
    private int bmpIndex = 0;
    private int lastBmpIndex = -1;
    private float mPercent = 0f;
    private Matrix matrix;
    private int mScreenWidth;

    private boolean isPlay = false;

    public FlyPlaneDrawable(Context context, View parent) {
        mContext = context;
        mParent = parent;
        init();
        initAnimations();
    }

    private Context getContext() {
        return mContext;
    }

    /**
     * 初始化动画，在加载过程中使用
     */
    private void initAnimations() {
        mAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setPercent(interpolatedTime);
            }
        };
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.RESTART);
        mAnimation.setInterpolator(LINEAR_INTERPOLATOR);
        mAnimation.setDuration(ANIMATION_DURATION);
    }

    private void init() {
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        /**
         *248和1080是原本刷新图片的高和宽，可以根据自己的图片修改
         */
        mTotalDragDistance = (int) (248f * ((float) mScreenWidth / 1080f));
        matrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //从assets中读取图片的线程，直接读取因为图片太多会导致UI卡顿
        new LoadBitmapThread().start();
    }


    /**
     * 从assets中读取图片
     *
     * @param fileName
     * @return
     */
    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getContext().getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 刷新需要拖动的总的高度
     *
     * @return
     */
    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    public void offsetTop(int offset) {
        mTop = offset;
    }

    /**
     * 设置图片切换进度的百分比，根据百分比确定要绘制的图片是哪一张
     *
     * @param percent
     */
    public void setPercent(float percent) {
        if (bitmaps == null)
            return;

        mPercent = percent;
        if (percent > 1) {
            mPercent = percent - 1;
            mPaint.setAlpha(255);
        } else {
            mPaint.setAlpha((int) (mPercent * 255));
        }

        if (isPlay)
            mPaint.setAlpha(255);

        bmpIndex = (int) (bitmaps.size() * mPercent);
        if (bmpIndex >= bitmaps.size())
            bmpIndex = bitmaps.size() - 1;

        setBmpIndex(bmpIndex);
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmaps == null || bitmaps.size() <= 0 || bmpIndex >= bitmaps.size())
            return;
        if (lastBmpIndex != -1 && lastBmpIndex == bmpIndex) {
            return;
        }
        final int saveCount = canvas.save();
        canvas.translate(0, mTotalDragDistance - mTop);
        canvas.drawColor(Color.parseColor("#0084f6"));
        drawBitmap(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void drawBitmap(Canvas canvas) {
        matrix.reset();
        matrix.setTranslate(0, mTop - mTotalDragDistance + 22 * getContext().getResources().getDisplayMetrics().density);
        canvas.drawBitmap(bitmaps.get(bmpIndex), matrix, mPaint);
    }

    public void resetOriginals() {
        mTop = 0;
        bmpIndex = 0;
        lastBmpIndex = -1;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, mTop + top);
    }

    private void setBmpIndex(int index) {
        bmpIndex = index;
        mParent.invalidate();
        invalidateSelf();
    }

    @Override
    public void start() {
        isPlay = true;
        mAnimation.reset();
        mParent.startAnimation(mAnimation);
    }


    @Override
    public void stop() {
        isPlay = false;
        mParent.clearAnimation();
        resetOriginals();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    /**
     * 图片加载线程
     */
    private class LoadBitmapThread extends Thread {
        @Override
        public void run() {
            super.run();
            String fileName;
            ArrayList<Bitmap> list = new ArrayList<Bitmap>();
            AssetManager am = getContext().getResources().getAssets();
            InputStream is;
            for (int i = 1; i < 76; i++) {
                fileName = "flyplane/xialashuaxin" + i + ".png";
                try {
                    is = am.open(fileName);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    if (bmp != null) {
                        bmp = Bitmap.createScaledBitmap(bmp, mScreenWidth, mTotalDragDistance, true);
                        list.add(bmp);
                    }
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bitmaps = list;
        }
    }
}
