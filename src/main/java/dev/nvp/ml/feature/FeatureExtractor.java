package dev.nvp.ml.feature;

/**
 * Builds a FeatureVector from a context object. Each check carries its own
 * extractor + schema so they can evolve independently.
 *
 * @param <C> per-check context type (e.g., HitContext, BlockPlaceContext).
 */
public interface FeatureExtractor<C> {
    FeatureSchema schema();
    FeatureVector extract(C context);
}
