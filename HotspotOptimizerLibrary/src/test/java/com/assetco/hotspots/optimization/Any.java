package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Used to generate test data.
 */
class Any {
    private static final Random RANDOM_GENERATOR = new Random();

    private Any() {
        // Shouldn't be instantiated.
    }

    /**
     * Generate a topic that can be used for testing.
     */
    static AssetTopic anyTopic() {
        return new AssetTopic(string(), string());
    }

    /**
     * Generate some purchase info that can be used for testing.
     */
    static AssetPurchaseInfo assetPurchaseInfo() {
        return new AssetPurchaseInfo(anyLong(), anyLong(), money(), money());
    }

    /**
     * Generate some amount of money.
     */
    static Money money() {
        return new Money(new BigDecimal(anyLong()));
    }

    /**
     * Generate some URI.
     */
    static URI URI() {
        return URI.create("https://" + string());
    }

    /**
     * Generate a String.
     */
    static String string() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a long integer.
     */
    static long anyLong() {
        return RANDOM_GENERATOR.nextInt();
    }

    /**
     * Genearte a set of topics with at least one topic in it but not more than five.
     */
    static List<AssetTopic> setOfTopics() {
        var result = new ArrayList<AssetTopic>();
        for (var count = 1 + RANDOM_GENERATOR.nextInt(4); count > 0; --count)
            result.add(anyTopic());

        return result;
    }
}
