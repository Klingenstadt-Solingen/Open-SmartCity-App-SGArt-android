package de.osca.android.sgart.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.osca.android.essentials.presentation.component.design.BaseCardContainer
import de.osca.android.essentials.presentation.component.design.MainButton
import de.osca.android.essentials.presentation.component.design.MasterDesignArgs
import de.osca.android.essentials.presentation.component.design.OpenWebsiteElement
import de.osca.android.essentials.presentation.component.design.RootContainer
import de.osca.android.essentials.presentation.component.screen_wrapper.ScreenWrapper
import de.osca.android.essentials.presentation.component.screen_wrapper.ScreenWrapperState
import de.osca.android.essentials.presentation.component.topbar.ScreenTopBar
import de.osca.android.essentials.utils.extensions.SetSystemStatusBar
import de.osca.android.sgart.R
import de.osca.android.sgart.navigation.ArtNavItems
import de.osca.android.sgart.presentation.components.ArtInfoElement

/**
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArtScreen(
    navController: NavController,
    artViewModel: ArtViewModel = hiltViewModel(),
    masterDesignArgs: MasterDesignArgs = artViewModel.defaultDesignArgs
) {
    val context = LocalContext.current
    val design = artViewModel.artDesignArgs

    if (artViewModel.artworks.isEmpty()) {
        LaunchedEffect(Unit) {
            artViewModel.initializeArtworks()
        }
    }

    val granted = remember { mutableStateOf(false) }
    val permissions: List<String>

    if (Build.VERSION.SDK_INT > 30) {
        granted.value = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        granted.value = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    val multiplePermissionState = rememberMultiplePermissionsState(
        permissions = permissions
    ) {
        if (it.values.contains(false).not()) {
            granted.value = true
        }
    }



    val userInRange = remember {
        mutableStateOf(false)
    }

    if(multiplePermissionState.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            artViewModel.isUserInRange(context, 250,
                inRange = {
                    userInRange.value = true

                    artViewModel.initializeBeaconManager(context)
                },
                notInRange = {
                    userInRange.value = false

                    artViewModel.stopSearchingBeacons()
                }
            )
        }

        SetSystemStatusBar(
            !(design.mIsStatusBarWhite ?: masterDesignArgs.mIsStatusBarWhite), Color.Transparent
        )

        ScreenWrapper(
            topBar = {
                ScreenTopBar(
                    title = stringResource(id = design.vModuleTitle),
                    masterDesignArgs = masterDesignArgs,
                    overrideTextColor = design.mTopBarTextColor,
                    overrideBackgroundColor = design.mTopBarBackColor,
                    navController = navController
                )
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize(),
            screenWrapperState = artViewModel.wrapperState,
            masterDesignArgs = masterDesignArgs,
            retryAction = {
                artViewModel.initializeBeaconManager(context)
            },
            moduleDesignArgs = design
        ) {
            Column(
                modifier = Modifier
                    .background(masterDesignArgs.getArtGradient())
                    .fillMaxHeight()
            ) {
                RootContainer(
                    masterDesignArgs = masterDesignArgs,
                    moduleDesignArgs = design
                ) {
                    item {
                        BaseCardContainer(
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.art_welcome),
                                    style = masterDesignArgs.normalTextStyle,
                                    color = design.mCardTextColor ?: masterDesignArgs.mCardTextColor
                                )

                                OpenWebsiteElement(
                                    url = stringResource(id = R.string.art_link),
                                    masterDesignArgs = masterDesignArgs,
                                    moduleDesignArgs = design,
                                    withTitle = false,
                                    context = context
                                )
                            }
                        }
                    }

                    if(userInRange.value) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    color = design.mDialogsTextColor
                                        ?: masterDesignArgs.mDialogsTextColor
                                )
                            }
                        }
                    } else {
                        item {
                            BaseCardContainer(
                                masterDesignArgs = masterDesignArgs,
                                moduleDesignArgs = design
                            ) {
                                Text(
                                    text = stringResource(id = R.string.art_not_at_wald),
                                    style = masterDesignArgs.bodyTextStyle,
                                    color = design.mCardTextColor
                                        ?: masterDesignArgs.mCardTextColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (artViewModel.finalArtworks.isNotEmpty()) { // finalArtworks
                        for (artwork in artViewModel.finalArtworks) { // finalArtworks
                            item {
                                ArtInfoElement(
                                    masterDesignArgs = masterDesignArgs,
                                    moduleDesignArgs = design,
                                    kunstInWald = artwork,
                                    onClick = {
                                        navController.navigate(ArtNavItems.getDetailsRoute(artwork))
                                    },
                                    distance = artwork.distance
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        SetSystemStatusBar(
            !(design.mIsStatusBarWhite ?: masterDesignArgs.mIsStatusBarWhite), Color.Transparent
        )

        ScreenWrapper(
            topBar = {
                ScreenTopBar(
                    title = stringResource(id = R.string.art_title),
                    masterDesignArgs = masterDesignArgs,
                    overrideTextColor = design.mTopBarTextColor,
                    overrideBackgroundColor = design.mTopBarBackColor,
                    navController = navController
                )
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize(),
            screenWrapperState = remember { mutableStateOf(ScreenWrapperState.DisplayContent) },
            masterDesignArgs = masterDesignArgs,
            retryAction = {
                artViewModel.initializeBeaconManager(context)
            },
            moduleDesignArgs = design
        ) {
            RootContainer(
                masterDesignArgs = masterDesignArgs,
                moduleDesignArgs = design
            ) {
                item {
                    BaseCardContainer(
                        masterDesignArgs = masterDesignArgs,
                        moduleDesignArgs = design
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.art_enable_location),
                                style = masterDesignArgs.normalTextStyle,
                                color = masterDesignArgs.mCardTextColor
                            )

                            if (multiplePermissionState.shouldShowRationale) {
                                Text(
                                    text = stringResource(id = R.string.art_denied_permission),
                                    style = masterDesignArgs.normalTextStyle,
                                    color = masterDesignArgs.mCardTextColor
                                )
                            } else {
                                MainButton(
                                    buttonText = "Rechte Freigeben",
                                    onClick = {
                                        // Check permission
                                        if (multiplePermissionState.allPermissionsGranted) {
                                            // Camera permissions are given, may proceed
                                            granted.value = true
                                        } else {
                                            // We can ask user for permission
                                            multiplePermissionState.launchMultiplePermissionRequest()
                                        }
                                    },
                                    masterDesignArgs = masterDesignArgs,
                                    moduleDesignArgs = design
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
