package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.assetco.hotspots.optimization.Any.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinalBugTest {
    private SearchResultHotspotOptimizer sut;
    private SearchResults searchResults;

    @BeforeEach
    void setUp() {
        searchResults = new SearchResults();
        sut = new SearchResultHotspotOptimizer();
    }

    @Test
    void assetWasSoldVeryWellBothInLast24HoursAndLast30DaysToo() {
        var purchaseInfoLast24Hours = givenPurchaseInfo(2000L, 1000L);
        var purchaseInfoLast30Days = givenPurchaseInfo(100_000L, 10_000L);
        var asset = givenAsset(purchaseInfoLast24Hours, purchaseInfoLast30Days);

        whenOptimize();

        thenHotspotContainsAssetSeveralTimes(HotspotKey.HighValue, asset, 1);
    }

    private Asset givenAsset(AssetPurchaseInfo purchaseInfoLast24Hours, AssetPurchaseInfo purchaseInfoLast30Days) {
        var asset = new Asset(
                string(), string(), URI(), URI(),
                purchaseInfoLast30Days, purchaseInfoLast24Hours,
                setOfTopics(), anyAssetVendor()
        );

        searchResults.addFound(asset);
        return asset;
    }

    private AssetPurchaseInfo givenPurchaseInfo(long assetShown, long assetSold) {
        return new AssetPurchaseInfo(assetShown, assetSold, new Money(BigDecimal.ZERO), new Money(BigDecimal.ZERO));
    }

    private void whenOptimize() {
        sut.optimize(searchResults);
    }

    private void thenHotspotContainsAssetSeveralTimes(HotspotKey hotspotKey, Asset expectedAsset, long times) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        long actualTimes = hotspot.getMembers().stream()
                .filter(expectedAsset::equals)
                .count();

        assertEquals(times, actualTimes);
    }
}
