/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2021 Simo Aaltonen
 */

package core.reinforcement.policy.executablepolicy;

import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;

import java.util.Objects;
import java.util.Random;
import java.util.TreeSet;

/**
 * Class that defines NoisyNextBestPolicy.<br>
 * Policy make a greedy decision (chooses best policy) or next best policy according to exploration probability.<br>
 *
 */
public class NoisyNextBestPolicy extends AbstractExecutablePolicy {

    /**
     * Parameter name types for NoisyNextBestPolicy.
     *     - initialExplorationNoise: initial exploration noise for NoisyNextBestPolicy. Default value 1.<br>
     *     - minExplorationNoise: minimum exploration noise for NoisyNextBestPolicy. Default value 0.2.<br>
     *     - explorationNoiseDecay: decay factor for exploration noise. Default value 0.999.<br>
     *
     */
    private final static String paramNameTypes = "(initialExplorationNoise:DOUBLE), " +
            "(minExplorationNoise:DOUBLE), " +
            "(explorationNoiseDecay:DOUBLE)";

    /**
     * Executable policy type.
     *
     */
    private final ExecutablePolicyType executablePolicyType = ExecutablePolicyType.NOISY_NEXT_BEST;

    /**
     * Random function for NoisyNextBestPolicy.
     *
     */
    private final Random random = new Random();

    /**
     * Exploration noise for NoisyNextBestPolicy.
     *
     */
    private double explorationNoise;

    /**
     * Exploration noise for NoisyNextBestPolicy.
     *
     */
    private double initialExplorationNoise;

    /**
     * Minimum exploration noise for NoisyNextBestPolicy.
     *
     */
    private double minExplorationNoise;

    /**
     * Decay for exploration noise for NoisyNextBestPolicy.
     *
     */
    private double explorationNoiseDecay;

    /**
     * Constructor for NoisyNextBestPolicy.
     *
     */
    public NoisyNextBestPolicy() {
        super();
    }

    /**
     * Constructor for NoisyNextBestPolicy.
     *
     * @param params parameters for NoisyNextBestPolicy.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public NoisyNextBestPolicy(String params) throws DynamicParamException {
        super(params, NoisyNextBestPolicy.paramNameTypes);
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        super.initializeDefaultParams();
        initialExplorationNoise = 1;
        minExplorationNoise = 0.2;
        explorationNoiseDecay = 0.999;
        explorationNoise = initialExplorationNoise;
    }

    /**
     * Returns parameters used for NoisyNextBestPolicy.
     *
     * @return parameters used for NoisyNextBestPolicy.
     */
    public String getParamDefs() {
        return super.getParamDefs() + ", " + NoisyNextBestPolicy.paramNameTypes;
    }

    /**
     * Sets parameters used for NoisyNextBestPolicy.<br>
     * <br>
     * Supported parameters are:<br>
     *     - initialExplorationNoise: initial exploration noise for NoisyNextBestPolicy. Default value 1.<br>
     *     - minExplorationNoise: minimum exploration noise for NoisyNextBestPolicy. Default value 0.2.<br>
     *     - explorationNoiseDecay: decay factor for exploration noise. Default value 0.999.<br>
     *
     * @param params parameters used for NoisyNextBestPolicy.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        super.setParams(params);
        if (params.hasParam("initialExplorationNoise")) initialExplorationNoise = params.getValueAsDouble("initialExplorationNoise");
        if (params.hasParam("minExplorationNoise")) minExplorationNoise = params.getValueAsDouble("minExplorationNoise");
        if (params.hasParam("explorationNoiseDecay")) explorationNoiseDecay = params.getValueAsDouble("explorationNoiseDecay");
        explorationNoise = initialExplorationNoise;
    }

    /**
     * Increments policy.
     *
     */
    public void increment() {
        if (explorationNoise > minExplorationNoise) explorationNoise *= explorationNoiseDecay;
    }

    /**
     * Returns action based on policy.
     *
     * @param stateValueSet priority queue containing action values in decreasing order.
     * @return chosen action.
     */
    protected int getAction(TreeSet<ActionValueTuple> stateValueSet) {
        if (stateValueSet.size() > 1 && explorationNoise > random.nextDouble()) stateValueSet.pollLast();
        return stateValueSet.isEmpty() ? -1 : Objects.requireNonNull(stateValueSet.pollLast()).action();
    }

    /**
     * Returns executable policy type.
     *
     * @return executable policy type.
     */
    public ExecutablePolicyType getExecutablePolicyType() {
        return executablePolicyType;
    }

}
