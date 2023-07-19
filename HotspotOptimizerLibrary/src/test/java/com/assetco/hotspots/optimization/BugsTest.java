package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Showcase;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BugsTest {
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

        List<Asset> expected = List.of(
                firstAssetOfPartnerVendor,
                givenAssetInResultsWithVendor(firstPartnerVendor),
                givenAssetInResultsWithVendor(firstPartnerVendor),
                givenAssetInResultsWithVendor(firstPartnerVendor),
                givenAssetInResultsWithVendor(firstPartnerVendor)
        );

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, anotherPartnerAsset);
        thenHotspotHasExactly(Showcase, expected);
    }

    @Test
    void noOneOwnsShowcase() {
        givenAssetInResultsWithVendor(firstPartnerVendor);
        givenAssetInResultsWithVendor(secondPartnerVendor);
        givenAssetInResultsWithVendor(secondPartnerVendor);
        givenAssetInResultsWithVendor(firstPartnerVendor);

        List<Asset> expected = Collections.emptyList();

        whenOptimize();

        thenHotspotHasExactly(Showcase, expected);
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
        var expected = List.of(secondPartnerAssert1, secondPartnerAssert2, secondPartnerAssert3);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, firstPartnerAsset1, firstPartnerAsset2, firstPartnerAsset3, firstPartnerAsset4);
        thenHotspotHasExactly(Showcase, expected);
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor assetVendor) {
        Asset asset = new Asset(
                string(), string(), URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
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
