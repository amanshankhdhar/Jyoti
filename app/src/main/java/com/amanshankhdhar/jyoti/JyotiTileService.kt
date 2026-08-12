// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class JyotiTileService : TileService() {
    override fun onClick() {
        if (TorchManager.mode != "NORMAL") {
            TorchManager.forceOff(this)
        } else {
            when (TorchManager.toggle(this)) {
                TorchResult.BLOCKED -> Toast.makeText(this, "Open Jyoti to grant permission", Toast.LENGTH_LONG).show()
                TorchResult.NO_HARDWARE -> Toast.makeText(this, "No torch hardware", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
        refresh()
    }

    override fun onStartListening() = refresh()

    private fun refresh() {
        qsTile?.let {
            val on = TorchManager.isOn
            it.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.icon = Icon.createWithResource(this,
                if (on) R.drawable.ic_tile_on else R.drawable.ic_tile_off)
            it.subtitle = when {
                TorchManager.mode == "STROBE" -> "STROBE"
                TorchManager.mode == "SOS" -> "SOS"
                on -> "ON"
                else -> null
            }
            it.updateTile()
        }
    }
}