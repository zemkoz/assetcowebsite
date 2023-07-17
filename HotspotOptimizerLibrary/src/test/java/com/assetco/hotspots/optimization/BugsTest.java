package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Showcase;

class BugsTest {

    @Test
    void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        AssetVendor partnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        Asset missingAsset = givenAssetInResultsWithVendor(partnerVendor);

        AssetVendor anotherPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        Asset disruptingAsset = givenAssetInResultsWithVendor(anotherPartnerVendor);

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
        return new Asset(string(), string(), URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), setOfTopics(), assetVendor);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(string(), string(), relationshipLevel, anyLong());
    }

    private void whenOptimize() {
    }

    private void thenHotspotDoesNotHave(HotspotKey hotspotKey, Asset asset) {
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
    }
}
