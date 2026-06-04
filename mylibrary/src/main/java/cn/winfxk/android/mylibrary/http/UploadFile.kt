package cn.winfxk.android.mylibrary.http

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

/**
 * 上传文件实体类
 */
data class UploadFile(
    val paramName: String,
    val file: File,
    val fileName: String = file.name,
    val mediaType: MediaType? = "application/octet-stream".toMediaTypeOrNull()
)