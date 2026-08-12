// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

enum class TorchResult { ON, OFF, NO_HARDWARE, BLOCKED, FAILED }

object TorchManager {
    var isOn = false; private set

    private fun cam(ctx: Context) = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private fun torchId(ctx: Context): String? =
        cam(ctx).cameraIdList.firstOrNull {
            cam(ctx).getCameraCharacteristics(it)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

    fun toggle(ctx: Context): TorchResult {
        val id = torchId(ctx) ?: return TorchResult.NO_HARDWARE
        return try {
            isOn = !isOn
            cam(ctx).setTorchMode(id, isOn)
            if (isOn) TorchResult.ON else TorchResult.OFF
        } catch (e: SecurityException) { isOn = false; TorchResult.BLOCKED }
        catch (e: Exception) { isOn = false; TorchResult.FAILED }
    }
}
