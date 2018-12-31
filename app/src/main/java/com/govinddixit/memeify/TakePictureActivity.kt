package com.govinddixit.memeify

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider.getUriForFile
import android.view.View
import kotlinx.android.synthetic.main.activity_take_picture.*
import java.io.File
const private val TAKE_PHOTO_REQUEST_CODE = 1
private var pictureTaken: Boolean = false


class TakePictureActivity : Activity(), View.OnClickListener {

    private var selectedPhotoPath: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_picture)

        pictureImageview.setOnClickListener(this)
        enterTextButton.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.pictureImageview -> takePictureWithCamera()
            R.id.enterTextButton -> moveToNextScreen()
            else -> println("No case satisfied")
        }

    }
    private fun takePictureWithCamera() {
        // 1
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 2
        val imagePath = File(filesDir, "images")
        val newFile = File(imagePath, "default_image.jpg")
        if (newFile.exists()) {
            newFile.delete()
        } else {
            newFile.parentFile.mkdirs()
        }
        selectedPhotoPath = getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", newFile)

        // 3
        captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedPhotoPath)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            val clip = ClipData.newUri(contentResolver, "A photo", selectedPhotoPath)
            captureIntent.clipData = clip
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(captureIntent, TAKE_PHOTO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setImageViewWithImage()
        }
    }

    private fun setImageViewWithImage() {
        val photoPath: Uri = selectedPhotoPath ?: return
        pictureImageview.post {
            val pictureBitmap = BitmapResizer.shrinkBitmap(
                this@TakePictureActivity,
                photoPath,
                pictureImageview.width,
                pictureImageview.height
            )
            pictureImageview.setImageBitmap(pictureBitmap)
        }
        lookingGoodTextView.visibility = View.VISIBLE
        pictureTaken = true
    }




    companion object {
        const private val MIME_TYPE_IMAGE = "image/"
    }

    private fun moveToNextScreen() {
        if (pictureTaken) {
            val nextScreenIntent = Intent(this, EnterTextActivity::class.java).apply {
                putExtra(IMAGE_URI_KEY, selectedPhotoPath)
                putExtra(BITMAP_WIDTH, pictureImageview.width)
                putExtra(BITMAP_HEIGHT, pictureImageview.height)
            }

            startActivity(nextScreenIntent)
        } else {
            Toaster.show(this, R.string.select_a_picture)
        }
    }


}
