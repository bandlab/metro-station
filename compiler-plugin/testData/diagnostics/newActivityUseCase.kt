// ENABLE_ACTIVITY_BASELINE: test.MyActivity

package test

@MetroStation(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<Unit>() {
    interface ServiceProvider
}

<!FORBIDDEN_ACTIVITY_USAGE!>@MetroStation(appDependencies = FooActivity.ServiceProvider::class)<!>
class FooActivity : CommonActivity<Unit>() {
    interface ServiceProvider
}

<!FORBIDDEN_ACTIVITY_USAGE!>@StationEntry<!>
class BarActivity : CommonActivity<Unit>()
