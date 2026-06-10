@MetroStation(
    appDependencies = FooPage.ServiceProvider::class,
    extraDependencies = FooPage.ExtraDependencies::class
)
class <!MISSING_EXTRA_DEPENDENCIES_PARAMETER!>FooPage<!> : Page<Unit> {
    interface ServiceProvider

    @Inject
    class ExtraDependencies(
        val int: Int
    )
}
