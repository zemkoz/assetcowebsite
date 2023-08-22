package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Showcase;
import static com.assetco.search.results.HotspotKey.TopPicks;
import static org.junit.jupiter.api.Assertions.*;

class RelationshipBasedOptimizerTest {
    private RelationshipBasedOptimizer sut;

    private SearchResults searchResults;
    private AssetVendor silverVendor;
    private AssetVendor goldVendor;
    private AssetVendor firstPartnerVendor;
    private AssetVendor secondPartnerVendor;

    @BeforeEach
    void setUp() {
        sut = new RelationshipBasedOptimizer();
        searchResults = new SearchResults();

        silverVendor = makeVendor(AssetVendorRelationshipLevel.Silver);
        goldVendor = makeVendor(AssetVendorRelationshipLevel.Gold);
        firstPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        secondPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);    }

    @Test
    void assetsFromMultipleVendorsAreAddedToFoldHotspot() {
        var goldAsset1 = givenAsset(goldVendor);
        var silverAsset1 = givenAsset(silverVendor);
        var silverAsset2 = givenAsset(silverVendor);
        var partnerAsset1 = givenAsset(firstPartnerVendor);
        var goldAsset2 = givenAsset(goldVendor);

        sut.optimize(searchResults);

        thenHotspotContains(HotspotKey.Fold,
                silverAsset1, silverAsset2,
                goldAsset1, goldAsset2,
                partnerAsset1
        );
    }

    @Test
    void onlyGoldAndPartnerAssetsAreAddedToEmptyHighValueHotspot() {
        var goldAsset1 = givenAsset(goldVendor);
        var silverAsset1 = givenAsset(silverVendor);
        var silverAsset2 = givenAsset(silverVendor);
        var partnerAsset1 = givenAsset(firstPartnerVendor);
        var goldAsset2 = givenAsset(goldVendor);

        sut.optimize(searchResults);

        thenHotspotContains(HotspotKey.HighValue,
                goldAsset1, goldAsset2,
                partnerAsset1
        );

        thenHotspotDoesNotContain(HotspotKey.HighValue, silverAsset1, silverAsset2);
    }

    @Test
    void firstPartnerWithLongTrailingAssetsOwnsShowcase() {
        Asset firstAssetOfPartnerVendor = givenAsset(firstPartnerVendor);
        Asset anotherPartnerAsset = givenAsset(secondPartnerVendor);

        List<Asset> expectedAssetsInShowcase = new ArrayList<>();
        expectedAssetsInShowcase.add(firstAssetOfPartnerVendor);
        expectedAssetsInShowcase.addAll(givenAssetMultipleTimes(firstPartnerVendor, 4));

        sut.optimize(searchResults);

        thenHotspotDoesNotContain(Showcase, anotherPartnerAsset);
        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
        thenHotspotHasExactly(TopPicks, List.of());
    }

    @Test
    void noOneFromPartnerVendorsOwnsShowcase() {
        givenAsset(firstPartnerVendor);
        givenAsset(secondPartnerVendor);
        givenAsset(secondPartnerVendor);
        givenAsset(firstPartnerVendor);
        List<Asset> expectedAssetsInShowcase = List.of();

        sut.optimize(searchResults);

        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
        thenHotspotHasExactly(TopPicks, List.of());
    }

    @Test
    void secondPartnerOwnsShowcaseOnlyWithThreeAssets() {
        var firstPartnerAsset1 = givenAsset(firstPartnerVendor);
        var secondPartnerAssert1 = givenAsset(secondPartnerVendor);
        var secondPartnerAssert2 = givenAsset(secondPartnerVendor);
        var firstPartnerAsset2 = givenAsset(firstPartnerVendor);
        var secondPartnerAssert3 = givenAsset(secondPartnerVendor);
        var firstPartnerAsset3 = givenAsset(firstPartnerVendor);
        var firstPartnerAsset4 = givenAsset(firstPartnerVendor);

        sut.optimize(searchResults);

        thenHotspotDoesNotContain(Showcase, firstPartnerAsset1, firstPartnerAsset2, firstPartnerAsset3, firstPartnerAsset4);
        thenHotspotContains(Showcase, secondPartnerAssert1, secondPartnerAssert2, secondPartnerAssert3);
        thenHotspotHasExactly(TopPicks, List.of());
    }

    @Test
    void partnerAssetsExceedingShowcaseSize() {
        var expectedAssetsInShowcase = givenAssetMultipleTimes(firstPartnerVendor, 5);
        var expectedAssetsInTopPicks = givenAssetMultipleTimes(firstPartnerVendor, 2);

        sut.optimize(searchResults);

        thenHotspotHasExactly(Showcase, expectedAssetsInShowcase);
        thenHotspotHasExactly(TopPicks, expectedAssetsInTopPicks);
    }

    @Test
    void eachGoldAndPartnerAssetIsUniqueInHighValueHotspot() {
        var goldAsset1 = givenAsset(goldVendor);
        var silverAsset1 = givenAsset(silverVendor);
        var silverAsset2 = givenAsset(silverVendor);
        var partnerAsset1 = givenAsset(firstPartnerVendor);
        var goldAsset2 = givenAsset(goldVendor);
        givenHotspotMembers(HotspotKey.HighValue, goldAsset2, partnerAsset1);

        sut.optimize(searchResults);

        thenHotspotSize(HotspotKey.HighValue, 3);
        thenHotspotContains(HotspotKey.HighValue, goldAsset1, goldAsset2, partnerAsset1);
        thenHotspotDoesNotContain(HotspotKey.HighValue, silverAsset1, silverAsset2);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(string(), string(), relationshipLevel, anyLong());
    }

    private Asset givenAsset(AssetVendor assetVendor) {
        Asset asset = new Asset(
                string(), string(), URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
    }

    private List<Asset> givenAssetMultipleTimes(AssetVendor assetVendor, int times) {
        List<Asset> assetsList = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            assetsList.add(givenAsset(assetVendor));
        }
        return assetsList;
    }

    private void givenHotspotMembers(HotspotKey hotspotKey, Asset...assets) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        for (var asset : assets) {
            hotspot.addMember(asset);
        }
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        assertEquals(expectedAssetList, hotspot.getMembers());
    }

    private void thenHotspotContains(HotspotKey hotspotKey, Asset... expectedAssets) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        for (var asset : expectedAssets) {
            assertTrue(hotspot.getMembers().contains(asset),
                    "Hotspot " + hotspot + " deals doesn't contain expected asset id=" + asset.getId() + ".");
        }
    }

    private void thenHotspotDoesNotContain(HotspotKey hotspotKey, Asset... expectedAssets) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        for (var asset : expectedAssets) {
            assertFalse(hotspot.getMembers().contains(asset),
                    "Hotspot " + hotspot + " wouldn't contain the expected asset id=" + asset.getId() + ".");
        }
    }

    private void thenHotspotSize(HotspotKey hotspotKey, int expectedSize) {
        assertEquals(expectedSize, searchResults.getHotspot(hotspotKey).getMembers().size());
    }
}