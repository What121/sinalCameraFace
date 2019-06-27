package com.openailab.sdkdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.openailab.facelibrary.FaceAPP;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GetSDTreeActivity extends Activity {
    //根目录
    File sdRoot;
    //存放当前路径下的所有文件及文件夹
    File[] datas;
    //当前路径
    File nowDir;
    //图片列表
    ArrayList<File> imageFileList;
    ListView listView;
    Button input_imgs_btn;
    ImageButton return_imgs_btn;
    TextView dir_path, showImportText;
    int lastPoint = 0;
    boolean isBack = false;
    MyAdapater adapater;
    String[] fileTypes = new String[]{"jpg", "png", "jpeg", "bmp"};
    static MyHandler mHandler;
    static boolean IS_CANCEL ;

    private final static int MAX_REGISTER= FileOperator.MAX_REGISTER;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filebrowser);
        listView = (ListView) findViewById(R.id.list);
        input_imgs_btn = (Button) findViewById(R.id.input_imgs_btn);
        dir_path =(TextView)findViewById(R.id.dir_path);
        showImportText = (TextView) findViewById(R.id.showImportText);
        return_imgs_btn = (ImageButton) findViewById(R.id.return_imgs_btn);

        mHandler = new MyHandler(this);


        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                File clickFile = datas[arg2];
                if(clickFile==null){
                    return;
                }
                Log.i("test", clickFile.getName());
                if (clickFile.isDirectory()) {
                    lastPoint = arg2;
                    loadFiles(clickFile);
                } else {
                    //  openFile(clickFile);
                }
            }
        });

        input_imgs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImportText.setVisibility(View.VISIBLE);
                new BatchRegisterTask(GetSDTreeActivity.this).execute(nowDir.getAbsolutePath());

            }
        });


        return_imgs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parent = nowDir.getParent();
                if (parent==null||parent.equals("/")) {
                    GetSDTreeActivity.this.finish();
                    return;
                }
                isBack = true;
                loadFiles(new File(parent));
            }
        });
        sdRoot = new File("/sdcard");
        if (sdRoot.exists()) {
            loadFiles(sdRoot);
        }


    }
    static class BatchRegisterTask extends AsyncTask<String,Integer,String> {
        private WeakReference<GetSDTreeActivity> mActivty;
        private FaceAPP face;
        private ProgressDialog pd = null;

        public BatchRegisterTask(GetSDTreeActivity activity){
            mActivty = new WeakReference<GetSDTreeActivity>(activity);
            face= FaceAPP.GetInstance();
            pd = new ProgressDialog(activity);
            pd.setCanceledOnTouchOutside(false);
        }
        @Override
        protected void onPreExecute() {
            pd.setTitle("批量注册");
            pd.setMessage("注册中.....");
            pd.setCancelable(true);
            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.i("zheng", "onCancel:");
                   IS_CANCEL =true;
                }
            });  //设置cancel的回调函数
            pd.setIndeterminate(false);  //表明是个detemininate精确显示的进度框
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.show();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            pd.setProgress(values[0]);//value from publishProgress

        }
        @Override
        protected String doInBackground(String... params) {
            int i=0;

            String path= params[0];
            File file = new File(path);
            File[] fs = file.listFiles();
            int filelenth=fs.length;
            float[] feature=new float[128];
            int res = -1;
            //        int ret;
          //  Log.i("zheng", "IS_CANCEL:"+IS_CANCEL);
            IS_CANCEL =false;
            for (File f : fs) {
                if(IS_CANCEL){
                    Message tempMsg = mHandler.obtainMessage();
                    tempMsg.what = 6;
                    mHandler.sendMessage(tempMsg);
                    IS_CANCEL=false;
                    return null;
                }
                feature = face.GetFeature(f.getAbsolutePath());
                i++;
                Log.d("zheng", "total:" + i);
                publishProgress(i*100/filelenth);
                if(feature!=null){
                    String nameStr = f.getName();
                    nameStr = nameStr.replaceAll(" ", "");
                    float[] high = new float[1];
                    String name = face.QueryDB(feature,high);
                    Log.i("zheng",name + " " + high[0]);
                    if(!name.equals("unknown") && high[0] > 0.6){
                        Message  tempMsg = mHandler.obtainMessage();
                        tempMsg.what = 2;
                        tempMsg.obj=nameStr.substring(0,nameStr.lastIndexOf("."));
                        mHandler.sendMessage(tempMsg);
                        continue;
                    }
                    //   ret= face.Recognize(feature,FileOperator.getflist(),FileOperator.getfIndex(),score,res);
                    res = face.AddDB(feature, nameStr.substring(0, nameStr.lastIndexOf(".")));
                    Log.d("zheng","AddDB res:"+res);
                    if (res == FaceAPP.ERROR_FAILURE) {
                        Message tempMsg = mHandler.obtainMessage();
                        tempMsg.what = 1;
                        mHandler.sendMessage(tempMsg);
                        continue;
                    }
                }


            }
            Message tempMsg = mHandler.obtainMessage();
            tempMsg.what = 6;
            mHandler.sendMessage(tempMsg);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("zheng", "执行结束了");
            GetSDTreeActivity mActivity = (GetSDTreeActivity) mActivty.get();
           // mixController.setState(mixController.STATE_IDLE,mixController.STATE_FACE_REGISTER);
            if(mActivity!=null&& !mActivity.isFinishing()) {
                pd.dismiss();
                pd.cancel();
                pd=null;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i("zheng", "执行了取消");

        }
    }
    void loadFiles(File directory) {
        nowDir = directory;
        setTitle(nowDir.getPath());
        dir_path.setText(nowDir.getAbsolutePath());
        //分类并排序
        File[] temp = directory.listFiles();
        ArrayList<File> tempFolder = new ArrayList<File>();
        ArrayList<File> tempFile = new ArrayList<File>();
        for (int i = 0; i < temp.length; i++) {
            File file = temp[i];
            if (file.isDirectory()) {
                tempFolder.add(file);
            } else if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith("jpg") ||
                        file.getName().toLowerCase().endsWith("png") ||
                        file.getName().toLowerCase().endsWith("jpeg") ||
                        file.getName().toLowerCase().endsWith("bmp")) {

                    tempFile.add(file);
                }
            }
        }
        //对List进行排序
        Comparator<File> comparator = new MyComparator();
        Collections.sort(tempFolder, comparator);
        Collections.sort(tempFile, comparator);

        imageFileList = tempFile;
        datas = new File[tempFolder.size() + tempFile.size()];
        System.arraycopy(tempFolder.toArray(), 0, datas, 0, tempFolder.size());
        System.arraycopy(tempFile.toArray(), 0, datas, tempFolder.size(), tempFile.size());
        //数据处理结束 =========================================
        adapater = new MyAdapater(GetSDTreeActivity.this);
        listView.setAdapter(adapater);
        if (isBack) {
            //listView.setSelection(lastPoint);
            listView.smoothScrollToPosition(lastPoint);
            lastPoint = 0;
            isBack = false;
        }


        //adapater.notifyDataSetChanged();
    }

    //自定义比较器
    class MyComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }

    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d("zheng", "clickback!");
//                String parent = nowDir.getParent();
//                if (parent.equals("/")) {
////                    Intent openFileBrowser = new Intent(this, MainActivity.class);
////                    startActivity(openFileBrowser);
////                    this.finish();
//                    return super.onKeyUp(keyCode, event);
//                }
//                isBack = true;
//                loadFiles(new File(parent));
//                Log.i("test", parent);
                this.finish();
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    // 自定义的BaseAdapter实现
    class ViewHolder {
        ImageView typeView;
        TextView nameView;
    }

    // TODO --Class-- MyAdapater 自定义适配器
    private class MyAdapater extends BaseAdapter {
        LayoutInflater mInflater;

        public MyAdapater(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        // TODO getView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item,
                        null);
                holder = new ViewHolder();
                holder.typeView = (ImageView) convertView
                        .findViewById(R.id.image_type);
                holder.nameView = (TextView) convertView
                        .findViewById(R.id.text_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 从arraylist集合里取出当前行数据；
            File file = datas[position];

            // 为页面控件设置数据
            if (file.isDirectory()) {
                holder.typeView.setImageResource(R.drawable.folder);
            } else {
                holder.typeView.setImageResource(R.drawable.file);
                String name = file.getName();
                int pointIndex = name.lastIndexOf(".");
                if (pointIndex != -1) {
                    String type = name.substring(pointIndex + 1).toLowerCase();
                    for (int i = 0; i < fileTypes.length; i++) {
                        if (type.equals(fileTypes[i])) {
                            try {
                                int resId = getResources().getIdentifier(type, "drawable", getPackageName());
                                holder.typeView.setImageResource(resId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }

            }
            holder.nameView.setText(file.getName());
            // 标示当前行是否被选中（通过显隐selectView实现）
            return convertView;
        }


        @Override
        public int getCount() {
            return datas.length;
        }

        @Override
        public Object getItem(int position) {
            return datas[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }

     class MyHandler extends Handler {
        GetSDTreeActivity activity;
        public  MyHandler(GetSDTreeActivity activity){
            this.activity=activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
                    showImportText.setText("本次导入图片的数量超过预设值");
                }
                break;
                case 2:{
                    showImportText.setText("you already exist" + msg.obj);
                }
                break;
                case 6: {
                   showImportText.setVisibility(View.GONE);
                }
                break;

            }
        }
    }
}