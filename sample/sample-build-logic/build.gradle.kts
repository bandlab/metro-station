plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.build.agp)
    compileOnly(libs.build.kotlin)
    compileOnly(libs.build.metro)
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("sample-app") {
            implementationClass = "SampleAppPlugin"
        }
        register("sample-lib") {
            implementationClass = "SampleLibPlugin"
        }
    }
}
