package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Highlight;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecondBugsTest {
    private SearchResultHotspotOptimizer sut;
    private SearchResults searchResults;

    private AssetTopic higherPriorityTopic;
    private AssetTopic lowerPriorityTopic;


    @BeforeEach
    void setUp() {
        higherPriorityTopic = givenTopic("HigherPriorityTopic");
        lowerPriorityTopic = givenTopic("lowerPriorityTopic");

        sut = new SearchResultHotspotOptimizer();
        sut.setHotTopics(() -> List.of(higherPriorityTopic, lowerPriorityTopic));

        searchResults = new SearchResults();
    }

    @Test
    void someHotTopicAssetsAreNotHighlighted() {
        var asset1 = givenAsset("asset1", List.of(lowerPriorityTopic));
        var asset2 = givenAsset("asset2", List.of(lowerPriorityTopic));
        var asset3 = givenAsset("asset3", List.of(higherPriorityTopic));
        var asset4 = givenAsset("asset4", List.of(lowerPriorityTopic));
        var asset5 = givenAsset("asset5", List.of(higherPriorityTopic));
        var asset6 = givenAsset("asset6", List.of(higherPriorityTopic));
        var asset7 = givenAsset("asset7", List.of(lowerPriorityTopic));

        var expectedHighlightedAssets = List.of(asset1, asset2, asset3, asset4, asset5);

        whenOptimize();

        thenHotspotContains(Highlight, expectedHighlightedAssets);
        thenHotspotDoesNotHave(Highlight, asset6, asset7);
    }

    private AssetTopic givenTopic(String topicId) {
        return new AssetTopic(topicId, topicId);
    }

    private Asset givenAsset(String title, List<AssetTopic> topics) {
        Asset asset = new Asset(
                title.toLowerCase(), title, URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), topics, anyAssetVendor());
        searchResults.addFound(asset);
        return asset;
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

    private void thenHotspotContains(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        for (var expected : expectedAssetList) {
            assertTrue(hotspot.getMembers().contains(expected));
        }
    }
}
