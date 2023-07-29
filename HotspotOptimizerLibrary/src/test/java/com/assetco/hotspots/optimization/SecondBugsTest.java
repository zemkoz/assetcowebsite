package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static com.assetco.search.results.HotspotKey.Highlight;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecondBugsTest {
    private SearchResultHotspotOptimizer sut;
    private SearchResults searchResults;

    private AssetTopic regularTopic;
    private AssetTopic higherPriorityHotTopic;
    private AssetTopic lowerPriorityHotTopic;


    @BeforeEach
    void setUp() {
        regularTopic = givenTopic("regularTopic");
        higherPriorityHotTopic = givenTopic("HigherPriorityTopic");
        lowerPriorityHotTopic = givenTopic("lowerPriorityTopic");

        sut = new SearchResultHotspotOptimizer();
        sut.setHotTopics(() -> List.of(higherPriorityHotTopic, lowerPriorityHotTopic));

        searchResults = new SearchResults();
    }

    @Test
    void allHotTopicAssetsAreHighlighted() {
        var expected = new ArrayList<Asset>();
        expected.add(givenAsset("asset1", List.of(lowerPriorityHotTopic)));
        expected.add(givenAsset("asset2", List.of(lowerPriorityHotTopic)));
        expected.add(givenAsset("asset3", List.of(higherPriorityHotTopic)));
        givenAsset("asset4", List.of(regularTopic));
        expected.add(givenAsset("asset5", List.of(lowerPriorityHotTopic)));
        expected.add(givenAsset("asset6", List.of(higherPriorityHotTopic)));
        givenAsset("asset7", List.of(regularTopic));
        expected.add(givenAsset("asset8", List.of(higherPriorityHotTopic)));
        expected.add(givenAsset("asset9", List.of(lowerPriorityHotTopic)));

        whenOptimize();

        thenHotspotContains(Highlight, expected);
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
