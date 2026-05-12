package com.bandlab.metro.extensions.sample.profile.content

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bandlab.common.android.di.ContributesComponent
import com.bandlab.metro.extensions.sample.profile.ProfileService
import com.bandlab.metro.extensions.sample.ui.theme.Purple80
import com.bandlab.metro.extensions.sample.utils.ScreenTracker
import com.bandlab.uikit.api.page.Page
import dev.zacsweers.metro.Inject

@ContributesComponent(
    appDependencies = ProfileContentPage.ServiceProvider::class,
    extraDependencies = ProfileContentPage.ActivityDependencies::class,
)
@Inject
class ProfileContentPage(
    context: Context,
    activityDependencies: ActivityDependencies,
) : Page<ProfileContentViewModel> {

    @Composable
    override fun Content(viewModel: ProfileContentViewModel) {
        val username by viewModel.username.collectAsState()
        LaunchedEffect(viewModel) { viewModel.loadUser() }

        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Purple80),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Profile Content for $username"
            )
        }
    }

    interface ServiceProvider {
        val screenTracker: ScreenTracker
    }

    @Inject
    class ActivityDependencies(
        val profileService: ProfileService
    )
}