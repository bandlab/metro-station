// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.profile.ProfileService
import com.bandlab.metro.station.sample.ui.theme.Purple80
import com.bandlab.metro.station.sample.utils.ScreenTracker
import com.bandlab.uikit.api.page.Page
import dev.zacsweers.metro.Inject

@MetroStation(
    appDependencies = ProfileContentPage.ServiceProvider::class,
    extraDependencies = ProfileContentPage.ActivityDependencies::class,
    additionalScopes = [AdditionalFeatureScope::class],
    excludes = [FakeProfileContentProvider::class],
    bindingContainers = [ProfileContentProvider::class],
)
@Inject
class ProfileContentPage(
    private val activityDependencies: ActivityDependencies /* used by the compiler */
) : Page<ProfileContentViewModel> {

    @Composable
    override fun Content(viewModel: ProfileContentViewModel) {
        val username by viewModel.username.collectAsState()
        val description by viewModel.description.collectAsState()
        LaunchedEffect(viewModel) { viewModel.loadUser() }

        Box(
            modifier = Modifier.size(300.dp).background(Purple80),
            contentAlignment = Alignment.Center,
        ) {
            if (username == null) {
                CircularProgressIndicator()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Profile Content for $username")
                    Text(description)
                }
            }
        }
    }

    interface ServiceProvider {
        val screenTracker: ScreenTracker
    }

    @Inject class ActivityDependencies(val profileService: ProfileService)
}
