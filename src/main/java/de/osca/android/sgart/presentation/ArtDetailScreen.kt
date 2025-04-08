package de.osca.android.sgart.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import de.osca.android.essentials.presentation.component.design.BaseCardContainer
import de.osca.android.essentials.presentation.component.design.BaseImageElement
import de.osca.android.essentials.presentation.component.design.MainButton
import de.osca.android.essentials.presentation.component.design.MasterDesignArgs
import de.osca.android.essentials.presentation.component.design.MultiColumnList
import de.osca.android.essentials.presentation.component.design.OpenWebsiteElement
import de.osca.android.essentials.presentation.component.design.RootContainer
import de.osca.android.essentials.presentation.component.design.SimpleSpacedList
import de.osca.android.essentials.presentation.component.design.ZoomableImage
import de.osca.android.essentials.presentation.component.screen_wrapper.ScreenWrapper
import de.osca.android.essentials.presentation.component.topbar.ScreenTopBar
import de.osca.android.essentials.utils.extensions.SetSystemStatusBar
import de.osca.android.sgart.R

/**
 * @param kunstInWaldId
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun ArtDetailScreen(
    navController: NavController,
    artViewModel: ArtViewModel = hiltViewModel(),
    masterDesignArgs: MasterDesignArgs = artViewModel.defaultDesignArgs,
    kunstInWaldId: Int
) {
    val context = LocalContext.current
    val design = artViewModel.artDesignArgs

    LaunchedEffect(Unit) {
        artViewModel.initializeArtworks()
    }

    val artwork = artViewModel.artworks.firstOrNull { it.ID_KIW == kunstInWaldId }

    val showImageDialog = remember {
        mutableStateOf<String?>(null)
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
        retryAction = null,
        masterDesignArgs = masterDesignArgs,
        moduleDesignArgs = design
    ) {
        if(showImageDialog.value != null) {
            AlertDialog(
                onDismissRequest = {
                    showImageDialog.value = null
                },
                title = {
                },
                text = {
                    ZoomableImage(
                        imageUrl = showImageDialog.value ?: "",
                        moduleDesignArgs = design,
                        masterDesignArgs = masterDesignArgs
                    )
                },
                buttons = {
                    Row(modifier = Modifier
                        .padding(all = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MainButton(
                            onClick = {
                                showImageDialog.value = null
                            },
                            buttonText = stringResource(id = R.string.global_close_button),
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design
                        )
                    }
                },
                shape = RoundedCornerShape(design.mShapeCard ?: masterDesignArgs.mShapeCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            )
        }

        Column(modifier = Modifier
            .background(masterDesignArgs.getArtGradient())
            .fillMaxHeight()
        ) {
            RootContainer(
                masterDesignArgs = masterDesignArgs,
                moduleDesignArgs = design
            ) {
                item {
                    SimpleSpacedList(
                        masterDesignArgs = masterDesignArgs
                    ) {
                        BaseCardContainer(
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design,
                            overrideConstraintHeight = 200.dp,
                            useContentPadding = false
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(stringResource(id = R.string.art_base_url) + "${artwork?.KW_TITLEIMAGE}${if(artwork?.KW_TITLEIMAGE?.endsWith(".jpg") == false) ".jpg" else ""}"),
                                contentDescription = "headerImage",
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = artwork?.KUENSTLER ?: "",
                            style = masterDesignArgs.bodyTextStyle,
                            color = design.mDialogsTextColor ?: masterDesignArgs.mDialogsTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        BaseCardContainer(
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design,
                            text = stringResource(id = R.string.art_details_artist_content_description)
                        ) {
                            Text(
                                text = artwork?.KURZTEXT ?: "",
                                style = masterDesignArgs.normalTextStyle,
                                color = design.mCardTextColor ?: masterDesignArgs.mCardTextColor
                            )
                        }

                        BaseCardContainer(
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design,
                            text = stringResource(id = R.string.art_details_default_text)
                        ) {
                            Column {
                                Text(
                                    text = artwork?.TITEL_HOTSPOT ?: "",
                                    style = masterDesignArgs.normalTextStyle,
                                    color = design.mCardTextColor ?: masterDesignArgs.mCardTextColor
                                )

                                Text(
                                    text = artwork?.ORTSBESCHREIBUNG ?: "",
                                    style = masterDesignArgs.normalTextStyle,
                                    color = design.mCardTextColor ?: masterDesignArgs.mCardTextColor
                                )
                            }
                        }

                        BaseCardContainer(
                            masterDesignArgs = masterDesignArgs,
                            moduleDesignArgs = design,
                            text = stringResource(id = R.string.art_websites)
                        ) {
                            if (artwork != null) {
                                Column {
                                    for (websiteUrl in artwork.KUENSTLER_URL) {
                                        OpenWebsiteElement(
                                            url = websiteUrl,
                                            withTitle = false,
                                            masterDesignArgs = masterDesignArgs,
                                            moduleDesignArgs = design,
                                            context = context
                                        )
                                    }
                                }
                            }
                        }

                        if (artwork != null) {
                            MultiColumnList(
                                columnCount = 3
                            ) {
                                artwork.KUENSTLER_IMAGES.map { element ->
                                    { modifier ->
                                        val url = stringResource(id = R.string.art_base_url) + "${element}${if (element.endsWith(".jpg")) "" else ".jpg"}"
                                        BaseImageElement(
                                            masterDesignArgs = masterDesignArgs,
                                            moduleDesignArgs = design,
                                            imageUrl = url,
                                            modifier = modifier,
                                            onClick = {
                                                // show bigger preview
                                                showImageDialog.value = url
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (artwork?.KW_IMAGEGALLERY != null) {
                            MultiColumnList(
                                columnCount = 3
                            ) {
                                artwork.KW_IMAGEGALLERY.map { element ->
                                    { modifier ->
                                        val url = stringResource(id = R.string.art_base_url) + "${element}${if (element.endsWith(".jpg")) "" else ".jpg"}"
                                        BaseImageElement(
                                            masterDesignArgs = masterDesignArgs,
                                            moduleDesignArgs = design,
                                            imageUrl = url,
                                            modifier = modifier,
                                            onClick = {
                                                // show bigger preview
                                                showImageDialog.value = url
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}