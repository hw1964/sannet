/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.layer.feedforward;

import core.network.NeuralNetworkException;
import utils.configurable.DynamicParamException;
import utils.matrix.*;

/**
 * Implements layer that dots multiple inputs.
 *
 */
public class DotLayer extends AbstractMultiInputLayer {

    /**
     * Constructor for dot layer.
     *
     * @param layerIndex layer index
     * @param initialization initialization function for weight.
     * @param params parameters for dot layer.
     * @throws NeuralNetworkException throws exception if setting of activation function fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public DotLayer(int layerIndex, Initialization initialization, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        super (layerIndex, initialization, params);
    }

    /**
     * Executes operation
     *
     * @param input current input.
     * @param output current output
     * @return result.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    protected Matrix executeOperation(Matrix input, Matrix output) throws MatrixException {
        return output.dot(input);
    }

}
