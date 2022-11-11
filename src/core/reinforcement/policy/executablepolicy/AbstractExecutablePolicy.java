/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2022 Simo Aaltonen
 */

package core.reinforcement.policy.executablepolicy;

import core.reinforcement.agent.StateTransition;
import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;
import utils.matrix.Matrix;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Implements abstract executable policy which contains shared functions for executable policies.<br>
 *
 */
public abstract class AbstractExecutablePolicy implements ExecutablePolicy, Serializable {

    @Serial
    private static final long serialVersionUID = -3999341188546094490L;

    /**
     * Parameter name types for abstract executable policy.
     *     - asSoftMax: true if action values are recorded as softmax values (e^x).<br>
     *
     */
    private final static String paramNameTypes = "(asSoftMax:BOOLEAN)";

    /**
     * Record that defines ActionValueTuple for policy.
     *
     * @param action action value.
     * @param value value for action.
     */
    protected record ActionValueTuple(int action, double value) {
    }

    /**
     * Executable policy type.
     *
     */
    private final ExecutablePolicyType executablePolicyType;

    /**
     * True if values are recorded as softmax values (e^x).
     *
     */
    protected boolean asSoftMax;

    /**
     * Default constructor for abstract executable policy.
     *
     * @param executablePolicyType executable policy type.
     */
    AbstractExecutablePolicy(ExecutablePolicyType executablePolicyType) {
        this.executablePolicyType = executablePolicyType;
        initializeDefaultParams();
    }

    /**
     * Default constructor for abstract executable policy.
     *
     * @param executablePolicyType executable policy type.
     * @param params parameters for abstract executable policy.
     * @param paramNameTypes parameter names types
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    AbstractExecutablePolicy(ExecutablePolicyType executablePolicyType, String params, String paramNameTypes) throws DynamicParamException {
        this(executablePolicyType);
        if (params != null) setParams(new DynamicParam(params, AbstractExecutablePolicy.paramNameTypes + (paramNameTypes != null ? ", " + paramNameTypes : "")));
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        asSoftMax = false;
    }

    /**
     * Returns parameters used for abstract executable policy.
     *
     * @return parameters used for abstract executable policy.
     */
    public String getParamDefs() {
        return AbstractExecutablePolicy.paramNameTypes;
    }

    /**
     * Sets parameters used for abstract executable policy.<br>
     * <br>
     * Supported parameters are:<br>
     *     - asSoftMax: true if action values are recorded as softmax values (e^x).<br>
     *
     * @param params parameters used for abstract executable policy.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        if (params.hasParam("asSoftMax")) asSoftMax = params.getValueAsBoolean("asSoftMax");
    }

    /**
     * Sets flag if agent is in learning mode.
     *
     * @param isLearning if true agent is in learning mode.
     */
    public void setLearning(boolean isLearning) {
    }

    /**
     * Takes action decided by external agent.
     *
     * @param policyValueMatrix current state value matrix.
     * @param availableActions available actions in current state
     * @param action action.
     */
    public void action(Matrix policyValueMatrix, HashSet<Integer> availableActions, int action) {
    }

    /**
     * Takes action based on policy.
     *
     * @param policyValueMatrix current state value matrix.
     * @param availableActions available actions in current state
     * @param alwaysGreedy if true greedy action is always taken.
     * @return action taken.
     */
    public int action(Matrix policyValueMatrix, HashSet<Integer> availableActions, boolean alwaysGreedy) {
        TreeSet<ActionValueTuple> stateValueSet = new TreeSet<>(Comparator.comparingDouble(o -> o.value));
        for (Integer action : availableActions) {
            stateValueSet.add(new ActionValueTuple(action, !asSoftMax ? policyValueMatrix.getValue(action, 0) : Math.exp(policyValueMatrix.getValue(action, 0))));
        }
        return stateValueSet.isEmpty() ? -1 : alwaysGreedy ? Objects.requireNonNull(stateValueSet.pollLast()).action : getAction(stateValueSet);
    }

    protected double getActionEntropy(TreeSet<ActionValueTuple> stateValueSet) {
        double entropy = 0;
        double actionValueSum = 0;
        for (ActionValueTuple actionValueTuple : stateValueSet) actionValueSum += (actionValueTuple.value() + 10E-08);
        for (ActionValueTuple actionValueTuple : stateValueSet) {
            entropy += -((actionValueTuple.value() + 10E-08) / actionValueSum) * Math.log10((actionValueTuple.value() + 10E-08) / actionValueSum);
        }
        return entropy;
    }

    /**
     * Adds state transition for action execution.
     *
     * @param stateTransition state transition.
     */
    public void add(StateTransition stateTransition) {
    }

    /**
     * Ends episode.
     *
     */
    public void endEpisode() {
    }

    /**
     * Returns action based on policy.
     *
     * @param stateValueSet priority queue containing action values in decreasing order.
     * @return chosen action.
     */
    protected abstract int getAction(TreeSet<ActionValueTuple> stateValueSet);

    /**
     * Returns executable policy type.
     *
     * @return executable policy type.
     */
    public ExecutablePolicyType getExecutablePolicyType() {
        return executablePolicyType;
    }

}
