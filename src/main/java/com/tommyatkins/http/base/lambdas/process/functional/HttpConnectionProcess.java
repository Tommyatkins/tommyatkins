package com.tommyatkins.http.base.lambdas.process.functional;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.tommyatkins.http.base.lambdas.process.enu.ProcessPoint;

@FunctionalInterface
public interface HttpConnectionProcess<V> {

	final ProcessPoint[] point = { ProcessPoint.NONE };

	V process(HttpURLConnection connection) throws IOException;

	public default HttpConnectionProcess<V> point(ProcessPoint position) {
		point[0] = position;
		return this;
	}

	public default ProcessPoint point() {
		return point[0];
	}
}
