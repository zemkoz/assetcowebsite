package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.assetco.hotspots.optimization.Any.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class DealsOptimizerTest {
    private DealsOptimizer sut;

    private SearchResults searchResults;
    private AssetVendor silverVendor;
    private AssetVendor goldVendor;
    private AssetVendor partnerVendor;

    @BeforeEach
    void setUp() {
        sut = new DealsOptimizer();
        searchResults = new SearchResults();

        silverVendor = makeVendor(AssetVendorRelationshipLevel.Silver);
        goldVendor = makeVendor(AssetVendorRelationshipLevel.Gold);
        partnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
    }

    @Test
    void noAssetAvailable() {
        sut.optimize(searchResults, mock(AssetAssessments.class));
        thenHotspotHasExactly(HotspotKey.Deals, List.of());
    }

    @Test
    void eligibleSilverAssetWithProfitMarginSevenToTenAndTopLine1500IsAddedToDealsHotspot() {
        var expectedAsset = givenAsset(silverVendor, 1500L, 1050L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotContains(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithProfitMarginOneToTwoAndHasTopLine1500IsAddedToDealsHotspot() {
        var expectedAsset = givenAsset(silverVendor, 1500L, 750L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotContains(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithoutProfitMarginSevenToTenIsNotAddedToDealsHotspot() {
        var expectedAsset = givenAsset(silverVendor, 1500L, 1051L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of());
    }

    @Test
    void eligibleSilverAssetWithoutTopLine1500IsNotAddedToDealsHotspot() {
        var expectedAsset = givenAsset(silverVendor, 1490L, 1043L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of());
    }

    @Test
    void aNotEligibleSilverAssetWithProfitMarginOneToTwoAndTopLine1500IsAddedToDealsHotspot() {
        var expectedAsset = givenAsset(silverVendor, 1500L, 750L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of(expectedAsset));
    }

    @Test
    void eligibleGoldAssetWithProfitMarginSevenToTenAndTopLine1000IsAddedToDealsHotspot() {
        var expectedAsset = givenAsset(goldVendor, 1000L, 700L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of(expectedAsset));
    }

    @Test
    void eligibleGoldAssetWithoutProfitMarginSevenToTenIsNotAddedToDealsHotspotWhenHighestRelationLevelIsGold() {
        var expectedAsset = givenAsset(goldVendor, 1000L, 701L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of());
    }

    @Test
    void eligibleGoldAssetWithoutTopLine1000IsNotAddedToDealsHotspot() {
        var expectedAsset = givenAsset(goldVendor, 990L, 693L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of());
    }

    @Test
    void aNotEligibleGoldAssetWithProfitMarginOneToTwoAndTopLine1000IsAddedToDealsHotspot() {
        var expectedAsset = givenAsset(goldVendor, 1000L, 500L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenHotspotHasExactly(HotspotKey.Deals, List.of(expectedAsset));
    }

    @Test
    void eligibleSilverAssetIsWithProfitMarginOneToTwoAndTopline1500IsAddedToDealsHotspotWhenHighestRelationshipLevelIsGold() {
        givenAsset(goldVendor);
        var expectedAsset = givenAsset(silverVendor, 1500L, 750L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotContains(expectedAsset);
    }

    @Test
    void notEligibleSilverAssetIsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsGold() {
        givenAsset(goldVendor);
        var expectedAsset = givenAsset(silverVendor, 1500L, 750L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithoutProfitMarginOneToTwoIsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsGold() {
        givenAsset(goldVendor);
        var expectedAsset = givenAsset(silverVendor, 1500L, 751L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }


    @Test
    void eligibleSilverAssetWithoutTopline1500IsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsGold() {
        givenAsset(goldVendor);
        var expectedAsset = givenAsset(silverVendor, 1490L, 745L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligiblePartnerAssetWithProfitMarginSevenToTenIsAddedToDealsHotspot() {
        var asset = givenAsset(partnerVendor, 10L, 7L);

        sut.optimize(searchResults, null);

        thenHotspotHasExactly(HotspotKey.Deals, List.of(asset));
    }

    @Test
    void partnerAssetWithoutProfitMarginSevenToTenIsAddedToDealsHotspot() {
        var asset = givenAsset(partnerVendor, 10L, 7L);

        sut.optimize(searchResults, null);

        thenHotspotHasExactly(HotspotKey.Deals, List.of(asset));
    }

    @Test
    void eligibleGoldAssetWithProfitMarginOneToTwoAndTopline1000IsAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(goldVendor, 1000L, 500L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotContains(expectedAsset);
    }

    @Test
    void aNotEligibleGoldAssetIsAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(goldVendor, 1000L, 500L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleGoldAssetWithoutProfitMarginOneToTwoIsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(goldVendor, 1000L, 501L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleGoldAssetWithTopline1000IsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(goldVendor, 998L, 499L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithProfitMarginOneToFourAndTopline10000IsAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(silverVendor, 10000L, 2500L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotContains(expectedAsset);
    }

    @Test
    void aNotEligibleSilverAssetIsAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(silverVendor, 10000L, 2500L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, false);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithoutProfitMarginOneToFourIsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(silverVendor, 10000L, 2501L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    @Test
    void eligibleSilverAssetWithTopline10000IsNotAddedToDealsHotspotWhenHighestRelationshipLevelIsPartner() {
        givenAsset(partnerVendor);
        var expectedAsset = givenAsset(silverVendor, 9996L, 249L);
        var assessmentsStub = givenAssetAssessmentsStubForAsset(expectedAsset, true);

        sut.optimize(searchResults, assessmentsStub);

        thenDealsHotspotDoesNotContain(expectedAsset);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(string(), string(), relationshipLevel, anyLong());
    }

    private Asset givenAsset(AssetVendor assetVendor, long last30DaysRevenue, long last30DaysRoyalties) {
        var last30DaysPurchaseInfo = givenPurchaseInfo(last30DaysRevenue, last30DaysRoyalties);
        Asset asset = new Asset(
                string(), string(), URI(), URI(), last30DaysPurchaseInfo, assetPurchaseInfo(), setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
    }

    private Asset givenAsset(AssetVendor assetVendor) {
        Asset asset = new Asset(
                string(), string(), URI(), URI(), assetPurchaseInfo(), assetPurchaseInfo(), setOfTopics(), assetVendor);
        searchResults.addFound(asset);
        return asset;
    }

    private AssetAssessments givenAssetAssessmentsStubForAsset(Asset asset, boolean isAssetEligible) {
        AssetAssessments assessmentsStub = mock(AssetAssessments.class);
        when(assessmentsStub.isAssetDealEligible(asset)).thenReturn(isAssetEligible);
        return assessmentsStub;
    }

    private AssetPurchaseInfo givenPurchaseInfo(long revenue, long royalties) {
        return new AssetPurchaseInfo(1, 1,
                new Money(BigDecimal.valueOf(revenue)), new Money(BigDecimal.valueOf(royalties)));
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expectedAssetList) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        assertEquals(expectedAssetList, hotspot.getMembers());
    }

    private void thenDealsHotspotContains(Asset expectedAsset) {
        Hotspot hotspot = searchResults.getHotspot(HotspotKey.Deals);
        assertTrue(hotspot.getMembers().contains(expectedAsset), "Hotspot deals doesn't contain expected asset.");
    }

    private void thenDealsHotspotDoesNotContain(Asset expectedAsset) {
        Hotspot hotspot = searchResults.getHotspot(HotspotKey.Deals);
        assertFalse(hotspot.getMembers().contains(expectedAsset), "Hotspot deals wouldn't contain the expected asset.");
    }
}