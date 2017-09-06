package org.drools.learner.eval.heuristic;

public interface HeuristicFactory {
    public Heuristic newHeuristic();

    public static HeuristicFactory ENTROPY_FACTORY = new EntropyFactory();

    public static HeuristicFactory MIN_ENTROPY_FACTORY = new MinEntropyFactory();

    public static HeuristicFactory GAIN_RATIO__FACTORY = new GainRatioFactory();

    public static HeuristicFactory RANDOM_INFO_FACTORY = new RandomInfoFactory();

    public static enum Heuristics {
        ENTROPY(ENTROPY_FACTORY), MIN_ENTROPY(MIN_ENTROPY_FACTORY), GAIN_RATIO(GAIN_RATIO__FACTORY), RANDOM_INFO(RANDOM_INFO_FACTORY);
        private HeuristicFactory factory;

        Heuristics(HeuristicFactory factory) {
            this.factory = factory;
        }

        public Heuristic newHeuristic() {
            return factory.newHeuristic();
        }
    }

    public static class EntropyFactory implements HeuristicFactory {
        @Override public Heuristic newHeuristic() {
            return new InformationGain();
        }
    }

    public static class MinEntropyFactory implements HeuristicFactory {
        @Override public Heuristic newHeuristic() {
            return new MinEntropy();
        }
    }

    public static class GainRatioFactory implements HeuristicFactory {
        @Override public Heuristic newHeuristic() {
            return new GainRatio();
        }
    }

    public static class RandomInfoFactory implements HeuristicFactory {
        @Override public Heuristic newHeuristic() {
            return new GainRatio();
        }
    }
}
