/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.reinforcement.agent;

import core.network.NeuralNetwork;
import core.network.NeuralNetworkException;
import core.reinforcement.function.FunctionEstimator;
import core.reinforcement.function.NNFunctionEstimator;
import core.reinforcement.policy.Policy;
import core.reinforcement.value.ValueFunction;
import utils.configurable.Configurable;
import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;
import utils.matrix.MatrixException;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Implements deep agent.<br>
 *
 */
public abstract class DeepAgent implements Agent, Configurable, Serializable {

    @Serial
    private static final long serialVersionUID = -1720953512017473344L;

    /**
     * Parameter name types for deep agent.
     *     - updateValuePerEpisode: if true updates value after each episode is completed. Default value false.<br>
     *     - agentUpdateCycle: estimator update cycle. Default value 1.<br>
     *     - rewardTau: tau value for reward averaging in non-episodic learning. Default value 0.9.<br>
     *     - nonEpisodicTrajectoryHistorySize: moving historical trajectory size for non-episodic agent. Default value 11.<br>
     *
     */
    private final static String paramNameTypes = "(updateValuePerEpisode:BOOLEAN), " +
            "(agentUpdateCycle:INT), " +
            "(rewardTau:DOUBLE), " +
            "(nonEpisodicTrajectoryHistorySize:INT)";

    /**
     * Parameters for deep agent.
     *
     */
    private String params = null;

    /**
     * Reference to state synchronization.
     *
     */
    private final StateSynchronization stateSynchronization;

    /**
     * Reference to environment.
     *
     */
    private Environment environment;

    /**
     * True if environment is episodic.
     *
     */
    private final boolean episodic;

    /**
     * If true updates value after each episode is completed.
     *
     */
    protected boolean updateValuePerEpisode;

    /**
     * Reference to current state.
     *
     */
    private transient State state;

    /**
     * Reference to current policy.
     *
     */
    protected final Policy policy;

    /**
     * Reference to value function.
     *
     */
    protected final ValueFunction valueFunction;

    /**
     * Update cycle in episode steps for function estimator.
     *
     */
    private int agentUpdateCycle;

    /**
     * Average reward for non-episodic learning.
     *
     */
    private double averageReward = Double.MIN_VALUE;

    /**
     * Tau value for reward averaging.
     *
     */
    private double rewardTau;

    /**
     * Tau value for reward moving averaging.
     *
     */
    private double rewardMovingAverageTau;

    /**
     * If true agent is in learning mode.
     *
     */
    private boolean isLearning;

    /**
     * Cumulative reward when agent is learning.
     *
     */
    private double cumulativeRewardAsLearning = 0;


    /**
     * Cumulative reward when agent is not learning.
     *
     */
    private double cumulativeRewardAsNotLearning = 0;

    /**
     * Moving average reward when agent is learning.
     *
     */
    private double movingAverageRewardAsLearning = Double.MIN_VALUE;


    /**
     * Moving average reward when agent is not learning.
     *
     */
    private double movingAverageRewardAsNotLearning = Double.MIN_VALUE;

    /**
     * Trajectory length count for non-episodic agent.
     *
     */
    private int nonEpisodicTrajectoryCount;

    /**
     * Trajectory length for non-episodic agent.
     *
     */
    private int nonEpisodicTrajectoryHistorySize;

    /**
     * Last non-episodic state in history.
     *
     */
    private State lastNonEpisodicState;

    /**
     * Episode ID when agent was last updated.
     *
     */
    private int agentLastUpdatedEpisodeID;

    /**
     * Time step when agent was last updated for non-episodic updates.
     *
     */
    private int agentLastUpdatedTimeStep;

    /**
     * Constructor for deep agent.
     *
     * @param stateSynchronization reference to state synchronization.
     * @param environment reference to environment.
     * @param policy reference to policy.
     * @param valueFunction reference to value function.
     */
    public DeepAgent(StateSynchronization stateSynchronization, Environment environment, Policy policy, ValueFunction valueFunction) {
        this.stateSynchronization = stateSynchronization;
        this.environment = environment;
        this.episodic = environment.isEpisodic();
        this.policy = policy;
        this.valueFunction = valueFunction;

        initializeDefaultParams();
    }

    /**
     * Constructor for deep agent.
     *
     * @param stateSynchronization reference to state synchronization.
     * @param environment reference to environment.
     * @param policy reference to policy.
     * @param valueFunction reference to value function.
     * @param params parameters for deep agent.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public DeepAgent(StateSynchronization stateSynchronization, Environment environment, Policy policy, ValueFunction valueFunction, String params) throws DynamicParamException {
        this(stateSynchronization, environment, policy, valueFunction);

        this.params = params;
        if (params != null) {
            DynamicParam dynamicParam = new DynamicParam(params, getParamDefs() + ", " + policy.getParamDefs() + ", " + valueFunction.getParamDefs());
            setParams(dynamicParam);
            policy.setParams(dynamicParam);
            valueFunction.setParams(dynamicParam);
        }
    }

    /**
    * Setter for environment
    *
    * @param environment reference to environment.
    * @return this DeepAgent ob
    */
    public DeepAgent setEnvironment(Environment environment){
        this.environment = environment;
        return this;
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        isLearning = true;
        updateValuePerEpisode = false;
        agentUpdateCycle = episodic ? 1 : 10;
        rewardTau = 0.9;
        rewardMovingAverageTau = 0.99;
        nonEpisodicTrajectoryHistorySize = 11;
        nonEpisodicTrajectoryCount = 0;
        agentLastUpdatedEpisodeID = 0;
        agentLastUpdatedTimeStep = 0;
    }

    /**
     * Returns parameters used for deep agent.
     *
     * @return parameters used for deep agent.
     */
    public String getParamDefs() {
        return DeepAgent.paramNameTypes;
    }

    /**
     * Sets parameters used for deep agent.<br>
     * <br>
     * Supported parameters are:<br>
     *     - updateValuePerEpisode: if true updates value after each episode is completed. Default value false.<br>
     *     - agentUpdateCycle: estimator update cycle. Default value 1.<br>
     *     - rewardTau: tau value for reward averaging in non-episodic learning. Default value 0.9.<br>
     *     - nonEpisodicTrajectoryHistorySize: moving historical trajectory size for non-episodic agent. Default value 11.<br>
     *
     * @param params parameters used for deep agent.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        if (params.hasParam("updateValuePerEpisode")) updateValuePerEpisode = params.getValueAsBoolean("updateValuePerEpisode");
        if (params.hasParam("agentUpdateCycle")) agentUpdateCycle = params.getValueAsInteger("agentUpdateCycle");
        if (params.hasParam("rewardTau")) rewardTau = params.getValueAsDouble("rewardTau");
        if (params.hasParam("nonEpisodicTrajectoryHistorySize")) nonEpisodicTrajectoryHistorySize = params.getValueAsInteger("nonEpisodicTrajectoryHistorySize");
    }

    /**
     * Returns parameters of deep agent.
     *
     * @return parameters of deep agent.
     */
    protected String getParams() {
        return params;
    }

    /**
     * Returns reference to state synchronization.
     *
     * @return reference to state synchronization.
     */
    public StateSynchronization getStateSynchronization() {
        return stateSynchronization;
    }

    /**
     * Returns reference to environment.
     *
     * @return reference to environment.
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Starts deep agent.
     *
     * @throws NeuralNetworkException throws exception if start of neural network estimator(s) fails.
     * @throws MatrixException throws exception if depth of matrix is less than 1.
     * @throws IOException throws exception if creation of FunctionEstimator copy fails.
     * @throws ClassNotFoundException throws exception if creation of FunctionEstimator copy fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void start() throws NeuralNetworkException, MatrixException, DynamicParamException, IOException, ClassNotFoundException {
        policy.start(this);
        if (valueFunction != null) valueFunction.start(this);
    }

    /**
     * Stops deep agent.
     *
     * @throws NeuralNetworkException throws exception is neural network is not started.
     */
    public void stop() throws NeuralNetworkException {
        policy.stop();
        if (valueFunction != null) valueFunction.stop();
    }

    /**
     * Starts episode.
     *
     */
    public void startEpisode() {
        stateSynchronization.newEpisode();
    }

    /**
     * Begins new episode step for deep agent.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws NeuralNetworkException throws exception if starting of value function estimator fails.
     * @throws IOException throws exception if creation of FunctionEstimator copy fails.
     * @throws ClassNotFoundException throws exception if creation of FunctionEstimator copy fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if update cycle is ongoing.
     */
    public void newTimeStep() throws MatrixException, DynamicParamException, NeuralNetworkException, AgentException, IOException, ClassNotFoundException {
        state = stateSynchronization.getNextState(environment);

        if (!episodic) {
            if (lastNonEpisodicState == null) lastNonEpisodicState = state;
            if (nonEpisodicTrajectoryCount >= nonEpisodicTrajectoryHistorySize - 1) {
                lastNonEpisodicState.previousState = null;
                if (lastNonEpisodicState.nextState != null) lastNonEpisodicState = lastNonEpisodicState.nextState;
            }
            else nonEpisodicTrajectoryCount++;
            endEpisode();
        }
    }

    /**
     * Ends episode and if end of update cycle is reached updates deep agent.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws NeuralNetworkException throws exception if starting of value function estimator fails.
     * @throws IOException throws exception if creation of FunctionEstimator copy fails.
     * @throws ClassNotFoundException throws exception if creation of FunctionEstimator copy fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if update cycle is ongoing.
     */
    public void endEpisode() throws MatrixException, NeuralNetworkException, DynamicParamException, AgentException, IOException, ClassNotFoundException {
        if (updateValuePerEpisode) valueFunction.update(state);

        policy.endEpisode();

        if (policy.isLearning()) {
            boolean makeUpdate = false;
            if (episodic) {
                if (state.getEpisodeID() >= agentLastUpdatedEpisodeID + agentUpdateCycle) {
                    agentLastUpdatedEpisodeID = state.getEpisodeID();
                    makeUpdate = true;
                }
            }
            else {
                if (state.getTimeStep() >= agentLastUpdatedTimeStep + agentUpdateCycle) {
                    agentLastUpdatedTimeStep = state.getTimeStep();
                    makeUpdate = true;
                }
            }
            if (makeUpdate) {
                updateFunctionEstimator();
                policy.increment();
            }
        }
    }

    /**
     * Enables learning.
     *
     */
    public void enableLearning() {
        isLearning = true;
        policy.setLearning(true);
    }

    /**
     * Disables learning.
     *
     */
    public void disableLearning() {
        isLearning = false;
        policy.setLearning(false);
    }

    /**
     * Takes action per defined agent policy.
     *
     * @throws NeuralNetworkException throws exception if neural network operation fails.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void act() throws NeuralNetworkException, MatrixException {
        act(false);
    }

    /**
     * Takes action per defined agent policy.
     *
     * @param alwaysGreedy if true greedy action is always taken.
     * @throws NeuralNetworkException throws exception if neural network operation fails.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void act(boolean alwaysGreedy) throws NeuralNetworkException, MatrixException {
        policy.act(state, alwaysGreedy);
        environment.commitAction(this, state.action);
    }

    /**
     * Takes action defined by external agent.
     *
     * @param action action.
     * @throws NeuralNetworkException throws exception if neural network operation fails.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void act(int action) throws NeuralNetworkException, MatrixException {
        state.action = action;
        policy.act(state);
        environment.commitAction(this, state.action);
    }

    /**
     * Assigns immediate reward from environment in response to action agent executed.
     *
     * @param reward immediate reward.
     */
    public void respond(double reward) {
        state.reward = reward;
        if (!episodic && averageReward != Double.MIN_VALUE) {
            averageReward = rewardTau * averageReward + (1 - rewardTau) * reward;
            state.reward -= rewardTau > 0 ? averageReward : 0;
        }
        else averageReward = state.reward;

        if (isLearning) {
            cumulativeRewardAsLearning += state.reward;
            movingAverageRewardAsLearning = movingAverageRewardAsLearning == Double.MIN_VALUE ? state.reward : movingAverageRewardAsNotLearning * rewardMovingAverageTau + (1 - rewardMovingAverageTau) * state.reward;
        }
        else {
            cumulativeRewardAsNotLearning += state.reward;
            movingAverageRewardAsNotLearning = movingAverageRewardAsNotLearning == Double.MIN_VALUE ? state.reward : movingAverageRewardAsNotLearning * rewardMovingAverageTau + (1 - rewardMovingAverageTau) * state.reward;
        }
    }

    /**
     * Returns cumulative reward.
     *
     * @param isLearning if true returns cumulative reward during learning otherwise returns cumulative reward when not learning
     * @return cumulative reward.
     */
    public double getCumulativeReward(boolean isLearning) {
        return isLearning ? cumulativeRewardAsLearning : cumulativeRewardAsNotLearning;
    }

    /**
     * Returns moving average reward.
     *
     * @param isLearning if true returns moving average reward during learning otherwise return moving average reward when not learning
     * @return moving average reward.
     */
    public double getMovingAverageReward(boolean isLearning) {
        return isLearning ? movingAverageRewardAsLearning : movingAverageRewardAsNotLearning;
    }

    /**
     * Resets cumulative and moving average reward metrics to zero.
     *
     */
    public void resetRewardMetrics() {
        cumulativeRewardAsLearning = 0;
        cumulativeRewardAsNotLearning = 0;
        movingAverageRewardAsLearning = Double.MIN_VALUE;
        movingAverageRewardAsNotLearning = Double.MIN_VALUE;
    }

    /**
     * Updates policy and value functions of agent.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws NeuralNetworkException throws exception if neural network operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if update of estimator fails.
     * @throws IOException throws exception if creation of FunctionEstimator copy fails.
     * @throws ClassNotFoundException throws exception if creation of FunctionEstimator copy fails.
     */
    protected abstract void updateFunctionEstimator() throws MatrixException, NeuralNetworkException, DynamicParamException, AgentException, IOException, ClassNotFoundException;

    /**
     * Getter for the value function estimator NN
     *
     * @return Function estimator NN for the value functions
     */
    public NeuralNetwork getValueFunctionEstimatorNN(){
        if(valueFunction == null || valueFunction.getFunctionEstimator() == null) return null;

        if(valueFunction.getFunctionEstimator() instanceof NNFunctionEstimator)
            return ((NNFunctionEstimator)valueFunction.getFunctionEstimator()).getNeuralNetwork();
        return null;
    }

    /**
     * Getter for the policy function estimator NN
     *
     * @return NN policy estimator
     */
    public NeuralNetwork getPolicyEstimatorNN(){
        if(policy == null || policy.getFunctionEstimator() == null) return null;
        if(policy.getFunctionEstimator() instanceof NNFunctionEstimator)
            return ((NNFunctionEstimator)policy.getFunctionEstimator()).getNeuralNetwork();
        return null;
    }
}
