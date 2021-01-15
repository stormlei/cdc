package com.qpsoft.cdc.ui

import android.R.attr
import android.content.Intent
import android.database.Cursor
import android.provider.MediaStore
import com.blankj.utilcode.util.LogUtils
import com.king.zxing.CaptureActivity
import com.king.zxing.util.CodeUtils
import com.qpsoft.cdc.R
import kotlinx.android.synthetic.main.activity_custom_capture.*


class CustomCaptureActivity : CaptureActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_capture
    }

    override fun initUI() {
        super.initUI()
        ivPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }
    }

    override fun initCameraScan() {
        super.initCameraScan()
        getCameraScan()
            .setPlayBeep(true)
            .setVibrate(true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val selectedImage = data?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            if(selectedImage != null) {
                val cursor: Cursor = this@CustomCaptureActivity.getContentResolver()
                    .query(selectedImage, filePathColumn, null, null, null)!!
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val imgPath = cursor.getString(columnIndex)
                cursor.close()
                val i = Intent()
                i.putExtra("result", CodeUtils.parseQRCode(imgPath))
                setResult(RESULT_OK, i)
                finish()
            }

        }
    }
}