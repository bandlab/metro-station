package com.bandlab.metro.station.sample.main

import android.content.Context
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
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.ui.theme.SampleAppTheme
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = MainActivity.ServiceProvider::class)
class MainActivity : CommonActivity<Unit>() {

    @Inject
    override lateinit var dependencies: CommonActivityDependencies

    @Inject
    private lateinit var viewModel: MainViewModel

    override fun parseRequiredParams(bundle: Bundle) = Unit

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
                                text = "Hello from Main screen!",
                            )

                            Button(onClick = viewModel::openProfile) {
                                Text(text = "Open Profile")
                            }
                        }
                    }
                }
            }
        }

        viewModel.showToast()
    }

    interface ServiceProvider {
        val context: Context
    }
}