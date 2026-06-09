@MetroStation(
    appDependencies = FooActivity.ServiceProvider::class,
    extraDependencies = FooActivity.ExtraDependencies::class
)
class <!EXTRA_DEPENDENCIES_UNSUPPORTED!>FooActivity<!> : CommonActivity<Unit>() {

    interface ServiceProvider

    class ExtraDependencies
}
