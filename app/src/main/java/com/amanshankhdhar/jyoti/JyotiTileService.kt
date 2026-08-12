// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class JyotiTileService : TileService() {
    override fun onClick() {
        if (TorchManager.mode != "NORMAL") {
            TorchManager.forceOff(this) // Stop strobe/SOS if active
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
            it.state = if (TorchManager.isOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.subtitle = when {
                TorchManager.mode == "STROBE" -> "STROBE"
                TorchManager.mode == "SOS" -> "SOS"
                TorchManager.isOn -> "ON"
                else -> null
            }
            it.updateTile()
        }
    }
}