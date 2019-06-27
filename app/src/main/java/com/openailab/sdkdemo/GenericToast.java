package com.openailab.sdkdemo;



import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;



    public class GenericToast{

        private static final String TAG = "GenericToast";



        private static final int TOAST_TEXTSIZE = 40;



        /** {@link Toast#LENGTH_SHORT} default time is 3500ms */

        private static final int LENGTH_SHORT_TIME = 2000;



        private static Context mContext = null;



        private static Toast mToast = null;

        private static TextView mTextView = null;

        private static int mDuration = 0;

        private static CharSequence mText = null;



        private Handler mHandler = new Handler();



        private GenericToast(Context context) {

            mContext = context;

        }



        public static GenericToast makeText(Context context, CharSequence text, int duration){

            GenericToast instance = new GenericToast(context);

            mContext = context;

            mDuration = duration;

            mText = text;

            return instance;

        }



        private static void getToast(Context context, CharSequence text){

            mToast = Toast.makeText(context, null, Toast.LENGTH_LONG);

            mToast.setGravity(Gravity.CENTER, 0, 0);

            LinearLayout toastView = (LinearLayout)mToast.getView();



            // Get the screen size with unit pixels.

            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

            DisplayMetrics outMetrics = new DisplayMetrics();

            wm.getDefaultDisplay().getMetrics(outMetrics);



            mTextView = new TextView(context);

            LayoutParams vlp = new LayoutParams(outMetrics.widthPixels/4,

                    outMetrics.heightPixels/4);

            vlp.setMargins(0, 0, 0, 0);

            mTextView.setLayoutParams(vlp);

            mTextView.setTextSize(TOAST_TEXTSIZE);

            mTextView.setText(text);

            mTextView.setGravity(Gravity.CENTER);

            toastView.addView(mTextView);

        }



        /**

         * Before call this method, you should call {@linkmakeText}.

         *

         * @return Toast display duration.

         */

        public int getDuration(){

            return mDuration;

        }



        public void show(){

            Log.d(TAG, "Show custom toast");

            mHandler.post(showRunnable);

        }



        public void hide(){

            Log.d(TAG, "Hide custom toast");

            mDuration = 0;

            if(mToast != null){

                mToast.cancel();

            }

        }



        private Runnable showRunnable = new Runnable(){

            @Override

            public void run() {

                if(mToast != null){

                    mTextView.setText(mText);

                }else{

                    getToast(mContext, mText);

                }

                if(mDuration != 0){

                    mToast.show();

                }else{

                    Log.d(TAG, "Hide custom toast in runnable");

                    hide();

                    return;

                }



                if(mDuration > LENGTH_SHORT_TIME){

                    mHandler.postDelayed(showRunnable, LENGTH_SHORT_TIME);

                    mDuration -= LENGTH_SHORT_TIME;

                }else{

                    mHandler.postDelayed(showRunnable, mDuration);

                    mDuration = 0;

                }

            }

        };

    }


