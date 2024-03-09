package com.fluxtah.application.api

import com.fluxtah.application.api.interop.CApplicationContext
import kotlinx.cinterop.ExperimentalForeignApi

object ApplicationContext {
    @OptIn(ExperimentalForeignApi::class)
    internal var vulcanContext: CApplicationContext? = null
}