/********************************************************
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 *
 ********************************************************/

package core.normalization;

import core.optimization.Optimizer;
import utils.*;
import utils.matrix.*;
import utils.procedure.ForwardProcedure;
import utils.procedure.Node;
import utils.procedure.Procedure;
import utils.procedure.ProcedureFactory;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class that implements Weight Normalization for neural network layer.<br>
 * <br>
 * Reference: https://arxiv.org/pdf/1602.07868.pdf<br>
 *
 */
public class WeightNormalization implements Normalization, ForwardProcedure, Serializable {

    private static final long serialVersionUID = 1741544680542755148L;

    /**
     * Type of normalization.
     *
     */
    private final NormalizationType normalizationType;

    /**
     * Tree map for un-normalized weights.
     *
     */
    private transient HashMap<Matrix, Matrix> weights = new HashMap<>();

    /**
     * Weight normalization scalar.
     *
     */
    private double g = 1;

    /**
     * Matrix for g value.
     *
     */
    private Matrix gMatrix;

    /**
     * Input matrix for procedure construction.
     *
     */
    private Matrix input;

    /**
     * Procedures for weight normalization.
     *
     */
    private transient HashMap<Matrix, Procedure> procedures = new HashMap<>();

    /**
     * Constructor for Weight normalization class.
     *
     * @param normalizationType normalizationType.
     */
    public WeightNormalization(NormalizationType normalizationType) {
        this.normalizationType = normalizationType;
        gMatrix = new DMatrix(g, "g");
    }

    /**
     * Constructor for Weight normalization class.
     *
     * @param normalizationType normalizationType.
     * @param params parameters for Weight normalization.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public WeightNormalization(NormalizationType normalizationType, String params) throws DynamicParamException {
        this(normalizationType);
        this.setParams(new DynamicParam(params, getParamDefs()));
        gMatrix = new DMatrix(g);
    }

    /**
     * Returns parameters used for Weight normalization.
     *
     * @return parameters used for Weight normalization.
     */
    private HashMap<String, DynamicParam.ParamType> getParamDefs() {
        HashMap<String, DynamicParam.ParamType> paramDefs = new HashMap<>();
        paramDefs.put("g", DynamicParam.ParamType.INT);
        return paramDefs;
    }

    /**
     * Sets parameters used for Weight Normalization.<br>
     * <br>
     * Supported parameters are:<br>
     *     - g: g multiplier value for normalization. Default value 1.<br>
     *
     * @param params parameters used for weight normalization.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        if (params.hasParam("g")) g = params.getValueAsInteger("g");
    }

    /**
     * Returns input matrices for procedure construction.
     *
     * @param resetPreviousInput if true resets also previous input.
     * @return input matrix for procedure construction.
     */
    public MMatrix getInputMatrices(boolean resetPreviousInput) {
        return new MMatrix(input);
    }

    /**
     * Builds forward procedure and implicitly builds backward procedure.
     *
     * @return output of forward procedure.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public MMatrix getForwardProcedure() throws MatrixException {
        MMatrix outputs = new MMatrix(1, "Output");
        Matrix output = input.multiply(gMatrix).divide(input.normAsMatrix(2));
        output.setName("Output");
        outputs.put(0, output);
        return outputs;
    }

    /**
     * Resets Weight normalizer.
     *
     */
    public void reset() {
        weights = new HashMap<>();
    }

    /**
     * Sets flag for Weight normalization if neural network is in training state.
     *
     * @param isTraining if true neural network is in state otherwise false.
     */
    public void setTraining(boolean isTraining) {
    }

    /**
     * Sets optimizer for normalizer.
     *
     * @param optimizer optimizer
     */
    public void setOptimizer(Optimizer optimizer) {
    }

    /**
     * Initializes normalization.
     *
     * @param node node for normalization.
     */
    public void initialize(Node node) {
    }

    /**
     * Initializes normalization.
     *
     * @param weight weight for normalization.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void initialize(Matrix weight) throws MatrixException {
        initializeProcedure(weight);
    }

    /**
     * Initializes weight normalization procedure.
     *
     * @param weight weight matrix for initialization.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    private void initializeProcedure(Matrix weight) throws MatrixException {
        if (procedures == null) procedures = new HashMap<>();
        if (!procedures.containsKey(weight)) {
            input = weight;
            procedures.put(weight, new ProcedureFactory().getProcedure(this, null));
        }
    }

    /**
     * Normalizes each weight for forward step i.e. multiplies each weight matrix by g / sqrt(2-norm of weights).
     *
     * @param weight weight for normalization.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void forward(Matrix weight) throws MatrixException {
        weights.put(weight, weight.copy());
        procedures.get(weight).reset();
        weight.setEqualTo(procedures.get(weight).calculateExpression(weight));
    }

    /**
     * Finalizes forward step for normalization.<br>
     * Used typically for weight normalization.<br>
     *
     * @param weight weight for normalization.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void forwardFinalize(Matrix weight) throws MatrixException {
        weight.setEqualTo(weights.get(weight));
    }

    /**
     * Executes backward propagation step for Weight normalization.<br>
     * Calculates gradients backwards at step end for previous layer.<br>
     *
     * @param weight weight for backward normalization.
     * @param weightGradient gradient of weight for backward normalization.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void backward(Matrix weight, Matrix weightGradient) throws MatrixException {
        weightGradient.setEqualTo(procedures.get(weight).calculateGradient(weightGradient));
    }

    /**
     * Not used.
     *
     * @param node node for normalization.
     */
    public void forward(Node node) {}

    /**
     * Not used.
     *
     * @param node node for normalization.
     */
    public void backward(Node node) {}

    /**
     * Not used.
     *
     * @param node node for normalization.
     * @param inputIndex input index for normalization.
     */
    public void forward(Node node, int inputIndex) {}

    /**
     * Not used.
     *
     * @param node node for normalization.
     * @param outputIndex input index for normalization.
     */
    public void backward(Node node, int outputIndex) {}

    /**
     * Executes optimizer step for normalizer.
     *
     */
    public void optimize() {}

    /**
     * Returns name of normalization.
     *
     * @return name of normalization.
     */
    public String getName() {
        return normalizationType.toString();
    }

    /**
     * Prints expression chains of normalization.
     *
     */
    public void printExpressions() {
        if (procedures.size() == 0) return;
        System.out.println("Normalization: " + getName() + " : ");
        for (Procedure procedure : procedures.values()) procedure.printExpressionChain();
        System.out.println();
    }

    /**
     * Prints gradient chains of normalization.
     *
     */
    public void printGradients() {
        if (procedures.size() == 0) return;
        System.out.println("Normalization: " + getName() + " : ");
        for (Procedure procedure : procedures.values()) procedure.printGradientChain();
        System.out.println();
    }

}