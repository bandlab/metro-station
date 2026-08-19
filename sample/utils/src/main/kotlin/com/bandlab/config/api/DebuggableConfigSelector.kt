// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.config.api

/**
 * A common base type for feature flags and user/ device preferences that are exposed in the debug
 * menu.
 */
interface DebuggableConfigSelector

interface BooleanConfigSelector : DebuggableConfigSelector {
    val key: String
    val defaultValue: Boolean
}
