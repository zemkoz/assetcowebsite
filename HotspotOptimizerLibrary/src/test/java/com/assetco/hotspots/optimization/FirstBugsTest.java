package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Showcase;
import static com.assetco.search.results.HotspotKey.TopPicks;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FirstBugsTest {
    private SearchResultHotspotOptimizer sut;
    private SearchResults searchResults;
    private AssetVendor firstPartnerVendor;
    private AssetVendor secondPartnerVendor;

    @BeforeEach
    void setUp() {
        firstPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        secondPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);

        searchResults = new SearchResults();
        sut = new SearchResultHotspotOptimizer();
    }

    @Test
    void firstPartnerWithLongTrailingAssetsWin() {
        Asset firstAssetOfPartnerVendor = givenAssetInResultsWithVendor(firstPartnerVendor);
        Asset anotherPartnerAsset = givenAssetInResultsWithVendor(secondPartnerVendor);

        List<Asset> expectedAssetsInShowcase = new ArrayList<>();
        expectedAssetsInShowcase.add(firstAssetOfPartnerVendor);
        expectedAssetsInShowcase.addAll(givenAssetInResultsWithVendorMultipleTimes(firstPartnerVendor, 4));

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, anotherPartnerAsset);
        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
        thenHotspotHasExactly(TopPicks, List.of());
    }

    @Test
    void noOneOwnsShowcase() {
        givenAssetInResultsWithVendor(firstPartnerVendor);
        givenAssetInResultsWithVendor(secondPartnerVendor);
        givenAssetInResultsWithVendor(secondPartnerVendor);
        givenAssetInResultsWithVendor(firstPartnerVendor);

        List<Asset> expectedAssetsInShowcase = List.of();

        whenOptimize();

        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
    }

    @Test
    void secondPartnerWinsOnlyWithThreeAssets() {
        var firstPartnerAsset1 = givenAssetInResultsWithVendor(firstPartnerVendor);
        var secondPartnerAssert1 = givenAssetInResultsWithVendor(secondPartnerVendor);
        var secondPartnerAssert2 = givenAssetInResultsWithVendor(secondPartnerVendor);
        var firstPartnerAsset2 = givenAssetInResultsWithVendor(firstPartnerVendor);
        var secondPartnerAssert3 = givenAssetInResultsWithVendor(secondPartnerVendor);
        var firstPartnerAsset3 = givenAssetInResultsWithVendor(firstPartnerVendor);
        var firstPartnerAsset4 = givenAssetInResultsWithVendor(firstPartnerVendor);
        var expectedAssetsInShowcase = List.of(secondPartnerAssert1, secondPartnerAssert2, secondPartnerAssert3);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, firstPartnerAsset1, firstPartnerAsset2, firstPartnerAsset3, firstPartnerAsset4);
        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
    }

    @Test
    void assetsExceedingShowcaseSize() {
        var expectedAssetsInShowcase = givenAssetInResultsWithVendorMultipleTimes(firstPartnerVendor, 5);
        var expectedAssetsInTopPicks = givenAssetInResultsWithVendorMultipleTimes(firstPartnerVendor, 2);

        whenOptimize();

        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
        thenHotspotHasExactly(TopPicks, expectedAssetsInTopPicks);
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor assetVendor) {
        Asset asset = new Asset(
                string(), string(), URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
    }

    private List<Asset> givenAssetInResultsWithVendorMultipleTimes(AssetVendor assetVendor, int times) {
        List<Asset> assetsList = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            assetsList.add(givenAssetInResultsWithVendor(assetVendor));
        }
        return assetsList;
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(string(), string(), relationshipLevel, anyLong());
    }

    private void whenOptimize() {
        sut.optimize(searchResults);
    }

    private void thenHotspotDoesNotHave(HotspotKey hotspotKey, Asset...forbiddenAssets) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        for (var forbiddenAsset : forbiddenAssets) {
            assertFalse(hotspot.getMembers().contains(forbiddenAsset));
        }
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        Asset[] actualAssets = hotspot.getMembers().toArray(new Asset[0]);
        Asset[] expectedAssets = expectedAssetList.toArray(new Asset[0]);

        assertArrayEquals(expectedAssets, actualAssets);
    }
}
