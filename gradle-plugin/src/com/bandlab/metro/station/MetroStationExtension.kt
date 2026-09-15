// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class MetroStationExtension @Inject constructor(objects: ObjectFactory) {

    /** Allow using @StationEntry. Default to true. */
    public val allowStationEntries: Property<Boolean> =
        objects.property(Boolean::class.javaObjectType)

    /**
     * A baseline of features that are allowed to use @StationEntry. This will only be respected
     * when [allowStationEntries] is true.
     */
    public val stationEntriesBaseline: SetProperty<String> = objects.setProperty(String::class.java)
}
