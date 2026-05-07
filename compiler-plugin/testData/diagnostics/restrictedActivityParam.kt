@ContributesInjector
class FooActivity : CommonActivity<<!RESTRICTED_PARAM_TYPE!>String<!>>()

@ContributesComponent(appDependencies = BarActivity.ServiceProvider::class)
class BarActivity : CommonActivity<<!RESTRICTED_PARAM_TYPE!>Int<!>>() {
    interface ServiceProvider
}
