package com.openailab.sdkdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class myDrawRectView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    private  static  final  String TAG="SurfaceView";
    //SurfaceHolder
    private SurfaceHolder mHolder;
    //用于绘图的Canvas
    private Canvas mCanvas;
    //子线程标志位
    private boolean mIsDrawing;
    //画笔
    private Paint mPaint;
    private Paint tpaint;
    //路径
    private Path mPath;
    private int MAX_SIZE = 32;
    private int[] x0=new int[MAX_SIZE],x1=new int[MAX_SIZE],y0=new int[MAX_SIZE],y1=new int[MAX_SIZE];
    private boolean drawf=false;
    private int thick = 1;
    private String name;
    private float fps;
    public myDrawRectView(Context context) {
        super(context);
        initView();
    }


    public myDrawRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public myDrawRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mHolder = getHolder();
        //添加回调
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mPath=new Path();
        //初始化画笔
        mPaint=new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(thick);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);

        tpaint=new Paint();
        tpaint.setColor(Color.GREEN);
        tpaint.setAntiAlias(true);
        tpaint.setTextAlign(Paint.Align.LEFT);//设置从右变开始写字
        tpaint.setTextSize(25);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

       // bitmap = BitmapFactory.decodeFile("/sdcard/ok.png");

    }
    //Surface的生命周期
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing=true;
        new Thread(this).start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing=false;

    }

    @Override
    public void run() {
        long start =System.currentTimeMillis();
        while(mIsDrawing){
            draw();
            long end = System.currentTimeMillis();
            if(end-start<100){
                try{
                    Thread.sleep(100-end+start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void updateRect(int left, int top, int right, int bottom, int num)
    {
        x0[num] = left;
        y0[num] = top;
        x1[num] = right;
        y1[num] = bottom;
    }
    public void updateDrawFlag(boolean drawflag,String name,float fps)
    {
        drawf = drawflag;
        this.fps = fps;
        if(name!=null)
            this.name = name;
    }
    private void draw() {
        try{
            //锁定画布并返回画布对象
            mCanvas=mHolder.lockCanvas();
            //接下去就是在画布上进行一下draw
            //mCanvas.drawColor(Color.WHITE);
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for(int i = 0; i < MAX_SIZE; i ++) {
                int w = (x1[i] - x0[i]) / 10;
                int h = (y1[i] - y0[i]) / 10;
//                int h = (x1[i] - x0[i]) / 10;
//                int w = (y1[i] - y0[i]) / 10;
                //mCanvas.drawRect(new Rect(x0,y0,x1,y1),mPaint);
                mCanvas.drawRect(new Rect(x0[i], y0[i], x0[i] + w, y0[i] + thick), mPaint);
                mCanvas.drawRect(new Rect(x0[i], y0[i], x0[i] + thick, y0[i] + h), mPaint);
                mCanvas.drawRect(new Rect(x1[i] - w, y0[i], x1[i], y0[i] + thick), mPaint);
                mCanvas.drawRect(new Rect(x1[i] - thick, y0[i], x1[i], y0[i] + h), mPaint);
                mCanvas.drawRect(new Rect(x0[i], y1[i] - thick, x0[i] + w, y1[i]), mPaint);
                mCanvas.drawRect(new Rect(x0[i], y1[i] - h, x0[i] + thick, y1[i]), mPaint);
                mCanvas.drawRect(new Rect(x1[i] - w, y1[i] - thick, x1[i], y1[i]), mPaint);
                mCanvas.drawRect(new Rect(x1[i] - thick, y1[i] - h, x1[i], y1[i]), mPaint);
            }
            if(drawf){
                Path path = new Path();
                path.moveTo(x0[0],y0[0]);
                path.lineTo(x1[0]*2,y0[0]);
                mCanvas.drawTextOnPath(name,path,0,0,tpaint);
            }
            mCanvas.drawText("fps: "+String.valueOf(fps),60,100,tpaint);

        }catch (Exception e){
        }finally {
            //当画布内容不为空时，才post，避免出现黑屏的情况。
            if(mCanvas!=null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 清屏
     * @return true
     */
    public boolean reDraw(){
        mPath.reset();
        return true;
    }
}
