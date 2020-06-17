/********************************************************
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 *
 ********************************************************/

package utils.procedure;

import utils.matrix.Matrix;
import utils.matrix.MatrixException;
import utils.matrix.UnaryFunction;
import utils.matrix.UnaryFunctionType;

import java.io.Serializable;

/**
 * Class that describes expression for standard deviation operation.
 *
 */
public class StandardDeviationExpression extends AbstractUnaryExpression implements Serializable {

    /**
     * True if calculation is done as multi matrix.
     *
     */
    private final boolean asMultiMatrix;

    /**
     * Mean value as matrix.
     *
     */
    private Matrix mean;

    /**
     * Operation for square root.
     *
     */
    private final UnaryFunction sqrtFunction = new UnaryFunction(UnaryFunctionType.SQRT);

    /**
     * Constructor for standard deviation operation.
     *
     * @param expressionID unique ID for expression.
     * @param argument1 first argument.
     * @param result result of expression.
     * @param asMultiMatrix true if calculation is done per index otherwise over all indices.
     * @throws MatrixException throws exception if expression arguments are not defined.
     */
    public StandardDeviationExpression(int expressionID, Node argument1, Node result, boolean asMultiMatrix) throws MatrixException {
        super(expressionID, argument1, result);
        this.asMultiMatrix = asMultiMatrix;
    }

    /**
     * Calculates expression.
     *
     * @throws MatrixException throws exception if calculation fails.
     */
    public void calculateExpression() throws MatrixException {
        if (!asMultiMatrix) return;
        if (argument1.getMatrices() == null) throw new MatrixException("Arguments for STANDARD DEVIATION operation not defined");
        mean = argument1.getMatrices().mean();
        Matrix standardDeviation = argument1.getMatrices().standardDeviation(mean);
        result.setMultiIndex(false);
        result.setMatrix(standardDeviation);
    }

    /**
     * Calculates expression.
     *
     * @param index data index.
     * @throws MatrixException throws exception if calculation fails.
     */
    public void calculateExpression(int index) throws MatrixException {
        if (asMultiMatrix) return;
        if (argument1.getMatrix(index) == null) throw new MatrixException("Arguments for STANDARD DEVIATION operation not defined");
        mean = argument1.getMatrix(index).meanAsMatrix();
        result.setMatrix(index, argument1.getMatrix(index).standardDeviationAsMatrix(mean));
    }

    /**
     * Calculates gradient of expression.
     *
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    public void calculateGradient() throws MatrixException {
        if (!asMultiMatrix) return;
        if (result.getGradient() == null) throw new MatrixException("Result gradient not defined.");
        for (Integer index : argument1.keySet()) {
            Matrix standardDeviationGradient = argument1.getMatrix(index).subtract(mean).multiply(2 / (double)argument1.size() - 1).apply(sqrtFunction.getDerivative());
            argument1.updateGradient(index, result.getGradient().multiply(standardDeviationGradient),true);
        }
    }

    /**
     * Calculates gradient of expression.
     *
     * @param index data index.
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    public void calculateGradient(int index) throws MatrixException {
        if (asMultiMatrix) return;
        if (result.getGradient(index) == null) throw new MatrixException("Result gradient not defined.");
        Matrix standardDeviationGradient = argument1.getMatrix(index).subtract(mean).multiply(2 / (double)(result.getGradient(index).size() - 1)).apply(sqrtFunction.getDerivative());
        argument1.updateGradient(index, result.getGradient(index).multiply(standardDeviationGradient), true);
    }

    /**
     * Prints expression.
     *
     */
    public void printExpression() {
        System.out.print("Expression " +getExpressionID() + ": ");
        System.out.println("STANDARD DEVIATION(" + argument1.getName() + ") = " + result.getName());
    }

    /**
     * Prints gradient.
     *
     */
    public void printGradient() {
        System.out.print("Expression " +getExpressionID() + ": ");
        System.out.println("STANDARD DEVIATION: d" + argument1.getName() + " = d" + result.getName() + " * SQRT_GRADIENT((" + argument1.getName() + " - MEAN("  + argument1.getName() + ")) * 2 / SIZE(" + argument1.getName() + "))");
    }

}