package com.bandlab.metro.station.sample.profile.footer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bandlab.common.android.pager.screen.ParamPage
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.ui.theme.Purple80
import com.bandlab.metro.station.sample.utils.ScreenTracker
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = ProfileFooterPage.ServiceProvider::class)
@Inject
class ProfileFooterPage : ParamPage<ProfileFooterViewModel, ProfileFooterPage.Param> {

    @Composable
    override fun Content(viewModel: ProfileFooterViewModel) {
        Box(
            modifier = Modifier
                .size(300.dp, 100.dp)
                .background(Purple80),
            contentAlignment = Alignment.Center
        ) {
            Text("Profile Footer for ${viewModel.username}")
        }
    }

    data class Param(
        val username: String
    )

    interface ServiceProvider {
        val screenTracker: ScreenTracker
    }
}