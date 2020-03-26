package cn.com.cg.ocr.common.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 15:15
 */
public class FileUtils {


    public static String catchPath = Environment.getExternalStorageDirectory() + "/CoolImage/";

    public static String saveToSDCard(Bitmap bitmap) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) // 判断是否可以对SDcard进行操作
        {    // 获取SDCard指定目录下
            String sdCardDir = catchPath;
            File dirFile = new File(sdCardDir);  //目录转化成文件夹
            if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }                          //文件夹有啦，就可以保存图片啦
            File file = new File(sdCardDir, System.currentTimeMillis() + ".jpg");// 在SDcard的目录下创建图片文,以当前时间为其命名
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                System.out.println("_________保存到____sd______指定目录文件夹下____________________");
                return file.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static void deleteCatchImageFile(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 获取SDCard指定目录下
            String sdCardDir = catchPath;
            File dirFile = new File(sdCardDir);  //目录转化成文件夹
            if (dirFile.exists()) {              //如果存在，则删除
                recursionDeleteFile(dirFile);
            }
        }
    }

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    private static void recursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                recursionDeleteFile(f);
            }
            file.delete();
        }
    }

}
