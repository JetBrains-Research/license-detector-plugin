package com.jetbrains.licensedetector.intellij.plugin.packagesearch.api.model

import com.google.gson.annotations.SerializedName

data class StandardV2LinkedFile(

        @SerializedName("name")
        val name: String?,

        @SerializedName("url")
        val url: String?,

        @SerializedName("html_url")
        val htmlUrl: String?,

        @SerializedName("spdx_id")
        val spdxId: String?,

        @SerializedName("key")
        val key: String?
)