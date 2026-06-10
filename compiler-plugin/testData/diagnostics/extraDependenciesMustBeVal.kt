@MetroStation(
    appDependencies = FooPage.ServiceProvider::class,
    extraDependencies = FooPage.ExtraDependencies::class
)
class <!EXTRA_DEPENDENCIES_PARAMETER_MUST_BE_VAL!>FooPage<!>(extraDependencies: ExtraDependencies) : Page<Unit> {
    interface ServiceProvider

    @Inject
    class ExtraDependencies(
        val int: Int
    )
}
