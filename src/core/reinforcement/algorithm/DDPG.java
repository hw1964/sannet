/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.reinforcement.algorithm;

import core.reinforcement.agent.AgentException;
import core.reinforcement.agent.Environment;
import core.reinforcement.agent.StateSynchronization;
import core.reinforcement.function.FunctionEstimator;
import core.reinforcement.policy.Policy;
import core.reinforcement.policy.executablepolicy.ExecutablePolicyType;
import core.reinforcement.policy.updateablepolicy.UpdateableQPolicy;
import core.reinforcement.value.QTargetValueFunctionEstimator;
import core.reinforcement.value.ValueFunction;
import utils.configurable.DynamicParamException;
import utils.matrix.MatrixException;

import java.io.IOException;

/**
 * Implements Deep Deterministic Policy Gradient (DDPG) algorithm.<br>
 * Reference: <a href="https://towardsdatascience.com/deep-deterministic-policy-gradient-ddpg-theory-and-implementation-747a3010e82f">...</a> <br>
 *
 */
public class DDPG extends AbstractPolicyGradient {

    /**
     * Constructor for Deep Deterministic Policy Gradient
     *
     * @param stateSynchronization reference to state synchronization.
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @param valueFunctionEstimator reference to value function estimator.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public DDPG(StateSynchronization stateSynchronization, Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator, FunctionEstimator valueFunctionEstimator) throws DynamicParamException, AgentException {
        super(stateSynchronization, environment, new UpdateableQPolicy(executablePolicyType, policyFunctionEstimator, valueFunctionEstimator), new QTargetValueFunctionEstimator(valueFunctionEstimator));
    }

    /**
     * Constructor for Deep Deterministic Policy Gradient
     *
     * @param stateSynchronization reference to state synchronization.
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @param valueFunctionEstimator reference to value function estimator.
     * @param params parameters for agent.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public DDPG(StateSynchronization stateSynchronization, Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator, FunctionEstimator valueFunctionEstimator, String params) throws DynamicParamException, AgentException {
        super(stateSynchronization, environment, new UpdateableQPolicy(executablePolicyType, policyFunctionEstimator, valueFunctionEstimator), new QTargetValueFunctionEstimator(valueFunctionEstimator), params);
    }

    /**
     * Returns reference to algorithm.
     *
     * @return reference to algorithm.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if neural network has less output than actions.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public DDPG reference() throws MatrixException, IOException, DynamicParamException, ClassNotFoundException, AgentException {
        ValueFunction newValueFunction = valueFunction.reference(false, false);
        Policy newPolicy = policy.reference(newValueFunction.getFunctionEstimator());
        return new DDPG(getStateSynchronization(), getEnvironment(), policy.getExecutablePolicy().getExecutablePolicyType(), newPolicy.getFunctionEstimator(), newValueFunction.getFunctionEstimator(), getParams());
    }

    /**
     * Returns reference to algorithm.
     *
     * @param sharedPolicyFunctionEstimator if true shared policy function estimator is used otherwise new policy function estimator is created.
     * @param sharedMemory if true shared memory is used between estimators.
     * @return reference to algorithm.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if neural network has less output than actions.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public DDPG reference(boolean sharedPolicyFunctionEstimator, boolean sharedMemory) throws MatrixException, IOException, DynamicParamException, ClassNotFoundException, AgentException {
        ValueFunction newValueFunction = valueFunction.reference(false, sharedMemory);
        Policy newPolicy = policy.reference(newValueFunction.getFunctionEstimator(), sharedPolicyFunctionEstimator, newValueFunction.getFunctionEstimator().getMemory());
        return new DDPG(getStateSynchronization(), getEnvironment(), policy.getExecutablePolicy().getExecutablePolicyType(), newPolicy.getFunctionEstimator(), newValueFunction.getFunctionEstimator(), getParams());
    }

    /**
     * Returns reference to algorithm.
     *
     * @param sharedPolicyFunctionEstimator if true shared policy function estimator is used otherwise new policy function estimator is created.
     * @param sharedValueFunctionEstimator if true shared value function estimator is used otherwise new policy function estimator is created.
     * @param sharedMemory if true shared memory is used between estimators.
     * @return reference to algorithm.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if neural network has less output than actions.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public DDPG reference(boolean sharedPolicyFunctionEstimator, boolean sharedValueFunctionEstimator, boolean sharedMemory) throws MatrixException, IOException, DynamicParamException, ClassNotFoundException, AgentException {
        ValueFunction newValueFunction = valueFunction.reference(sharedValueFunctionEstimator, sharedMemory);
        Policy newPolicy = policy.reference(newValueFunction.getFunctionEstimator(), sharedPolicyFunctionEstimator, newValueFunction.getFunctionEstimator().getMemory());
        return new DDPG(getStateSynchronization(), getEnvironment(), policy.getExecutablePolicy().getExecutablePolicyType(), newPolicy.getFunctionEstimator(), newValueFunction.getFunctionEstimator(), getParams());
    }

}
