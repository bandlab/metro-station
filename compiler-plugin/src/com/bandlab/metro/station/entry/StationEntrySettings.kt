// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.entry

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

/**
 * Session component that carries whether the deprecated `@StationEntry` pipeline is enabled.
 *
 * This is registered by our own [com.bandlab.metro.station.MetroStationPluginRegistrar] (which has
 * access to the compiler configuration) so that the ServiceLoader-instantiated FIR extensions
 * ([StationEntryFir], [StationEntryContributionExtension]) can read the flag from the shared
 * [FirSession].
 *
 * When the component is absent (e.g. a session set up without our registrar), consumers should
 * default [allowStationEntries] to `true` for backwards compatibility. This is handled by the
 * [FirSession.allowStationEntries] accessor below.
 */
public class StationEntrySettings(
    session: FirSession,
    public val allowStationEntries: Boolean,
) : FirExtensionSessionComponent(session)

private val FirSession.stationEntrySettings: StationEntrySettings? by
    FirSession.nullableSessionComponentAccessor<StationEntrySettings>()

/**
 * Reads whether the `@StationEntry` pipeline is enabled for this [FirSession], defaulting to `true`
 * when the [StationEntrySettings] component is not registered.
 */
internal val FirSession.allowStationEntries: Boolean
    get() = stationEntrySettings?.allowStationEntries ?: true
