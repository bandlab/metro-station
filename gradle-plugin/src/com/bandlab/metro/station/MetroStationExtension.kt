// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty

public abstract class MetroStationExtension @Inject constructor(objects: ObjectFactory) {

    /** A baseline of features that are allowed to use @StationEntry. */
    public val stationEntriesBaseline: SetProperty<String> = objects.setProperty(String::class.java)
}
