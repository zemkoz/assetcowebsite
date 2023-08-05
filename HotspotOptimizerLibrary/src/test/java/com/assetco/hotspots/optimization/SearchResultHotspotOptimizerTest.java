package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SearchResultHotspotOptimizerTest {
    private SearchResultHotspotOptimizer sut;
    private SearchResults searchResults;

    @BeforeEach
    void setUp() {
        sut = new SearchResultHotspotOptimizer();
        searchResults = new SearchResults();
    }

    @Test
    void partnerLevelAssetWith30DaysPurchaseInfoIsAddedToDealsHotspot() {
        var asset = givenAssetInResultsWithVendorAnd30DaysPurchaseInfo(
                makeVendor(AssetVendorRelationshipLevel.Partner),
                givenPurchaseInfo(1000, 0));

        whenOptimize();

        thenHotspotHasExactly(HotspotKey.Deals, List.of(asset));
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(string(), string(), relationshipLevel, anyLong());
    }

    private Asset givenAssetInResultsWithVendorAnd30DaysPurchaseInfo(AssetVendor assetVendor,
                                                                     AssetPurchaseInfo purchaseInfoOfLast30Days) {
        var asset = new Asset(
                string(), string(), URI(), URI(), purchaseInfoOfLast30Days, assetPurchaseInfo(),
                setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
    }

    private AssetPurchaseInfo givenPurchaseInfo(long revenue, long royaltiesOwed) {
        return new AssetPurchaseInfo(anyLong(),
                anyLong(),
                new Money(BigDecimal.valueOf(revenue)),
                new Money(BigDecimal.valueOf(royaltiesOwed))
        );
    }

    private void whenOptimize() {
        sut.optimize(searchResults);
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        Asset[] actualAssets = hotspot.getMembers().toArray(new Asset[0]);
        Asset[] expectedAssets = expectedAssetList.toArray(new Asset[0]);

        assertArrayEquals(expectedAssets, actualAssets);
    }
}

