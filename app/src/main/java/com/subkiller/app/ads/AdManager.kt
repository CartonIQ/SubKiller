package com.subkiller.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages Banner and Interstitial AdMob integrations with TEST IDs ONLY.
 * Banner test unit: ca-app-pub-9771662959477951/1652345559
 * Interstitial test unit: ca-app-pub-9771662959477951/1892018703
 * Shows Interstitial ad every 3rd time the user opens the Dashboard.
 */
object AdManager {
    private const val TAG = "AdManager"
    const val BANNER_TEST_UNIT_ID = "ca-app-pub-9771662959477951/1652345559"
    const val INTERSTITIAL_TEST_UNIT_ID = "ca-app-pub-9771662959477951/1892018703"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var dashboardOpenCount = 0

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            loadInterstitial(context)
        }
    }

    fun loadBanner(context: Context, container: ViewGroup): AdView {
        val adView = AdView(context).apply {
            adUnitId = BANNER_TEST_UNIT_ID
            setAdSize(AdSize.BANNER)
        }
        container.removeAllViews()
        container.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        return adView
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "AdMob Interstitial loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "AdMob Interstitial failed: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Increments the open counter and shows interstitial on every 3rd dashboard open.
     */
    fun onDashboardOpened(activity: Activity) {
        dashboardOpenCount++
        Log.d(TAG, "Dashboard opened count: \$dashboardOpenCount")

        if (dashboardOpenCount % 3 == 0) {
            interstitialAd?.let { ad ->
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitial(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        interstitialAd = null
                        loadInterstitial(activity)
                    }
                }
                ad.show(activity)
            } ?: run {
                loadInterstitial(activity)
            }
        }
    }
}
