package com.bandlab.metro.station.sample.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.android.common.activity.CommonActivityDependencies
import com.bandlab.metro.station.StationEntry
import com.bandlab.metro.station.sample.profile.content.ProfileContentPage
import com.bandlab.metro.station.sample.profile.footer.ProfileFooterPage
import com.bandlab.metro.station.sample.ui.theme.SampleAppTheme
import com.bandlab.uikit.compose.page.container.PageContainer
import com.bandlab.uikit.compose.page.container.toPageContainerState
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json

@StationEntry
class ProfileActivity : CommonActivity<Profile>() {

    @Inject override lateinit var dependencies: CommonActivityDependencies
    @Inject private lateinit var viewModel: ProfileViewModel
    @Inject private lateinit var profileContentPage: ProfileContentPage
    @Inject private lateinit var profileFooterPage: ProfileFooterPage

    override fun parseRequiredParams(bundle: Bundle): Profile {
        return Json.decodeFromString(requireNotNull(bundle.getString(ARG_PROFILE)))
    }

    override fun onCreate() {
        enableEdgeToEdge()
        setContent {
            SampleAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = "Hello from Profile screen!",
                            )

                            PageContainer(profileContentPage.toPageContainerState())

                            PageContainer(
                                profileFooterPage.toPageContainerState(
                                    ProfileFooterPage.Param(username = "BandLab")
                                )
                            )

                            Button(onClick = { finish() }) {
                                Text(text = "Go back")
                            }
                        }
                    }
                }
            }
        }

        viewModel.showToast()
    }

    companion object {
        const val ARG_PROFILE = "profile"
    }
}