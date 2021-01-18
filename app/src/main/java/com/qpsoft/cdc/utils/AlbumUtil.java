package com.qpsoft.cdc.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AlbumUtil {

    public static void saveBitmap2file(Bitmap bmp, String name, Context context) {
        //String savePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separator;
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"cdc/";
        String fileName = name + ".jpg";
        File filePic = new File(savePath + fileName);
        try {
            boolean isSave = FileUtils.createFileByDeleteOldFile(filePic);
            if (!isSave) {
                Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "已保存至相册", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    filePic.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath+fileName)));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filePic)));

    }
}
