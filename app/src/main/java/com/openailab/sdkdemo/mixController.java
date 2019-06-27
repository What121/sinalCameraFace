package com.openailab.sdkdemo;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



/**
 * Created by user on 2017/12/18.
 */

public class mixController  {
    public static final String START_TRACK="START_TRACK";
    public static final String EXIT="EXIT_TAG";
    public static final String REGISTE_FACE="REGISTER";
    public static final String RECOGNIZE_FACE="RECOGNIZE";
    public static final String DELETE_FACE="DELETE";
    public static final String STOP_TRACK="STOP_TRACK";
    public static final String RESET_ROBOT="RESET";
    public static final String MANAGE="MANAGE";
    public static final String SAY_YES="YES";
    public static final String SAY_NO ="NO";
    public static final String GESTURE ="GESTURE" ;
    public static final String READY="我准备好了";
    public static final String M_ASK_NAME="我不认识你，你叫什么名字";

    public static final String  M_TRACK_ERR0_NONE="我看不到你了";
    public static final String  M_PLAY_U="我们一起玩吧";
    public static final String  M_KNOW_U="我认识你";
    public static final String  M_DELETE_U="你要删除谁了";
    public static final String  M_DELETE_OK="删除成功";
    public static final String  M_DELETE_WRONG="你要删除的人不存在";

    public static final int STATE_NO=-1;
    public static final int STATE_REGISTER_ADMIN=0;
    public static final int STATE_FACE_REGISTER=1;
    public static final int STATE_FACE_RECOGNIZE=2;
    public static final int STATE_IDLE=3;
    public static final int STATE_TRACK_START=4;
    public static final int STATE_TRACK_STOP=5;
    public static final int STATE_TRACKING=6;
    public static final int STATE_NAME_ASK=7;
    public static final int STATE_NAME_CONFIRM=8;
    public static final int STATE_NAME_KNOWN_YOU=9;
    public static final int STATE_DELETE=10;
    public static final int STATE_DELETE_NAME=11;
    public static final int STATE_DELETE_CONFIRM=12;
    public static final int STATE_GESTURE_DETECT=13;
    public static final int STATE_GESTRUE_OR_TRACK=14;
    //public static final int STATE_IDLE=15;

    public  static int curState=STATE_IDLE;
    public static int prevState=STATE_NO;
    private String gotNmae=null;
    private static String TAG="mixController";

    private static mixController mController;
    private Context mContext;
    public Handler mHandler;
    private String wakeupGrammarFile = "test_offline";

    final Handler handler = new Handler();
    private mixController(Context mCon){



       // mVUI.setTTSListener(this);
        mContext=mCon;


    }
    public  static mixController getInstance(Context mContext) {
        if(mController==null){
            mController=new mixController(mContext);
        }
        return mController;
    }
    public  static void setState(int sta,int pre){
        curState=sta;
        prevState=pre;
        Log.d("morrisdebug","ASRResult setstate  cursta="+sta+"pre"+pre);
    }






   public void destroyController(){
       // if (mVUI != null) {
       //      mVUI.release();
       //  }

       mController=null;

   }


}
