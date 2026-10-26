@BindingContainer
object FooBindings {
    @Provides
    fun provideLong(): Long = 123L
}

// Only forwards FooBindings, declares nothing itself: must be reported.
@BindingContainer(includes = [FooBindings::class])
object <!FORWARDING_BINDING_CONTAINER!>ForwardingBindings<!>

// Has its own @Provides: allowed.
@BindingContainer(includes = [FooBindings::class])
object BindingsWithProvides {
    @Provides
    fun provideInt(): Int = 1
}

// Bindings live only in the companion object: allowed.
@BindingContainer(includes = [FooBindings::class])
interface BindingsWithCompanion {
    companion object {
        @Provides
        fun provideString(): String = "value"
    }
}

interface Repo

@Inject
class RepoImpl : Repo

// Has its own @Binds (Metro's canonical receiver form): allowed.
@BindingContainer(includes = [FooBindings::class])
interface BindingsWithBinds {
    @Binds
    fun RepoImpl.bindRepo(): Repo
}

// Binding annotation placed on the property getter: allowed.
@BindingContainer(includes = [FooBindings::class])
interface BindingsWithGetterProvides {
    @get:Provides
    val provideBoolean: Boolean
        get() = true
}

// Has its own @Multibinds: allowed.
@BindingContainer(includes = [FooBindings::class])
interface BindingsWithMultibinds {
    @Multibinds
    fun provideRepos(): Set<Repo>
}

// Any declared member counts, even without a Metro annotation: the checker must not depend on a
// fixed set of binding annotations. Allowed.
@BindingContainer(includes = [FooBindings::class])
object BindingsWithPlainMember {
    fun helper(): Int = 1
}

// No includes at all: not a forwarding container, allowed.
@BindingContainer
object EmptyBindings
