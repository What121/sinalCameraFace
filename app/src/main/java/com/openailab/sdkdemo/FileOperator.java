package com.openailab.sdkdemo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FileOperator {
    public final static int MAX_REGISTER=5000; // set max number of faces ,could be modify
    private final static int FEATURE_LEN=128;
    private final static String IMAGE_PATH= "/sdcard/openailab/image";
    private  static File storeFile=null;
    private static  int fIndex=0;

    public static float[][] flist=new float[MAX_REGISTER][FEATURE_LEN];
    public static String[] namelist=new String[MAX_REGISTER+1];
    public static int getfIndex(){
        return fIndex;
    }
    public static void incfIndex(){
        fIndex=fIndex+1;
        Log.d("morrisdebug","findex"+fIndex);
    }
    public static float[][] getflist(){
        return flist;
    }
    public static String[] getnamelist(){
        return namelist;
    }
    public static boolean setStorePath(String path){
        if(storeFile==null){
            storeFile=new File(path);
        }

        if(storeFile.exists()){
            return true;//already exist
        }else{
            return false;
        }
    }

    public static boolean saveOneItem(int index){
        FileWriter out = null;  //文件写入流
        try {
            out = new FileWriter(storeFile,true);//append
            out.write(namelist[index]+" ");
            for (int k = 0; k < 128; k++) {
                out.write(flist[index][k] + " ");
            }
            out.write("\r\n");//forget to add \r\n for another line
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean saveStoreFile(int filelenth){
        FileWriter out = null;  //文件写入流
        try {
            out = new FileWriter(storeFile,true);//append
            for(int j=fIndex-filelenth;j<fIndex;j++) { //from first register feature to last one
                out.write(namelist[j]+" ");//write file name as name
                for (int k = 0; k < 128; k++) {
                    out.write(flist[j][k] + " ");
                }
                out.write("\r\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean loadStoreFile(){
        BufferedReader in = null;  //
        try {
            in = new BufferedReader(new FileReader(storeFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;  //一行数据

//逐行读取，并将每个数组放入到数组中
        try {
            while((line = in.readLine()) != null){
                String[] temp = line.split(" ");
                namelist[fIndex]=temp[0];
                for(int k=1;k<temp.length;k++){
                    flist[fIndex][k-1] = Float.parseFloat(temp[k]);
                    //Log.d("morrisdebug","value"+"["+k+"]"+flist[fIndex][k-1]);
                }
                fIndex++;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean createFolder(String ffd) {
        File dir = new File(ffd);
        return createFolder(dir);
    }

    public static boolean createFolder(File dir) {
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static Boolean isFileExist(String ffn) {
        if (ffn == null)
            return false;

        File f = new File(ffn);
        return f.exists();
    }


    public static Map<String, String> getSettingMapFromFile(File f) {
        Map<String, String> map = new HashMap<String, String>();

        BufferedReader br = null;
        InputStreamReader isr = null;
        String[] strs = null;
        String flip = null;

        String str = null;
        try {
            isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                strs = splitText(str);
                flip = strs[0];

                map.put("flip", flip);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                isr.close();
                br.close();
            } catch (Exception ex2) {
            }
        }

        return map;
    }

    public static String[] splitText(String str) {
        return str.split(";");
    }

    public static void saveSettingIntoTxt(Map<String, String> settingMap,File file) {

        StringBuffer flexoFileTxtStr = new StringBuffer();
        flexoFileTxtStr.append(settingMap.get("flip") + ";");
        writeStringBuffer(flexoFileTxtStr, file);

    }


    public static void writeStringBuffer(StringBuffer strb, File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            out.write(strb.toString().getBytes("utf-8"));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }
    }


}
