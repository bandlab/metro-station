package com.bandlab.metro.station

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

public abstract class MetroStationExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * A baseline of features that are allowed to use @StationEntry.
     */
    public val stationEntriesBaseline: SetProperty<String> = objects.setProperty(String::class.java)
}
