package com.assetco.hotspots.optimization;

import com.assetco.search.results.Asset;
import com.assetco.search.results.AssetVendor;
import com.assetco.search.results.Hotspot;
import com.assetco.search.results.SearchResults;

import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;

// This code manages filling the showcase if it's not already set
// it make sure the first partner-lvl vendor with enough assets on the page gets the showcase
//
// From Jamie's reqs:
//   1. If a Partner-level vendor has at least three (3) assets in the result set, that partner's assets shall own the showcase
//   2. If two (2) Partner-level vendors meet the criteria to own the showcase, the first vendor to meet the criteria shall own the showcase
//   3. If a Partner-level has more than five (5) showcase assets, additional assets shall be treated as Top Picks
//
// -johnw
// 1/3/07

/**
 * Assigns assets to the showcase hotspot group based on their vendor status.
 */
class RelationshipBasedOptimizer {
    private static final int MAX_ASSETS_IN_SHOWCASE = 5;
    private static final int ASSETS_COUNTS_TO_OWN_SHOWCASE = 3;

    public void optimize(SearchResults searchResults) {
        // don't affect a showcase built by an earlier rule
        var partnerAssets = new ArrayList<Asset>();
        var goldAssets = new ArrayList<Asset>();
        var silverAssets = new ArrayList<Asset>();

        for (Asset asset : searchResults.getFound()) {
            // HACK! trap gold and silver assets for use later
            if (asset.getVendor().getRelationshipLevel() == Gold) {
                goldAssets.add(asset);
            } else if (asset.getVendor().getRelationshipLevel() == Silver) {
                silverAssets.add(asset);
            } else if (asset.getVendor().getRelationshipLevel() == Partner) {
                // remember this partner asset
                partnerAssets.add(asset);
            }
        }

        // [DBV], 4/14/2014:
        // need added this here even though it's not about this rules
        // frm Jamie,
        // 1. All partner assets should be eligible for high-value slots in the main grid.
        // 2. All partner assets should be eligible to appear in the fold.

        // todo - this does not belong here!!!
        var highValueHotspot = searchResults.getHotspot(HighValue);
        for (var asset : partnerAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // TODO - this needs to be moved to something that only manages the fold
        for (var asset : partnerAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        var showcaseHotspot = searchResults.getHotspot(Showcase);
        if (showcaseHotspot.getMembers().isEmpty()) {
            var showcaseAssets = createShowcaseFromPartnerAssets(partnerAssets);
            showcaseAssets =
                    pushAssetsExceedingAllowedSizeOfShowcaseToHotspot(searchResults.getHotspot(TopPicks), showcaseAssets);
            showcaseAssets.forEach(showcaseHotspot::addMember);
        }

        // acw-14339: gold assets should be in high value hotspots if there are no partner assets in search
        for (var asset : goldAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // acw-14341: gold assets should appear in fold box when appropriate
        for (var asset : goldAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        // LOL acw-14511: gold assets should appear in fold box when appropriate
        for (var asset : silverAssets)
            searchResults.getHotspot(Fold).addMember(asset);
    }

    private static List<Asset> pushAssetsExceedingAllowedSizeOfShowcaseToHotspot(Hotspot hotspot, List<Asset> showcaseAssets) {
        if (showcaseAssets.size() > MAX_ASSETS_IN_SHOWCASE) {
            showcaseAssets.subList(MAX_ASSETS_IN_SHOWCASE, showcaseAssets.size())
                    .forEach(hotspot::addMember);
            showcaseAssets = showcaseAssets.subList(0, MAX_ASSETS_IN_SHOWCASE);
        }
        return showcaseAssets;
    }

    private List<Asset> createShowcaseFromPartnerAssets(List<Asset> partnerAssets) {
        List<Asset> showcaseAssets = new ArrayList<>();
        var assetsByVendorMap = new HashMap<AssetVendor, List<Asset>>();

        for (Asset asset : partnerAssets) {
            var currentAssetList = getAssetsListFromAssetsVendorMap(asset, assetsByVendorMap);

            if (showcaseAssets == currentAssetList || showcaseAssets.size() < ASSETS_COUNTS_TO_OWN_SHOWCASE) {
                showcaseAssets = currentAssetList;
                showcaseAssets.add(asset);
            }
        }

        if (showcaseAssets.size() < ASSETS_COUNTS_TO_OWN_SHOWCASE) {
            return Collections.emptyList();
        }
         return showcaseAssets;
    }

    private static List<Asset> getAssetsListFromAssetsVendorMap(Asset asset, Map<AssetVendor, List<Asset>> assetsByVendorMap) {
        if (!assetsByVendorMap.containsKey(asset.getVendor())) {
            assetsByVendorMap.put(asset.getVendor(), new ArrayList<>());
        }
        return assetsByVendorMap.get(asset.getVendor());
    }
}
