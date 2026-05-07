package com.bandlab.metro.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty

public open class MetroExtensionsGradleExtension(objectFactory: ObjectFactory) {
    public val contributesInjectorBaseline: SetProperty<String> =
        objectFactory.setProperty(String::class.java).convention(emptySet())
}
