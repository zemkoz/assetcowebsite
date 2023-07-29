package com.assetco.hotspots.optimization;

import com.assetco.search.results.AssetTopic;

/**
 * Acquires a list of topics, such as the current, ordered list of hot topics.
 */
public interface AssetTopicsSource {
    /**
     * Fetch the list of topics encapsulated by this object.
     */
    Iterable<AssetTopic> getTopics();
}
