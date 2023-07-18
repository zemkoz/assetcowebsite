package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Showcase;
import static org.junit.jupiter.api.Assertions.*;

class BugsTest {
    private SearchResultHotspotOptimizer searchResultHotspotOptimizer;
    private SearchResults searchResults;

    @BeforeEach
    void setUp() {
        searchResults = new SearchResults();
        searchResultHotspotOptimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        AssetVendor partnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        Asset missingAsset = givenAssetInResultsWithVendor(partnerVendor);

        AssetVendor anotherPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        givenAssetInResultsWithVendor(anotherPartnerVendor);

        List<Asset> expected = List.of(
                givenAssetInResultsWithVendor(partnerVendor),
                givenAssetInResultsWithVendor(partnerVendor),
                givenAssetInResultsWithVendor(partnerVendor),
                givenAssetInResultsWithVendor(partnerVendor)
        );
        
        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missingAsset);
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
        searchResultHotspotOptimizer.optimize(searchResults);
    }

    private void thenHotspotDoesNotHave(HotspotKey hotspotKey, Asset forbiddenAsset) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        assertFalse(hotspot.getMembers().contains(forbiddenAsset));
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        Asset[] actualAssets = hotspot.getMembers().toArray(new Asset[0]);
        Asset[] expectedAssets = expectedAssetList.toArray(new Asset[0]);

        assertArrayEquals(expectedAssets, actualAssets);
    }
}
