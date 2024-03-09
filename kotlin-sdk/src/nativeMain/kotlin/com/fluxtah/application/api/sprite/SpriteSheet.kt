package com.fluxtah.application.api.sprite

import com.fluxtah.application.api.interop.CSpriteSheet
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class SpriteSheet(val id: String, val handle: CSpriteSheet)