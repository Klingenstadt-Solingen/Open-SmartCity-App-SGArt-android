package de.osca.android.sgart.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.osca.android.essentials.domain.entity.navigation.NavigationItem
import de.osca.android.sgart.R
import de.osca.android.sgart.entity.KunstInWald

sealed class ArtNavItems {
    object ArtFormNavItem : NavigationItem(
        title = R.string.art_title,
        route = "art_form",
        icon = R.drawable.ic_circle,
        deepLinks = listOf(navDeepLink { uriPattern = "solingen://art" }),
    )

    object ArtDetailsNavItem : NavigationItem(
        title = R.string.art_title,
        route = getDetailsRoute(),
        arguments =
            listOf(
                navArgument(ART_ID_ARGS) {
                    type = NavType.IntType
                },
            ),
        icon = R.drawable.ic_circle,
    )

    companion object {
        private const val ART_DETAILS_ROUTE = "art_details"
        const val ART_ID_ARGS = "art_id"

        fun getDetailsRoute(kunstInWald: KunstInWald? = null): String {
            return if (kunstInWald != null) {
                "$ART_DETAILS_ROUTE?$ART_ID_ARGS=${kunstInWald.ID_KIW}"
            } else {
                "$ART_DETAILS_ROUTE?$ART_ID_ARGS={$ART_ID_ARGS}"
            }
        }
    }
}
