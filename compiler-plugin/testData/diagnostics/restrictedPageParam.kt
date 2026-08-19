@StationEntry
class FooPage : ParamPage<Unit, <!RESTRICTED_PARAM_TYPE!>Long<!>>

@MetroStation(appDependencies = BarPage.ServiceProvider::class)
class BarPage : ParamPage<Unit, <!RESTRICTED_PARAM_TYPE!>Boolean<!>> {
    interface ServiceProvider
}
