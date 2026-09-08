// KOTLIN_REFLECT_DUMP_MISMATCH
// Metro generates the synthetic `BindsMirror` private constructor twice at the FIR level
// (both ContributionsFirGenerator and BindingMirrorClassFirGenerator claim it; see Metro's own
// "TODO dedupe with BindingMirrorClassFirGenerator" comments). The duplicate is collapsed in IR,
// but the new kotlin-reflect dump surfaces it and diverges from the K1 dump, so we skip that check.
@ContributesConfigSelector
object FooConfigSelector : DebuggableConfigSelector
