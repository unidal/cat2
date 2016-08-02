package org.unidal.cat.core.report.view.svg;

public interface ValueTranslater {
	public double getMaxValue(double[] values);

	public int[] translate(int height, double maxValue, double[] values);
}
