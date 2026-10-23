/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package dev.voidtech.glacia.core

import dev.voidtech.glacia.core.configs.GlaciaChatsConfig

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    fun switchNoAuthor(b: Boolean) {
        GlaciaChatsConfig.noAuthorship = b
    }

    fun switchNoCaptions(b: Boolean) {
        GlaciaChatsConfig.noCaptions = b
    }

}