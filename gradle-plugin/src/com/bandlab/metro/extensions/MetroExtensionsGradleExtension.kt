package com.bandlab.metro.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

public abstract class MetroExtensionsGradleExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * A baseline of features that are allowed to use @ContributesInjector.
     */
    public val contributesInjectorBaseline: SetProperty<String> = objects.setProperty(String::class.java)
}
