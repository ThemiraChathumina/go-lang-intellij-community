package com.github.themirachathumina.golangintellijcommunity

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LayeredIcon
import javax.swing.Icon

object GoIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/golang-icon.svg", GoIcons::class.java)

    private val GOMOD_BADGE: Icon = IconLoader.getIcon("/icons/gomod-badge.svg", GoIcons::class.java)
    private val GOWORK_BADGE: Icon = IconLoader.getIcon("/icons/gowork-badge.svg", GoIcons::class.java)
    private val GOSUM_BADGE: Icon = IconLoader.getIcon("/icons/gosum-badge.svg", GoIcons::class.java)

    @JvmField val MOD_FILE: Icon = layered(FILE, GOMOD_BADGE)
    @JvmField val WORK_FILE: Icon = layered(FILE, GOWORK_BADGE)
    @JvmField val SUM_FILE: Icon = layered(FILE, GOSUM_BADGE)

    private fun layered(base: Icon, badge: Icon): Icon {
        val icon = LayeredIcon(2)
        icon.setIcon(base, 0)
        // position badge at the bottom-right of the 16x16 base icon
        icon.setIcon(badge, 1, 16 - badge.iconWidth, 16 - badge.iconHeight)
        return icon
    }
}
