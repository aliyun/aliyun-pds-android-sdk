package com.aliyun.pds.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class ResultContract: ActivityResultContract<String, Uri>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent().setType("*/*")
            .setAction(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent!!.data!! else null
    }

}