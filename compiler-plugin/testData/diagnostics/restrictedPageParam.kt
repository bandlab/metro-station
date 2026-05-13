@StationEntry
class FooPage(context: Context) : ParamPage<Unit, <!RESTRICTED_PARAM_TYPE!>Long<!>>

@MetroStation(appDependencies = BarPage.ServiceProvider::class)
class BarPage(context: Context) : ParamPage<Unit, <!RESTRICTED_PARAM_TYPE!>Boolean<!>> {
    interface ServiceProvider
}
