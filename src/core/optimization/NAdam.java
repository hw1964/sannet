/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.optimization;

import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;
import utils.matrix.UnaryFunctionType;

import java.util.HashMap;

/**
 * Implements Nadam optimizer.<br>
 * <br>
 * Reference: <a href="http://ruder.io/optimizing-gradient-descent/">...</a> <br>
 *
 */
public class NAdam extends AbstractOptimizer {

    /**
     * Parameter name types for NAdam.
     *     - learningRate: learning rate for optimizer. Default value 0.001.<br>
     *     - beta1: beta1 value for optimizer. Default value 0.9.<br>
     *     - beta2: beta2 value for optimizer. Default value 0.999.<br>
     *
     */
    private final static String paramNameTypes = "(learningRate:DOUBLE), " +
            "(beta1:DOUBLE), " +
            "(beta2:DOUBLE)";

    /**
     * Learning rate for Nadam. Default value 0.001.
     *
     */
    private double learningRate;

    /**
     * Beta1 term for Nadam. Default value 0.9.
     *
     */
    private double beta1;

    /**
     * Beta2 term for Nadam. Default value 0.999.
     *
     */
    private double beta2;

    /**
     * Hash map to store iteration counts.
     *
     */
    private final HashMap<Matrix, Integer> iterations = new HashMap<>();

    /**
     * Hash map to store first moments (means).
     *
     */
    private final HashMap<Matrix, Matrix> m = new HashMap<>();

    /**
     * Hash map to store second moments (uncentered variances).
     *
     */
    private final HashMap<Matrix, Matrix> v = new HashMap<>();

    /**
     * Default constructor for Nadam.
     *
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public NAdam() throws DynamicParamException {
        super(OptimizationType.NADAM, NAdam.paramNameTypes);
    }

    /**
     * Constructor for Nadam.
     *
     * @param params parameters for Nadam.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public NAdam(String params) throws DynamicParamException {
        super(OptimizationType.NADAM, NAdam.paramNameTypes, params);
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        learningRate = 0.001;
        beta1 = 0.9;
        beta2 = 0.999;
    }

    /**
     * Sets parameters used for Nadam.<br>
     * <br>
     * Supported parameters are:<br>
     *     - learningRate: learning rate for optimizer. Default value 0.001.<br>
     *     - beta1: beta1 value for optimizer. Default value 0.9.<br>
     *     - beta2: beta2 value for optimizer. Default value 0.999.<br>
     *
     * @param params parameters used for Nadam.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        if (params.hasParam("learningRate")) learningRate = params.getValueAsDouble("learningRate");
        if (params.hasParam("beta1")) beta1 = params.getValueAsDouble("beta1");
        if (params.hasParam("beta2")) beta2 = params.getValueAsDouble("beta2");
    }

    /**
     * Resets optimizer state.
     *
     */
    public void reset() {
        iterations.clear();
        m.clear();
        v.clear();
    }

    /**
     * Optimizes single matrix (M) using calculated matrix gradient (dM).<br>
     * Matrix can be for example weight or bias matrix with gradient.<br>
     *
     * @param matrix matrix to be optimized.
     * @param matrixGradient matrix gradients for optimization step.
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void optimize(Matrix matrix, Matrix matrixGradient) throws MatrixException, DynamicParamException {
        int iteration;
        iterations.put(matrix, iteration = iterations.getOrDefault(matrix, 0) + 1);


        // mt = β1*mt − 1 + (1 − β1)*gt
        Matrix mM = getParameterMatrix(m, matrix);
        mM = mM.multiply(beta1).add(matrixGradient.multiply(1 - beta1));
        setParameterMatrix(m, matrix, mM);

        // vt = β2*vt − 1 + (1 − β2)*g2t
        Matrix vM = getParameterMatrix(v, matrix);
        vM = vM.multiply(beta2).add(matrixGradient.power(2).multiply(1 - beta2));
        setParameterMatrix(v, matrix, vM);

        // mt = mt / (1 − βt1)
        Matrix mM_hat = mM.divide(1 - Math.pow(beta1, iteration));

        // vt = vt / (1 − βt2)
        Matrix vM_hat = vM.divide(1 - Math.pow(beta2, iteration));

        // θt+1 = θt − η / (√^vt+ϵ) * (β1 * mt + (1 − β1) * gt / (1 − βt1))
        double epsilon = 10E-8;
        matrix.subtractBy(mM_hat.multiply(beta1).add(matrixGradient.multiply((1 - beta1) / (1 - Math.pow(beta1, iteration)))).divide(vM_hat.add(epsilon).apply(UnaryFunctionType.SQRT)).multiply(learningRate));
    }

}
