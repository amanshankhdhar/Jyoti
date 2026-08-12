// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class JyotiTileService : TileService() {
    override fun onClick() {
        when (TorchManager.toggle(this)) {
            TorchResult.ON  -> Toast.makeText(this, "🪔 Jyoti ON", Toast.LENGTH_SHORT).show()
            TorchResult.OFF -> Toast.makeText(this, "Jyoti OFF", Toast.LENGTH_SHORT).show()
            TorchResult.BLOCKED -> Toast.makeText(this, "System blocked torch — open Jyoti once", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "No torch hardware — use Screen Light in app", Toast.LENGTH_LONG).show()
        }
        refresh()
    }

    override fun onStartListening() = refresh()

    private fun refresh() {
        qsTile?.let {
            it.state = if (TorchManager.isOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.updateTile()
        }
    }
}
